/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.nodeconfigurator;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Callable;
import org.eclipse.jdt.annotation.Nullable;

/**
 * JobEntry is the entity built per job submitted by the application and
 * enqueued to the book-keeping data structure.
 */
class JobEntry<T> {
    private final SettableFuture<T> resultFuture = SettableFuture.create();
    private @Nullable final Callable<ListenableFuture<T>> mainWorker;
    private final String key;

    JobEntry(String key, Callable<ListenableFuture<T>> mainWorker) {
        this.key = key;
        this.mainWorker = mainWorker;
    }

    String getKey() {
        return key;
    }

    @Nullable Callable<ListenableFuture<T>> getMainWorker() {
        return mainWorker;
    }

    ListenableFuture<T> getResultFuture() {
        return resultFuture;
    }

    void setResult(@Nullable T result) {
        resultFuture.set(result);
    }

    void setFailure(Throwable failure) {
        resultFuture.setException(failure);
    }
}
