/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.jobcoordinator.impl;

import java.util.concurrent.ConcurrentLinkedQueue;

public class JobQueue {
    private ConcurrentLinkedQueue<JobEntry> waitingEntries;
    private JobEntry executingEntry;

    public JobQueue() {
        waitingEntries = new ConcurrentLinkedQueue<JobEntry>();
    }

    public void addEntry(JobEntry entry) {
        waitingEntries.add(entry); // FIXME - Try/Catch.
    }

    public ConcurrentLinkedQueue<JobEntry> getWaitingEntries() {
        return waitingEntries;
    }

    public JobEntry getExecutingEntry() {
        return executingEntry;
    }

    public void setExecutingEntry(JobEntry executingEntry) {
        this.executingEntry = executingEntry;
    }
}
