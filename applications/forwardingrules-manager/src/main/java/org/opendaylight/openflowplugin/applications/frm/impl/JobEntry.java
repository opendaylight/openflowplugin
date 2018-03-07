/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static java.util.Collections.emptyList;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import javax.annotation.Nullable;

/**
 * JobEntry is the entity built per job submitted by the application and
 * enqueued to the book-keeping data structure.
 */
class JobEntry {

        private final String key;
        private volatile @Nullable Callable<Future<? extends RpcResult<?>>> mainWorker;
        private volatile Future<? extends RpcResult<?>> resultFuture;
        private volatile int retryCount;
        private static final AtomicIntegerFieldUpdater<JobEntry> RETRY_COUNT_FIELD_UPDATER =
                AtomicIntegerFieldUpdater.newUpdater(JobEntry.class, "retryCount");
        private volatile @Nullable List<ListenableFuture<Void>> futures;

        JobEntry(String key, Callable<Future<? extends RpcResult<?>>> mainWorker, Future<? extends RpcResult<?>> resultFuture) {
                this.key = key;
                this.mainWorker = mainWorker;
                this.resultFuture = resultFuture;
        }

        /**
         * Get the key provided by the application that segregates the callables
         * that can be run parallely. NOTE: Currently, this is a string. Can be
         * converted to Object where Object implementation should provide the
         * hashcode and equals methods.
         */
        public String getKey() {
                return key;
        }

        public @Nullable Callable<Future<? extends RpcResult<?>>> getMainWorker() {
                return mainWorker;
        }

        public void setMainWorker(@Nullable Callable<Future<? extends RpcResult<?>>> mainWorker) {
                this.mainWorker = mainWorker;
        }

        public List<ListenableFuture<Void>> getFutures() {
                List<ListenableFuture<Void>> nullableFutures = futures;
                if (nullableFutures != null) {
                        return nullableFutures;
                } else {
                        return emptyList();
                }
        }

        public void setFutures(List<ListenableFuture<Void>> futures) {
                this.futures = futures;
        }

        public Future<? extends RpcResult<?>> getResultFuture() {
                return resultFuture;
        }

        public void setResultFuture(Future<? extends RpcResult<?>> resultFuture) {
                this.resultFuture = resultFuture;
        }
}
