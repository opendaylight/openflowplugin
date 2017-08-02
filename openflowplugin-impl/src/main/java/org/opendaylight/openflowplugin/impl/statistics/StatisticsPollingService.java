/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StatisticsPollingService extends AbstractScheduledService {
    private static final long DEFAULT_STATS_TIMEOUT = 50000;

    private final TimeCounter counter;
    private final long pollingInterval;
    private final long maximumTimerDelay;
    private final Supplier<ListenableFuture<Boolean>> gatheringSupplier;

    StatisticsPollingService(@Nonnull final TimeCounter counter,
                             final long pollingInterval,
                             final long maximumTimerDelay,
                             @Nonnull final Supplier<ListenableFuture<Boolean>> gatheringSupplier) {
        this.counter = counter;
        this.pollingInterval = pollingInterval;
        this.maximumTimerDelay = maximumTimerDelay;
        this.gatheringSupplier = gatheringSupplier;
    }

    @Override
    protected void startUp() throws Exception {
        counter.markStart();
    }

    @Override
    protected void runOneIteration() throws Exception {
        final long averageTime = counter.getAverageTimeBetweenMarks();
        final long statsTimeout = averageTime > 0 ? 3 * averageTime : DEFAULT_STATS_TIMEOUT;
        final CompletableFuture<Boolean> waitFuture = new CompletableFuture<>();

        Futures.addCallback(gatheringSupplier.get(), new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable final Boolean result) {
                waitFuture.complete(result);
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
                waitFuture.completeExceptionally(t);
            }
        }, MoreExecutors.directExecutor());

        try {
            waitFuture.get(statsTimeout, TimeUnit.MILLISECONDS);
        } finally {
            counter.addTimeMark();
        }
    }

    @Override
    protected Scheduler scheduler() {
        final long averageStatisticsGatheringTime = counter.getAverageTimeBetweenMarks();
        long currentTimerDelay = pollingInterval;

        if (averageStatisticsGatheringTime > currentTimerDelay) {
            currentTimerDelay = averageStatisticsGatheringTime;

            if (currentTimerDelay > maximumTimerDelay) {
                currentTimerDelay = maximumTimerDelay;
            }
        }

        return Scheduler.newFixedDelaySchedule(currentTimerDelay, currentTimerDelay, TimeUnit.MILLISECONDS);
    }
}