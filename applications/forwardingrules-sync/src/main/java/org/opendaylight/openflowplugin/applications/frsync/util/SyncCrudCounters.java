/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.util;

/**
 * One-shot (per sync) placeholder for counts of added/updated/removed flows/groups/meters.
 */
public class SyncCrudCounters {

    private final CrudCounts flowCrudCounts;
    private final CrudCounts groupCrudCounts;
    private final CrudCounts meterCrudCounts;
    private long startNano;

    public SyncCrudCounters() {
        flowCrudCounts = new CrudCounts();
        groupCrudCounts = new CrudCounts();
        meterCrudCounts = new CrudCounts();
    }

    public CrudCounts getFlowCrudCounts() {
        return flowCrudCounts;
    }

    public CrudCounts getGroupCrudCounts() {
        return groupCrudCounts;
    }

    public CrudCounts getMeterCrudCounts() {
        return meterCrudCounts;
    }


    public long getStartNano() {
        return startNano;
    }

    public void setStartNano(final long startNano) {
        this.startNano = startNano;
    }
}
