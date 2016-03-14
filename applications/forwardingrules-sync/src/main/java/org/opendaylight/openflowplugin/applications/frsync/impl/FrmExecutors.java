package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Static Factory for creating ExecutorServicess (because there is no dependency injection but
 * static getInstance).
 * 
 * @author joslezak
 *
 */
public final class FrmExecutors {
    public static PceExecursFactory instance() {
        return DEFAULT_EXECUTORS;
    }

    public interface PceExecursFactory {

        public ListeningExecutorService newSingleThreadExecutor();

        public ListeningExecutorService newFixedThreadPool(int nThreads);
    }

    /**
     * This will be rewritten in JUnits using SynchronousExecutorService
     */
    @VisibleForTesting // should not be private and final
    static PceExecursFactory DEFAULT_EXECUTORS = new PceExecursFactory() {
        public ListeningExecutorService newSingleThreadExecutor() {
            final ExecutorService executorService = Executors.newSingleThreadExecutor();
            return MoreExecutors.listeningDecorator(executorService);
        }

        public ListeningExecutorService newFixedThreadPool(int nThreads) {
            final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
            return MoreExecutors.listeningDecorator(executorService);
        }
    };
}
