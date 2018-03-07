package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.errorprone.annotations.Var;
import org.opendaylight.openflowplugin.applications.frm.NodeConfigurator;
import org.opendaylight.yangtools.util.concurrent.ThreadFactoryProvider;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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



        @Override
        public void enqueueJob(String key, Callable<Future<? extends RpcResult<?>>> mainWorker, Future<RpcResult<? extends RpcResult<?>>> resultFuture) {

                JobEntry jobEntry = new JobEntry(key, mainWorker, resultFuture);
                JobQueue jobQueue = jobQueueMap.computeIfAbsent(key, mapKey -> new JobQueue());
                jobQueue.addEntry(jobEntry);


                signalForNextJob();

        }

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

        private class JobQueueHandler implements Runnable {
                @Override
                @SuppressWarnings("checkstyle:illegalcatch")
                public void run() {
                        LOG.info("Starting JobQueue Handler Thread");
                        while (true) {
                                try {
                                        for (Map.Entry<String, JobQueue> entry : jobQueueMap.entrySet()) {
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
                                                jobEntry.setResultFuture(executeTask(worker)) ;
                                        }
                                } catch (Exception e) {
                                        LOG.error("Exception while executing the tasks", e);
                                }
                        }
                }

        }

        private class MainTask implements Callable <Future<RpcResult<? extends RpcResult<?>>>>{
                private static final int LONG_JOBS_THRESHOLD_MS = 1000;
                private final JobEntry jobEntry;

                MainTask(JobEntry jobEntry) {
                        this.jobEntry = jobEntry;
                }

                @SuppressWarnings("checkstyle:illegalcatch")
                public Future<RpcResult<? extends RpcResult<?>>> call() {
                        @Var List<ListenableFuture<Void>> futures = null;
                        Future<? extends RpcResult<?>> a;

                        LOG.trace("Running job with key: {}", jobEntry.getKey());

                        try {
                                Callable<Future<? extends RpcResult<?>>> mainWorker = jobEntry.getMainWorker();
                                if (mainWorker != null) {
                                        a = mainWorker.call();
                                } else {
                                        LOG.error("Unexpected no (null) main worker on job: {}", jobEntry);
                                }
                        } catch (Exception e) {
                                LOG.error("Direct Exception (not failed Future) when executing job, won't even retry: {}", jobEntry, e);
                        }

                        jobEntry.setFutures(futures);
                        ListenableFuture<List<Void>> listenableFuture = Futures.allAsList(futures);
                        return null;
                }
        }

        private ForkJoinTask executeTask(Callable task) {
                        return fjPool.submit(task);

        }
}
