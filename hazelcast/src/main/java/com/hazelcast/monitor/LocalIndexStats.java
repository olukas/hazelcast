package com.hazelcast.monitor;

/**
 * Provides local statistics for an index to be used by {@link MemberState}
 * implementations.
 */
public interface LocalIndexStats extends LocalInstanceStats {

    /**
     * Returns the current number of entries indexed by the index.
     */
    long getEntryCount();

    /**
     * Returns the total number of queries served by the index.
     * <p>
     * To calculate the index hit rate just divide the returned value by a value
     * returned by {@link LocalMapStats#getQueryCount()}.
     * <p>
     * The returned value may be less than the one returned by {@link
     * #getHitCount()} since a single query may hit the same index more than once.
     * <p>
     * Following operations counted as a query:
     * <ul>
     * <li>{@link com.hazelcast.query.impl.Index#getRecords(Comparable) Index.getRecords(Comparable)}
     * <li>{@link com.hazelcast.query.impl.Index#getRecords(Comparable[]) Index.getRecords(Comparable[])}
     * <li>{@link com.hazelcast.query.impl.Index#getSubRecords Index.getSubRecords}
     * <li>{@link com.hazelcast.query.impl.Index#getSubRecordsBetween Index.getSubRecordsBetween}
     * </ul>
     */
    long getQueryCount();

    /**
     * Returns the total number of hits into the index.
     * <p>
     * The returned value may be greater than the one returned by {@link
     * #getQueryCount} since a single query may hit the same index more than once.
     * <p>
     * Following operations generate a hit:
     * <ul>
     * <li>{@link com.hazelcast.query.impl.Index#getRecords(Comparable) Index.getRecords(Comparable)}
     * <li>{@link com.hazelcast.query.impl.Index#getRecords(Comparable[]) Index.getRecords(Comparable[])}
     * <li>{@link com.hazelcast.query.impl.Index#getSubRecords Index.getSubRecords}
     * <li>{@link com.hazelcast.query.impl.Index#getSubRecordsBetween Index.getSubRecordsBetween}
     * </ul>
     */
    long getHitCount();

    /**
     * Returns the average hit latency for the index.
     */
    long getAverageHitLatency();

    /**
     * Returns the average selectivity of the hits served by the index.
     * <p>
     * The returned value is in the range from 0.0 to 1.0. Values close to 1.0
     * indicate a high selectivity meaning the index is efficient; values close
     * to 0.0 indicate a low selectivity meaning the index efficiency is
     * approaching an efficiency of a simple full scan.
     */
    double getAverageHitSelectivity();

    /**
     * Returns the number of insert operations performed on the index.
     */
    long getInsertCount();

    /**
     * Returns the total latency (in nanoseconds) of insert operations performed
     * on the index.
     * <p>
     * To compute the average latency divide the returned value by {@link
     * #getInsertCount() insert operation count}.
     */
    long getTotalInsertLatency();

    /**
     * Returns the number of update operations performed on the index.
     */
    long getUpdateCount();

    /**
     * Returns the total latency (in nanoseconds) of update operations performed
     * on the index.
     * <p>
     * To compute the average latency divide the returned value by {@link
     * #getUpdateCount() update operation count}.
     */
    long getTotalUpdateLatency();

    /**
     * Returns the number of remove operations performed on the index.
     */
    long getRemoveCount();

    /**
     * Returns the total latency (in nanoseconds) of remove operations performed
     * on the index.
     * <p>
     * To compute the average latency divide the returned value by {@link
     * #getRemoveCount() remove operation count}.
     */
    long getTotalRemoveLatency();

    /**
     * Returns the on-heap memory cost of the index in bytes.
     * <p>
     * Currently, the returned value is just a best-effort approximation and
     * doesn't indicate the precise on-heap memory usage of the index.
     */
    long getOnHeapMemoryCost();

    /**
     * Returns the off-heap memory cost of the index in bytes.
     * <p>
     * The returned value includes all active off-heap allocations associated
     * with the index.
     */
    long getOffHeapMemoryCost();

}
