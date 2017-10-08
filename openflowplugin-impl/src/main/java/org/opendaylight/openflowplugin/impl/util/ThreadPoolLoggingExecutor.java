/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * threadPoolExecutor implementation logging exceptions thrown by threads.
 */
public class ThreadPoolLoggingExecutor extends ThreadPoolExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolLoggingExecutor.class);

    /**
     * Logging executor.
     *
     * @param corePoolSize thread pool size
     * @param maximumPoolSize maximum pool size
     * @param keepAliveTime keep alive time
     * @param unit time unit
     * @param workQueue task queue
     * @param poolName thread name prefix
     */
    public ThreadPoolLoggingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                     BlockingQueue<Runnable> workQueue, final String poolName) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
              new ThreadFactoryBuilder().setNameFormat(poolName + "-%d").build());
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);
        // in case of executing pure Runnable
        if (throwable != null) {
            LOG.warn("thread in pool stopped with error", throwable);
        }
    }
}
