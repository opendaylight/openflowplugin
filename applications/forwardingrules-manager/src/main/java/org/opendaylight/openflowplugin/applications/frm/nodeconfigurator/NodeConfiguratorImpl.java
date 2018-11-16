/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.nodeconfigurator;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.errorprone.annotations.Var;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opendaylight.infrautils.utils.concurrent.LoggingUncaughtThreadDeathContextRunnable;
import org.opendaylight.openflowplugin.applications.frm.NodeConfigurator;
import org.opendaylight.yangtools.util.concurrent.NotificationManager;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeConfiguratorImpl implements NodeConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(NodeConfiguratorImpl.class);
    private static final String NODE_EXECUTOR_PREFIX = "nc-exe-";

    private final NotificationManager<String, JobEntry<?>> manager;
    private final ExecutorService syncThreadPool;

    public NodeConfiguratorImpl() {
        syncThreadPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setNameFormat(NODE_EXECUTOR_PREFIX + "%d")
                .setDaemon(true)
                .setUncaughtExceptionHandler((thread, ex) -> LOG.error("Uncaught exception {}", thread, ex))
                .build());
        manager = QueuedNotificationManager.create(syncThreadPool, (key, entries) -> {
            LOG.trace("Executing job with key: {}", key);
            entries.forEach(jobEntry -> new MainTask<>(jobEntry).run());
        }, 4096, "nc-jobqueue");
    }

    @Override
    public <T> ListenableFuture<T> enqueueJob(final String key, final Callable<ListenableFuture<T>> mainWorker) {
        final JobEntry<T> jobEntry = new JobEntry<>(key, mainWorker);
        manager.submitNotification(key, jobEntry);
        return jobEntry.getResultFuture();
    }

    @Override
    public void close() {
        LOG.info("NodeConfigurator shutting down... (tasks still running may be stopped/cancelled/interrupted)");
        syncThreadPool.shutdownNow();
        LOG.info("NodeConfigurator now closed for business.");
    }

    private static final class MainTask<T> extends LoggingUncaughtThreadDeathContextRunnable {
        private final JobEntry<T> jobEntry;

        MainTask(final JobEntry<T> jobEntry) {
            super(LOG, jobEntry::toString);
            this.jobEntry = jobEntry;
        }

        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void runWithUncheckedExceptionLogging() {
            @Var ListenableFuture<T> future = null;
            LOG.trace("Running job with key: {}", jobEntry.getKey());

            try {
                Callable<ListenableFuture<T>> mainWorker = jobEntry.getMainWorker();
                if (mainWorker != null) {
                    future = mainWorker.call();
                } else {
                    LOG.error("Unexpected no (null) main worker on job: {}", jobEntry);
                }

            } catch (Exception e) {
                LOG.error("Direct Exception (not failed Future) when executing job, won't even retry: {}", jobEntry, e);
            }

            if (future == null) {
                jobEntry.setResultFuture(null);
                return;
            }
            Futures.addCallback(future, new FutureCallback<T>() {
                @Override
                public void onSuccess(final T result) {
                    LOG.trace("Job completed successfully: {}", jobEntry.getKey());
                    jobEntry.setResultFuture(result);
                }

                @Override
                public void onFailure(final Throwable cause) {

                }
            }, MoreExecutors.directExecutor());
        }
    }
}
