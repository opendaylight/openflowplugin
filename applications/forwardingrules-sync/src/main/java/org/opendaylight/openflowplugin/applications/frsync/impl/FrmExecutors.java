/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Static Factory for creating ExecutorServices (because there is no dependency injection but
 * static getInstance).
 */
public final class FrmExecutors {
    public static PceExecutorsFactory instance() {
        return DEFAULT_EXECUTORS;
    }

    public interface PceExecutorsFactory {

        ListeningExecutorService newFixedThreadPool(int nThreads, ThreadFactory factory);
    }

    /**
     * This will be rewritten in JUnits using SynchronousExecutorService.
     */
    @VisibleForTesting // should not be private and final
    static PceExecutorsFactory DEFAULT_EXECUTORS = new PceExecutorsFactory() {

        public ListeningExecutorService newFixedThreadPool(int nThreads, ThreadFactory factory) {
            final ExecutorService executorService = Executors.newFixedThreadPool(nThreads, factory);
            return MoreExecutors.listeningDecorator(executorService);
        }
    };
}
