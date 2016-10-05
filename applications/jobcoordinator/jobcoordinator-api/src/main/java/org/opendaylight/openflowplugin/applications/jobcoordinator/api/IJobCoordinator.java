/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.jobcoordinator.api;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Callable;

public interface IJobCoordinator {

    /**
     * @param key Jobs for the same key will be executed sequentially.
     * @param mainWorker The worker that will do the actual job.
     * @param successWorker Will be called if the main worker succeeds.
     * @param rollbackWorker Will be called if the main worker fails.
     * @param maxRetries Max number of retries.
     *
     * This is used by the external applications to enqueue a Job with an appropriate key.
     * A JobEntry is created and queued appropriately.
     */
    public void enqueueJob(String key,
                           Callable<List<ListenableFuture<? extends Object>>> mainWorker,
                           SuccessCallable successWorker,
                           RollbackCallable rollbackWorker,
                           int maxRetries);
}