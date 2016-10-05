/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.jobcoordinator.impl;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.applications.jobcoordinator.api.RollbackCallable;
import org.opendaylight.openflowplugin.applications.jobcoordinator.api.SuccessCallable;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JobEntry is the entity built per job submitted by the application and
 * enqueued to the book-keeping data structure.
 */
public class JobEntry {
    final private String key;
    private Callable<List<ListenableFuture<? extends Object>>> mainWorker;
    final private RollbackCallable rollbackWorker;
    final private SuccessCallable successWorker;
    private AtomicInteger retryCount;
    private List<ListenableFuture<?>> futures;

    public JobEntry(String key,
                    Callable<List<ListenableFuture<? extends Object>>> mainWorker,
                    SuccessCallable  successWorker,
                    RollbackCallable rollbackWorker,
                    int maxRetries) {
        this.key = key;
        this.mainWorker = mainWorker;
        this.successWorker = successWorker;
        this.rollbackWorker = rollbackWorker;
        retryCount = new AtomicInteger(maxRetries);
    }

    /**
     *
     * @return
     *
     * The key provided by the application that segregates the
     * callables that can be run parallely.
     * NOTE: Currently, this is a string. Can be converted to Object where
     * Object implementation should provide the hashcode and equals methods.
     */
    public String getKey() {
        return key;
    }

    public Callable<List<ListenableFuture<?>>> getMainWorker() {
        return mainWorker;
    }

    public void setMainWorker(Callable<List<ListenableFuture<? extends Object>>> mainWorker) {
        this.mainWorker = mainWorker;
    }

    public SuccessCallable getSuccessWorker() {
        return successWorker;
    }

    public RollbackCallable getRollbackWorker() {
        return rollbackWorker;
    }

    public int decrementRetryCountAndGet() {
        return retryCount.decrementAndGet();
    }

    public List<ListenableFuture<?>> getFutures() {
        return futures;
    }

    public void setFutures(List<ListenableFuture<? extends Object>> futures) {
        this.futures = futures;
    }

    @Override
    public String toString() {
        return "JobEntry{" +
                "key='" + key + '\'' +
                ", mainWorker=" + mainWorker +
                ", successWorker=" + successWorker +
                ", rollbackWorker=" + rollbackWorker +
                ", retryCount=" + retryCount +
                ", futures=" + futures +
                '}';
    }
}