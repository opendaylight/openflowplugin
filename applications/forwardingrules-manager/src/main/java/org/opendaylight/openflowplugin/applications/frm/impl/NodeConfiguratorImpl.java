package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.errorprone.annotations.Var;
import org.opendaylight.infrautils.utils.concurrent.LoggingUncaughtThreadDeathContextRunnable;
import org.opendaylight.openflowplugin.applications.frm.NodeConfigurator;
import org.opendaylight.yangtools.util.concurrent.ThreadFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NodeConfiguratorImpl implements NodeConfigurator {

        private static final Logger LOG = LoggerFactory.getLogger(NodeConfiguratorImpl.class);

        private final Map<String, JobQueue> jobQueueMap = new ConcurrentHashMap<>();
        private final ReentrantLock jobQueueMapLock = new ReentrantLock();
        private final Condition jobQueueMapCondition = jobQueueMapLock.newCondition();
        private final AtomicBoolean jobQueueHandlerThreadStarted = new AtomicBoolean(false);
        private final Thread jobQueueHandlerThread;
        private static final int FJP_MAX_CAP = 0x7fff; // max #workers - 1; copy/pasted from ForkJoinPool private
        private volatile boolean shutdown = false;


        private final ForkJoinPool fjPool = new ForkJoinPool(
                Math.min(FJP_MAX_CAP, Runtime.getRuntime().availableProcessors()),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                LoggingThreadUncaughtExceptionHandler.toLogger(LOG),
                false);

        public NodeConfiguratorImpl() {
                jobQueueHandlerThread = ThreadFactoryProvider.builder()
                        .namePrefix("NodeConfigurator-JobQueueHandler")
                        .logger(LOG)
                        .build().get()
                        .newThread(new JobQueueHandler());
        }
        private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5,
                ThreadFactoryProvider.builder().namePrefix("NodeConfigurator-ScheduledExecutor").logger(LOG).build().get());


        private void signalForNextJob() {
                if (jobQueueHandlerThreadStarted.compareAndSet(false, true)) {
                        jobQueueHandlerThread.start();
                }

                jobQueueMapLock.lock();
                try {
                        jobQueueMapCondition.signalAll();
                } finally {
                        jobQueueMapLock.unlock();
                }
        }

        @Override public <T> ListenableFuture<T> enqueueJob(String key, Callable<ListenableFuture<T>> mainWorker) {

                JobEntry jobEntry = new JobEntry(key, mainWorker);
                JobQueue jobQueue = jobQueueMap.computeIfAbsent(key, mapKey -> new JobQueue());
                jobQueue.addEntry(jobEntry);
                signalForNextJob();

                return jobEntry.getResultFuture();
        }

        @Override
        public void close() throws Exception {
                {
                        LOG.info("JobCoordinator shutting down... (tasks still running may be stopped/cancelled/interrupted)");

                        jobQueueMapLock.lock();
                        try {
                                shutdown = true;
                                jobQueueMapCondition.signalAll();
                        } finally {
                                jobQueueMapLock.unlock();
                        }

                        fjPool.shutdownNow();
                        scheduledExecutorService.shutdownNow();

                        try {
                                jobQueueHandlerThread.join(10000);
                        } catch (InterruptedException e) {
                                // Shouldn't get interrupted - either way we don't care.
                        }

                        LOG.info("JobCoordinator now closed for business.");
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
                private static final int LONG_JOBS_THRESHOLD_MS = 1000;
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

                        /**
                         * This method is used to handle failure callbacks. If more retry
                         * needed, the retrycount is decremented and mainworker is executed
                         * again. After retries completed, rollbackworker is executed. If
                         * rollbackworker fails, this is a double-fault. Double fault is logged
                         * and ignored.
                         */
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
                signalForNextJob();
        }

        private boolean executeTask(Runnable task) {
                try {
                        fjPool.execute(task);
                        return true;
                } catch (RejectedExecutionException e) {
                        if (!fjPool.isShutdown()) {
                                LOG.error("ForkJoinPool task rejected", e);
                        }

                        return false;
                }
        }
}
