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
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.ConnectionException;

public class StatisticsPollingService extends AbstractScheduledService {
    private static final long DEFAULT_STATS_TIMEOUT = 50000;

    private final TimeCounter counter;
    private final long pollingInterval;
    private final long maximumTimerDelay;
    private final Supplier<ListenableFuture<Boolean>> gatheringSupplier;
    private final SettableFuture<Void> future = SettableFuture.create();

    StatisticsPollingService(@NonNull final TimeCounter counter,
                             final long pollingInterval,
                             final long maximumTimerDelay,
                             @NonNull final Supplier<ListenableFuture<Boolean>> gatheringSupplier) {
        this.counter = counter;
        this.pollingInterval = pollingInterval;
        this.maximumTimerDelay = maximumTimerDelay;
        this.gatheringSupplier = gatheringSupplier;
        this.addListener(new StatisticsPollingServiceListener(), MoreExecutors.directExecutor());
    }

    ListenableFuture<Void> stop() {
        stopAsync();
        return future;
    }

    @Override
    protected void startUp() {
        counter.markStart();
    }

    @Override
    protected void runOneIteration() throws Exception {
        final long averageTime = counter.getAverageTimeBetweenMarks();
        final long statsTimeout = averageTime > 0 ? 3 * averageTime : DEFAULT_STATS_TIMEOUT;
        final CompletableFuture<Boolean> waitFuture = new CompletableFuture<>();

        Futures.addCallback(gatheringSupplier.get(), new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean result) {
                waitFuture.complete(result);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                waitFuture.completeExceptionally(throwable);
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

    private final class StatisticsPollingServiceListener extends Service.Listener {
        @Override
        public void terminated(final Service.State from) {
            super.terminated(from);
            future.set(null);
        }

        @Override
        public void failed(final Service.State from, final Throwable failure) {
            super.failed(from, failure);
            if (!(failure instanceof CancellationException) && !(failure instanceof ConnectionException)) {
                future.setException(failure);
            } else {
                future.set(null);
            }
        }
    }
}
