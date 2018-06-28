/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.nodeconfigurator;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A queue which holds job entries and the current running job.
 */
public class JobQueue {
    private static final String NODE_EXECUTOR_PREFIX = "nc-exe-";
    private static final Logger LOG = LoggerFactory.getLogger(JobQueue.class);

    private final Queue<JobEntry> jobQueue = new ConcurrentLinkedQueue<>();
    private volatile @Nullable JobEntry executingEntry;
    private ExecutorService executorService;
    private ListeningExecutorService syncThreadPool;

    public JobQueue(String key) {
        executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat(NODE_EXECUTOR_PREFIX + key)
                .setDaemon(true)
                .setUncaughtExceptionHandler((thread, ex) -> LOG.error("Uncaught exception {}", thread, ex))
                .build());
        syncThreadPool = MoreExecutors.listeningDecorator(executorService);
    }

    public ListeningExecutorService getSyncThreadPool() {
        return syncThreadPool;
    }


    public void addEntry(JobEntry entry) {
        jobQueue.add(entry);
    }

    public boolean isEmpty() {
        return jobQueue.isEmpty();
    }

    public @Nullable JobEntry poll() {
        return jobQueue.poll();
    }

    public @Nullable JobEntry getExecutingEntry() {
        return executingEntry;
    }

    public void setExecutingEntry(@Nullable JobEntry executingEntry) {
        this.executingEntry = executingEntry;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("executing", executingEntry).add("queue", jobQueue).toString();
    }
}
