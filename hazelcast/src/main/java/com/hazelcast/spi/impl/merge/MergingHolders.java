/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.spi.impl.merge;

import com.hazelcast.cache.CacheEntryView;
import com.hazelcast.cache.impl.record.CacheRecord;
import com.hazelcast.cardinality.impl.hyperloglog.HyperLogLog;
import com.hazelcast.collection.impl.collection.CollectionItem;
import com.hazelcast.collection.impl.queue.QueueItem;
import com.hazelcast.core.EntryView;
import com.hazelcast.map.impl.record.Record;
import com.hazelcast.multimap.impl.MultiMapContainer;
import com.hazelcast.multimap.impl.MultiMapMergeContainer;
import com.hazelcast.multimap.impl.MultiMapRecord;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.replicatedmap.impl.record.ReplicatedRecord;
import com.hazelcast.scheduledexecutor.impl.ScheduledTaskDescriptor;
import com.hazelcast.spi.merge.MergingEntryHolder;
import com.hazelcast.spi.merge.MergingValueHolder;
import com.hazelcast.util.Clock;

/**
 * Provides static factory methods to create {@link MergingValueHolder} and {@link MergingEntryHolder} instances.
 *
 * @since 3.10
 */
public final class MergingHolders {

    private MergingHolders() {
    }

    public static <V> MergingValueHolder<V> createMergeHolder(V value) {
        return new MergingValueHolderImpl<V>()
                .setValue(value);
    }

    public static <K, V> MergingEntryHolder<K, V> createMergeHolder(K key, V value) {
        return new MergingEntryHolderImpl<K, V>()
                .setKey(key)
                .setValue(value)
                .setCreationTime(Clock.currentTimeMillis());
    }

    public static <K, V> MergingEntryHolder<K, V> createMergeHolder(EntryView<K, V> entryView) {
        return new FullMergingEntryHolderImpl<K, V>()
                .setKey(entryView.getKey())
                .setValue(entryView.getValue())
                .setCreationTime(entryView.getCreationTime())
                .setExpirationTime(entryView.getExpirationTime())
                .setLastUpdateTime(entryView.getLastUpdateTime())
                .setLastAccessTime(entryView.getLastAccessTime())
                .setHits(entryView.getHits())
                .setTtl(entryView.getTtl())
                .setVersion(entryView.getVersion())
                .setCost(entryView.getCost());
    }

    public static <K, V> MergingEntryHolder<K, V> createMergeHolder(CacheEntryView<K, V> entryView) {
        return new FullMergingEntryHolderImpl<K, V>()
                .setKey(entryView.getKey())
                .setValue(entryView.getValue())
                .setCreationTime(entryView.getCreationTime())
                .setExpirationTime(entryView.getExpirationTime())
                .setLastAccessTime(entryView.getLastAccessTime())
                .setHits(entryView.getAccessHit());
    }

    public static MergingValueHolder<Data> createMergeHolder(CollectionItem item) {
        return new MergingValueHolderImpl<Data>()
                .setValue(item.getValue());
    }

    public static MergingValueHolder<Data> createMergeHolder(QueueItem item) {
        return new MergingValueHolderImpl<Data>()
                .setValue(item.getData());
    }

    public static MergingEntryHolder<Data, Object> createMergeHolder(MultiMapMergeContainer container,
                                                                     MultiMapRecord record) {
        return new FullMergingEntryHolderImpl<Data, Object>()
                .setKey(container.getKey())
                .setValue(record.getObject())
                .setCreationTime(container.getCreationTime())
                .setLastAccessTime(container.getLastAccessTime())
                .setLastUpdateTime(container.getLastUpdateTime())
                .setHits(container.getHits());
    }

    public static MergingEntryHolder<Data, Object> createMergeHolder(MultiMapContainer container, Data key,
                                                                     MultiMapRecord record, int hits) {
        return new FullMergingEntryHolderImpl<Data, Object>()
                .setKey(key)
                .setValue(record.getObject())
                .setCreationTime(container.getCreationTime())
                .setLastAccessTime(container.getLastAccessTime())
                .setLastUpdateTime(container.getLastUpdateTime())
                .setHits(hits);
    }

    public static MergingEntryHolder<Data, Data> createMergeHolder(Record record, Data dataValue) {
        return new FullMergingEntryHolderImpl<Data, Data>()
                .setKey(record.getKey())
                .setValue(dataValue)
                .setCreationTime(record.getCreationTime())
                .setExpirationTime(record.getExpirationTime())
                .setHits(record.getHits())
                .setLastAccessTime(record.getLastAccessTime())
                .setLastStoredTime(record.getLastStoredTime())
                .setLastUpdateTime(record.getLastUpdateTime())
                .setVersion(record.getVersion())
                .setTtl(record.getTtl());
    }

    public static MergingEntryHolder<Data, Object> createMergeHolder(Record record) {
        return new FullMergingEntryHolderImpl<Data, Object>()
                .setKey(record.getKey())
                .setValue(record.getValue())
                .setCreationTime(record.getCreationTime())
                .setExpirationTime(record.getExpirationTime())
                .setHits(record.getHits())
                .setLastAccessTime(record.getLastAccessTime())
                .setLastStoredTime(record.getLastStoredTime())
                .setLastUpdateTime(record.getLastUpdateTime())
                .setVersion(record.getVersion())
                .setTtl(record.getTtl());
    }

    public static <R extends CacheRecord> MergingEntryHolder<Data, Data> createMergeHolder(Data key, Data value, R record) {
        return new FullMergingEntryHolderImpl<Data, Data>()
                .setKey(key)
                .setValue(value)
                .setCreationTime(record.getCreationTime())
                .setExpirationTime(record.getExpirationTime())
                .setHits(record.getAccessHit())
                .setLastAccessTime(record.getLastAccessTime());
    }

    public static MergingEntryHolder<Object, Object> createMergeHolder(ReplicatedRecord record) {
        return new FullMergingEntryHolderImpl<Object, Object>()
                .setKey(record.getKeyInternal())
                .setValue(record.getValueInternal())
                .setCreationTime(record.getCreationTime())
                .setHits(record.getHits())
                .setLastAccessTime(record.getLastAccessTime())
                .setLastUpdateTime(record.getUpdateTime())
                .setTtl(record.getTtlMillis());
    }

    public static MergingEntryHolder<String, HyperLogLog> createMergeHolder(String name, HyperLogLog item) {
        return new MergingEntryHolderImpl<String, HyperLogLog>()
                .setKey(name)
                .setValue(item)
                .setCreationTime(Clock.currentTimeMillis());
    }

    public static MergingEntryHolder<String, ScheduledTaskDescriptor> createMergeHolder(ScheduledTaskDescriptor task) {
        return new MergingEntryHolderImpl<String, ScheduledTaskDescriptor>()
                .setKey(task.getDefinition().getName())
                .setValue(task)
                .setCreationTime(Clock.currentTimeMillis());
    }
}
