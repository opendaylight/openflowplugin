/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.flowprogrammer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.List;
import java.util.concurrent.*;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is FlowProgrammerImpl class. It is instantiated from the FlowProgrammerImplModule class.
 * <p>
 * @author Yi Yang (yi.y.yang@intel.com)
 * @since 2015-09-15
 * @see org.opendaylight.openflowplugin.flowprogrammer.config.yang.impl.FlowProgrammerImplModule
 */

public class FlowProgrammerImpl implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(FlowProgrammerImpl.class);

    private static final long SHUTDOWN_TIME = 5;
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("FlowProgrammer-%d")
            .setDaemon(false)
            .build();

    public static final int EXECUTOR_THREAD_POOL_SIZE = 1;

    private final ExecutorService executor;
    protected DataBroker dataProvider;
    private static FlowProgrammerImpl flowProgrammerImplObj;

    public FlowProgrammerImpl() {
        executor = Executors.newFixedThreadPool(EXECUTOR_THREAD_POOL_SIZE, THREAD_FACTORY);
        if (executor == null) {
            LOG.error("Could you not create FlowProgrammerImpl Executors");
        }
        flowProgrammerImplObj = this;
        LOG.info("Openflowplugin FlowProgrammerImpl initialized");
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setDataProvider(DataBroker salDataProvider) {
        this.dataProvider = salDataProvider;
    }

    public DataBroker getDataProvider() {
        return this.dataProvider;
    }

    public static FlowProgrammerImpl getFlowProgrammerImplObj() {
        return FlowProgrammerImpl.flowProgrammerImplObj;
    }

    /**
     * Implemented from the AutoCloseable interface.
     */
    @Override
    public void close() throws ExecutionException, InterruptedException {
        // When we close this service we need to shutdown our executor!
        if (dataProvider != null) {
            // When we close this service we need to shutdown our executor!
            executor.shutdown();
            if (!executor.awaitTermination(SHUTDOWN_TIME, TimeUnit.SECONDS)) {
                LOG.error("Executor did not terminate in the specified time.");
                List<Runnable> droppedTasks = executor.shutdownNow();
                LOG.error("Executor was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed.");
            }
        }
    }
}
