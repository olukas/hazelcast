package com.hazelcast.cache.impl.maxsize;

import com.hazelcast.cache.impl.CacheInfo;
import com.hazelcast.config.MaxSizeConfig;

/**
 * @author sozal 20/11/14
 */
public class UsedHeapPercentageCacheMaxSizeChecker implements CacheMaxSizeChecker {

    public UsedHeapPercentageCacheMaxSizeChecker(CacheInfo cacheInfo, MaxSizeConfig maxSizeConfig) {

    }

    @Override
    public boolean isReachedToMaxSize() {
        // TODO Not supported yet
        return false;
    }

}
