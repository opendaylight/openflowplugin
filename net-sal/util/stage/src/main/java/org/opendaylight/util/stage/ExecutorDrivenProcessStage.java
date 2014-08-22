/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Base implementation of a {@link ProcessStageOutlet process stage outlet}
 * semantics.
 * <p>
 * This implementation employs an executor service to process items that are
 * fed into the outlet. Depending on the executor service, several items may
 * be processed in parallel. Consequently, the stage does not guarantee that
 * items will get processed in strict order.
 * 
 * @author Thomas Vachuska
 * 
 * @param <T> type of item accepted/taken by the stage
 * @param <P> type of item produced by the stage
 * @param <B> type of alternate item produced by the stage when branching
 */
public abstract class ExecutorDrivenProcessStage<T, P, B> extends AbstractProcessStage<T, P, B> {

    private static final int DEFAULT_CORE_POOL_SIZE = 2;
    private static final int DEFAULT_MAX_POOL_SIZE = 2;

    // Executor service to be used for processing items.
    private ExecutorService es = null;

    // Core and maximum number of threads the default executor should use.
    // These are ignored if the stage has been provided with an explicit
    // executor.
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

    /**
     * Sets the executor service to be used for processing accepted items. If
     * not explicitly set, the implementation will use
     * {@link ThreadPoolExecutor} by default. Any previously set executor
     * service will be forcefully shutdown, before the new one is applied to
     * this process stage.
     * <p>
     * This method can be called only while the stage is stopped.
     * 
     * @param executorService executor service for driving the process stage
     */
    public synchronized void setExecutorService(ExecutorService executorService) {
        if (!isStopped())
            throw new IllegalStateException("Cannot change executor service while started.");
            
        // If there is an executor service already, shut it down first.
        if (this.es != null)
            this.es.shutdownNow();
        
        this.es = executorService;
    }

    /**
     * Get the current executor service. If not explicitly set via
     * {@link #setExecutorService(ExecutorService)} this call will return null
     * until the stage has been started at which time a default
     * {@link ThreadPoolExecutor} executor service will be allocated.
     * 
     * @return executor service in use by the process stage; null if one has
     *         not been explicitly set via
     *         {@link #setExecutorService(ExecutorService)}
     */
    public synchronized ExecutorService getExecutorService() {
        return es;
    }

    /**
     * Gets the number of threads that the default executor service thread
     * pool is configured for.
     * 
     * @return number of core threads
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * Sets the number of threads in the default executor service thread pool.
     * 
     * @param threadCount core pool size, in number of threads
     */
    public void setCorePoolSize(int threadCount) {
        this.corePoolSize = threadCount;
    }

    
    /**
     * Gets the maximum number of threads that the default executor service
     * should use.
     * 
     * @return maximum number of threads
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Sets the maximum number of threads in the default executor service
     * thread pool.
     * 
     * @param threadCount maximum pool size, in number of threads
     */
    public void setMaxPoolSize(int threadCount) {
        this.maxPoolSize = threadCount;
    }

    /**
     * Creates a default {@link ThreadPoolExecutor} executor service, using
     * the {@code getCorePoolSize()}, {@code getMaxPoolSize()},
     * {@code getIdleTimeOut()} and using a newly allocated
     * {@code LinkedBlockingQueue}.
     * 
     * @return default executor service
     */
    protected ExecutorService createDefaultExecutor() {
        return new ThreadPoolExecutor(getCorePoolSize(), getMaxPoolSize(),
                                      getIdleTimeOut(), TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }


    /**
     * This method allows subclasses to implement the essence of processing a
     * single in-bound item of type T into an out-bound item of type P.
     * 
     * @param item in-bound item to be processed
     * @return out-bound processed item; may be null to signify there was no
     *         item produced
     */
    public abstract P processItem(T item);


    @Override
    public synchronized void start() {
        if (!isStopped())
            return;
        
        // If there is no executor service set, create a default one.
        if (es == null)
            es = createDefaultExecutor();
        
        super.start();
    }

    @Override
    public synchronized boolean accept(T item) {
        if (!super.accept(item))
            return false;
        es.submit(new ItemProcessor(item));
        return true;
    }

    /**
     * Auxiliary Runnable item wrapper, used to queue and process items.
     */
    private class ItemProcessor implements Runnable {

        // Item to be processed
        private T item;

        private ItemProcessor(T item) {
            this.item = item;
        }

        @Override
        public void run() {
            try {
                // Process the item and if the result is not null and there is
                // a downstream outlet, forward the result downstream.
                P result = processItem(item);
                if (result != null)
                    forward(result);
            } finally {
                // Tally the out-bound item and record the departure time.
                tally(-1);
                
            }
        }

    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation leaves the the backing executor service running.
     */
    @Override
    public synchronized void stop() {
        super.stop();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation shuts-down the backing executor service via
     * {@link ExecutorService#shutdownNow()} which means the stage will not
     * be executable again until a new executor is set.
     */
    @Override
    public synchronized void forceStop() {
        super.forceStop();
        if (es != null)
            es.shutdownNow();
    }

}
