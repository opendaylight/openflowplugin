/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.nodeconfigurator;

import com.google.common.base.MoreObjects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A queue which holds job entries and the current running job.
 */
public class JobQueue {

    private final Queue<JobEntry> jobQueue = new ConcurrentLinkedQueue<>();
    private volatile @Nullable JobEntry executingEntry;

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
