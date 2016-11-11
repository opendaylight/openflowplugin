package org.opendaylight.scale.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.scale.dataaccess.StorageWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class BulkModStatistics {
    private final ExecutorService executor = new ForkJoinPool();
    private long expectedCount = 0;

    private LongAdder successCount = new LongAdder();
    private LongAdder failureCount = new LongAdder();
    private long startTime = 0;
    private static Logger LOG = LoggerFactory.getLogger(BulkModStatistics.class);

    public void start(long expectedCount) {
        this.expectedCount = expectedCount;
        startTime = System.nanoTime();
    }

    public long getSuccessCount() {
        return successCount.longValue();
    }

    public long getFailureCount() {
        return failureCount.longValue();
    }

    public long getStartTime() {
        return startTime;
    }

    public void add(ResultSetFuture resultSetFuture) {
        ListenableFuture<ResultSet> future = resultSetFuture;
        Futures.addCallback(future, new ResultSetHandler(), executor);
    }

    private class ResultSetHandler implements FutureCallback<ResultSet> {
        @Override
        public void onSuccess(@Nullable ResultSet rows) {
            successCount.increment();
        }

        @Override
        public void onFailure(Throwable throwable) {
            failureCount.increment();
            LOG.error("Failed to insert:",throwable);
        }
    }
}
