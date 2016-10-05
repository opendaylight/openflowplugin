/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.jobcoordinator.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.openflowplugin.applications.jobcoordinator.api.IJobCoordinator;
import org.opendaylight.openflowplugin.applications.jobcoordinator.api.RollbackCallable;
import org.opendaylight.openflowplugin.applications.jobcoordinator.api.SuccessCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class JobCoordinator implements IJobCoordinator, BindingAwareProvider, AutoCloseable{
    private static final Logger LOG = LoggerFactory.getLogger(JobCoordinator.class);

    private static final int THREADPOOL_SIZE = Runtime.getRuntime().availableProcessors();

    private ForkJoinPool fjPool;
    private Map<Integer,Map<String, JobQueue>> jobQueueMap = new ConcurrentHashMap<>();

    public JobCoordinator() {
        fjPool = new ForkJoinPool();

        for (int i = 0; i < THREADPOOL_SIZE; i++) {
            Map<String, JobQueue> jobEntriesMap = new ConcurrentHashMap<String, JobQueue>();
            jobQueueMap.put(i, jobEntriesMap);
        }

        new Thread(new JobQueueHandler()).start();
    }

    @Override
    public void onSessionInitiated(BindingAwareBroker.ProviderContext providerContext) {
        LOG.info("Job Coordinator Initiated");
    }

    @Override
    public void enqueueJob (String key,
                           Callable<List<ListenableFuture<? extends Object>>> mainWorker,
                            SuccessCallable  successWorker,
                           RollbackCallable rollbackWorker,
                           int maxRetries) {
        JobEntry jobEntry = new JobEntry(key, mainWorker, successWorker, rollbackWorker, maxRetries);
        Integer hashKey = getHashKey(key);
        LOG.debug("Obtained Hashkey: {}, for jobkey: {}", hashKey, key);

        Map<String, JobQueue> jobEntriesMap = jobQueueMap.get(hashKey);
        synchronized (jobEntriesMap) {
            JobQueue jobQueue = jobEntriesMap.get(key);
            if (jobQueue == null) {
                jobQueue = new JobQueue();
            }
            jobQueue.addEntry(jobEntry);
            jobEntriesMap.put(key, jobQueue);
        }

        jobQueueMap.put(hashKey, jobEntriesMap); // Is this really needed ?
    }

    /**
     * clearJob is used to cleanup the submitted job from the jobqueue.
     **/
    private void clearJob(JobEntry jobEntry) {
        Map<String, JobQueue> jobEntriesMap = jobQueueMap.get(getHashKey(jobEntry.getKey()));
        synchronized (jobEntriesMap) {
            JobQueue jobQueue = jobEntriesMap.get(jobEntry.getKey());
            jobQueue.setExecutingEntry(null);
            if (jobQueue.getWaitingEntries().isEmpty()) {
                jobEntriesMap.remove(jobEntry.getKey());
            }
        }
    }

    /**
     *
     * @param key
     * @return generated hashkey
     *
     * Used to generate the hashkey in to the jobQueueMap.
     */
    private Integer getHashKey(String key) {
        int code = key.hashCode();
        return (code % THREADPOOL_SIZE + THREADPOOL_SIZE) % THREADPOOL_SIZE;
    }

    @Override
    public void close() throws Exception {
        LOG.info("Job Coordinator Shutting Down.");
    }

    /**
     * JobCallback class is used as a future callback for
     * main and rollback workers to handle success and failure.
     */
    private class JobCallback implements FutureCallback<List<? extends Object>> {
        private JobEntry jobEntry;

        public JobCallback(JobEntry jobEntry) {
            this.jobEntry = jobEntry;
        }

        /**
         * @param results
         * This implies that all the future instances have returned success. -- TODO: Confirm this
         */
        @Override
        public void onSuccess(List<? extends Object> results) {
            SuccessCallable successCallable = jobEntry.getSuccessWorker();
            if (successCallable != null) {
                successCallable.setResults(results);
                SuccessTask successTask = new SuccessTask(jobEntry);
                fjPool.execute(successTask);
                return;
            }
            clearJob(jobEntry);
        }

        /**
         *
         * @param throwable
         * This method is used to handle failure callbacks.
         * If more retry needed, the retrycount is decremented and mainworker is executed again.
         * After retries completed, rollbackworker is executed.
         * If rollbackworker fails, this is a double-fault. Double fault is logged and ignored.
         */

        @Override
        public void onFailure(Throwable throwable) {
            LOG.warn("Job: {} failed with exception: {}", jobEntry, throwable.getStackTrace());
            if (jobEntry.getMainWorker() == null) {
                LOG.error("Job: {} failed with Double-Fault. Bailing Out.", jobEntry);
                clearJob(jobEntry);
                return;
            }

            if (jobEntry.decrementRetryCountAndGet() > 0) {
                MainTask worker = new MainTask(jobEntry);
                fjPool.execute(worker);
                return;
            }

            if (jobEntry.getRollbackWorker() != null) {
                jobEntry.setMainWorker(null);
                RollbackTask rollbackTask = new RollbackTask(jobEntry);
                fjPool.execute(rollbackTask);
                return;
            }

            clearJob(jobEntry);
        }
    }

    /**
     * SuccessTask is used to execute the SuccessCallable provided by the application
     * during success condition.
     */

    private class SuccessTask implements Runnable {
        private JobEntry jobEntry;

        public SuccessTask(JobEntry jobEntry) {
            this.jobEntry = jobEntry;
        }

        @Override
        public void run() {
            SuccessCallable callable = jobEntry.getSuccessWorker();
            List<ListenableFuture<? extends Object>> futures = null;

            try {
                futures = callable.call();
            } catch (Exception e){
                LOG.error("SuccessTask: Exception when executing jobEntry: {}, exception: {}", jobEntry, e.getStackTrace());
            }

            if (futures == null || futures.isEmpty()) {
                clearJob(jobEntry);
                return;
            }

            ListenableFuture<List<Object>> listenableFuture = Futures.allAsList(futures);
            Futures.addCallback(listenableFuture, new JobCallback(jobEntry));
            jobEntry.setFutures(futures);
        }
    }

    /**
     * RollbackTask is used to execute the RollbackCallable provided by the application
     * in the eventuality of a failure.
     */

    private class RollbackTask implements Runnable {
        private JobEntry jobEntry;

        public RollbackTask(JobEntry jobEntry) {
            this.jobEntry = jobEntry;
        }

        @Override
        public void run() {
            RollbackCallable callable = jobEntry.getRollbackWorker();
            callable.setFutures(jobEntry.getFutures());
            List<ListenableFuture<? extends Object>> futures = null;

            try {
                futures = callable.call();
            } catch (Exception e){
                LOG.error("RollbackTask: Exception when executing jobEntry: {}, exception: {}", jobEntry, e.getStackTrace());
            }

            if (futures == null || futures.isEmpty()) {
                clearJob(jobEntry);
                return;
            }

            ListenableFuture<List<Object>> listenableFuture = Futures.allAsList(futures);
            Futures.addCallback(listenableFuture, new JobCallback(jobEntry));
            jobEntry.setFutures(futures);
        }
    }

    /**
     * MainTask is used to execute the MainWorker callable.
     */

    private class MainTask implements Runnable {
        private JobEntry jobEntry;

        public MainTask(JobEntry jobEntry) {
            this.jobEntry = jobEntry;
        }

        @Override
        public void run() {
            List<ListenableFuture<? extends Object>> futures = null;
            try {
                futures = jobEntry.getMainWorker().call();
            } catch (Exception e){
                LOG.error("Exception when executing jobEntry: {}, exception: {}", jobEntry, e.getStackTrace());
            }

            if (futures == null || futures.isEmpty()) {
                clearJob(jobEntry);
                return;
            }

            ListenableFuture<List<Object>> listenableFuture = Futures.allAsList(futures);
            Futures.addCallback(listenableFuture, new JobCallback(jobEntry));
            jobEntry.setFutures(futures);
        }
    }

    private class JobQueueHandler implements Runnable {
        @Override
        public void run() {
            LOG.debug("Starting JobQueue Handler Thread.");
            while (true) {
                try {
                    boolean jobAddedToPool = false;
                    for (int i = 0; i < THREADPOOL_SIZE; i++) {
                        Map<String, JobQueue> jobEntriesMap = jobQueueMap.get(i);
                        if (jobEntriesMap.isEmpty()) {
                            continue;
                        }

                        synchronized (jobEntriesMap) {
                            Iterator it = jobEntriesMap.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry<String, JobQueue> entry = (Map.Entry)it.next();
                                if (entry.getValue().getExecutingEntry() != null) {
                                    continue;
                                }
                                JobEntry jobEntry = entry.getValue().getWaitingEntries().poll();
                                if (jobEntry != null) {
                                    entry.getValue().setExecutingEntry(jobEntry);
                                    MainTask worker = new MainTask(jobEntry);
                                    fjPool.execute(worker);
                                    jobAddedToPool = true;
                                } else {
                                    it.remove();
                                }
                            }
                        }
                    }

                    if (!jobAddedToPool) {
                        TimeUnit.SECONDS.sleep(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
