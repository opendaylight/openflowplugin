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
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.annotation.Nullable;

/**
 * JobEntry is the entity built per job submitted by the application and
 * enqueued to the book-keeping data structure.
 */
class JobEntry<T> {

        private final String key;
        private volatile @Nullable Callable<ListenableFuture<T>> mainWorker;
        private volatile SettableFuture<T> resultFuture;

        JobEntry(String key, Callable<ListenableFuture<T>> mainWorker) {
                this.key = key;
                this.mainWorker = mainWorker;
                resultFuture = SettableFuture.create();
        }

        public String getKey() {
                return key;
        }

        @Nullable public Callable<ListenableFuture<T>> getMainWorker() {
                return mainWorker;
        }

        public void setMainWorker(@Nullable Callable<ListenableFuture<T>> mainWorker) {
                this.mainWorker = mainWorker;
        }

        public ListenableFuture<T> getResultFuture() {
                return resultFuture;
        }

        public void setResultFuture(T result) {
                this.resultFuture.set(result);
        }

}
