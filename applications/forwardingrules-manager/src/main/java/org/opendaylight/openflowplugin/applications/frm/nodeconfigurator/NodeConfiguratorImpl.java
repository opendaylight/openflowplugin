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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        manager = QueuedNotificationManager.create(syncThreadPool, (key, jobs) -> {
            LOG.trace("Executing jobs with key: {}", key);
            jobs.forEach(NodeConfiguratorImpl::executeJob);
            LOG.trace("Finished executing jobs with key: {}", key);
        }, 4096, "nc-jobqueue");
    }

    @Override
    public <T> ListenableFuture<T> enqueueJob(final String key, final Callable<ListenableFuture<T>> mainWorker) {
        final JobEntry<T> jobEntry = new JobEntry<>(key, mainWorker);
        LOG.trace("Enqueueing job {} with key: {}", jobEntry, key);
        manager.submitNotification(key, jobEntry);
        return jobEntry.getResultFuture();
    }

    @Override
    public void close() {
        LOG.info("NodeConfigurator shutting down... (tasks still running may be stopped/cancelled/interrupted)");
        syncThreadPool.shutdownNow();
        LOG.info("NodeConfigurator now closed for business.");
    }

    private static <T> void executeJob(JobEntry<T> job) {
        LOG.trace("Running job: {}", job);

        final Callable<ListenableFuture<T>> mainWorker = job.getMainWorker();
        if (mainWorker == null) {
            LOG.error("Unexpected no (null) main worker on job: {}", job);
            job.setResultFuture(null);
            return;
        }

        final ListenableFuture<T> future;
        try {
            future = mainWorker.call();
        } catch (Exception e) {
            LOG.error("Direct Exception (not failed Future) when executing job, won't even retry: {}", job, e);
            job.setResultFuture(null);
            return;
        }

        Futures.addCallback(future, new FutureCallback<T>() {
            @Override
            public void onSuccess(final T result) {
                LOG.trace("Job completed successfully: {}", job.getKey());
                job.setResultFuture(result);
            }

            @Override
            public void onFailure(final Throwable cause) {
                LOG.error("Job {} failed", job.getKey(), cause);
            }
        }, MoreExecutors.directExecutor());

        LOG.trace("Finished running job: {}", job);
    }
}
