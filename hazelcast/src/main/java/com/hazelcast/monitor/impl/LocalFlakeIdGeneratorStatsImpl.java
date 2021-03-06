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

package com.hazelcast.monitor.impl;

import static com.hazelcast.util.JsonUtil.getLong;
import static java.util.concurrent.atomic.AtomicLongFieldUpdater.newUpdater;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import com.eclipsesource.json.JsonObject;
import com.hazelcast.internal.metrics.Probe;
import com.hazelcast.monitor.LocalFlakeIdGeneratorStats;
import com.hazelcast.util.Clock;

public class LocalFlakeIdGeneratorStatsImpl implements LocalFlakeIdGeneratorStats {

    private static final AtomicLongFieldUpdater<LocalFlakeIdGeneratorStatsImpl> USAGE_COUNT =
            newUpdater(LocalFlakeIdGeneratorStatsImpl.class, "usageCount");

    @Probe
    private volatile long creationTime;
    @Probe
    private volatile long usageCount;

    public LocalFlakeIdGeneratorStatsImpl() {
        creationTime = Clock.currentTimeMillis();
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public long getUsageCount() {
        return usageCount;
    }

    public void incrementUsage() {
        USAGE_COUNT.incrementAndGet(this);
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.add("creationTime", creationTime);
        root.add("usageCount", usageCount);
        return root;
    }

    @Override
    public void fromJson(JsonObject json) {
        creationTime = getLong(json, "creationTime", -1L);
        usageCount = getLong(json, "usageCount", -1L);
    }

    @Override
    public String toString() {
        return "LocalFlakeIdStatsImpl{" + "creationTime=" + creationTime + ", usageCount=" + usageCount + '}';
    }
}
