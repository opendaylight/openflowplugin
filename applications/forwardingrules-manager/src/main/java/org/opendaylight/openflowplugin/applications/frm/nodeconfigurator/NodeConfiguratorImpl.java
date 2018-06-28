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
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.errorprone.annotations.Var;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.opendaylight.infrautils.utils.concurrent.LoggingUncaughtThreadDeathContextRunnable;
import org.opendaylight.openflowplugin.applications.frm.NodeConfigurator;
import org.opendaylight.yangtools.util.concurrent.ThreadFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeConfiguratorImpl implements NodeConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(NodeConfiguratorImpl.class);
    private static final String NODE_EXECUTOR_PREFIX = "nc-exe-";

    private final Map<String, JobQueue> jobQueueMap = new ConcurrentHashMap<>();
    private final AtomicBoolean jobQueueHandlerThreadStarted = new AtomicBoolean(false);
    private final Thread jobQueueHandlerThread;
    private volatile boolean shutdown = false;
    private final ListeningExecutorService syncThreadPool;

    public NodeConfiguratorImpl() {
        jobQueueHandlerThread = ThreadFactoryProvider.builder()
                .namePrefix("nc-jobqueue")
                .logger(LOG)
                .build().get()
                .newThread(new JobQueueHandler());
        final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setNameFormat(NODE_EXECUTOR_PREFIX + "%d")
                .setDaemon(true)
                .setUncaughtExceptionHandler((thread, ex) -> LOG.error("Uncaught exception {}", thread, ex))
                .build());
        syncThreadPool = MoreExecutors.listeningDecorator(executorService);
    }

    private void signalForNextJob() {
        if (jobQueueHandlerThreadStarted.compareAndSet(false, true)) {
            jobQueueHandlerThread.start();
        }
    }

    @Override
    public <T> ListenableFuture<T> enqueueJob(String key, Callable<ListenableFuture<T>> mainWorker) {

        JobEntry jobEntry = new JobEntry(key, mainWorker);
        JobQueue jobQueue = jobQueueMap.computeIfAbsent(key, mapKey -> new JobQueue());
        jobQueue.addEntry(jobEntry);
        signalForNextJob();

        return jobEntry.getResultFuture();
    }

    @Override
    public void close() throws Exception {
        {
            LOG.info("NodeConfigurator shutting down... (tasks still running may be stopped/cancelled/interrupted)");
            syncThreadPool.shutdownNow();
            try {
                jobQueueHandlerThread.join(10000);
            } catch (InterruptedException e) {
                // Shouldn't get interrupted - either way we don't care.
            }

            LOG.info("NodeConfigurator now closed for business.");
        }
    }

    private class JobQueueHandler implements Runnable {
        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void run() {
            LOG.info("Starting JobQueue Handler Thread");
            while (true) {
                try {
                    for (Map.Entry<String, JobQueue> entry : jobQueueMap.entrySet()) {
                        if (shutdown) {
                            break;
                        }
                        JobQueue jobQueue = entry.getValue();
                        if (jobQueue.getExecutingEntry() != null) {
                            continue;
                        }
                        JobEntry jobEntry = jobQueue.poll();
                        if (jobEntry == null) {
                            // job queue is empty. so continue with next job queue entry
                            continue;
                        }
                        jobQueue.setExecutingEntry(jobEntry);
                        MainTask worker = new MainTask(jobEntry);
                        LOG.trace("Executing job with key: {}", jobEntry.getKey());
                        executeTask(worker) ;
                    }
                } catch (Exception e) {
                    LOG.error("Exception while executing the tasks", e);
                }
            }
        }

    }

    private class MainTask<T> extends LoggingUncaughtThreadDeathContextRunnable {
        private final JobEntry jobEntry;

        MainTask(JobEntry jobEntry) {
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
                clearJob(jobEntry);
                return;
            }
            clearJob(jobEntry);
            Futures.addCallback(future, new JobCallback(jobEntry), MoreExecutors.directExecutor());

        }

        private class JobCallback<T> implements FutureCallback<T> {
            private final JobEntry jobEntry;

            JobCallback(JobEntry jobEntry) {
                this.jobEntry = jobEntry;
            }

            /**
             * This implies that all the future instances have returned success. --
             * TODO: Confirm this
             */
            @Override
            public void onSuccess(T result) {
                LOG.trace("Job completed successfully: {}", jobEntry.getKey());
                jobEntry.setResultFuture(result);
                clearJob(jobEntry);
            }

            @Override
            public void onFailure(Throwable throwable) {
                clearJob(jobEntry);
            }
        }
    }

    private void clearJob(JobEntry jobEntry) {
        String jobKey = jobEntry.getKey();
        LOG.trace("About to clear jobKey: {}", jobKey);
        JobQueue jobQueue = jobQueueMap.get(jobKey);
        if (jobQueue != null) {
            jobQueue.setExecutingEntry(null);
        } else {
            LOG.error("clearJob: jobQueueMap did not contain the key for this entry: {}", jobEntry);
        }
    }

    private void executeTask(Runnable task) {
        try {
            syncThreadPool.submit(task);
        } catch (RejectedExecutionException e) {
            if (!syncThreadPool.isShutdown()) {
                LOG.error("syncThreadPool task rejected", e);
            }
        }
    }
}
