/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * threadPoolExecutor implementation logging exceptions thrown by threads
 */
public class ThreadPoolLoggingExecutor extends ThreadPoolExecutor {
    
    private static Logger LOG = LoggerFactory.getLogger(ThreadPoolLoggingExecutor.class);

    /**
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param poolName thread name prefix
     */
    public ThreadPoolLoggingExecutor(int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, 
            final String poolName) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, 
                new SimpleNamingThreadFactory(poolName));
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        // in case of executing pure Runnable
        if (t != null) {
            LOG.warn("thread in pool stopped with error", t);
        }
    }
    
    static class SimpleNamingThreadFactory implements ThreadFactory {
        
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private String threadNamePrefix;

        /**
         * @param threadNamePrefix
         */
        public SimpleNamingThreadFactory(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, threadNamePrefix + "-" +threadNumber.getAndIncrement());
        }
        
    }
}
