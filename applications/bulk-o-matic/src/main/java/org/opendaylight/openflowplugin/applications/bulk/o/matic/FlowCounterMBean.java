/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

public interface FlowCounterMBean {

    default public long getFlowCount() {
        return BulkOMaticUtils.DEFAULT_FLOW_COUNT;
    }

    default public int getReadOpStatus() {
        return BulkOMaticUtils.DEFUALT_STATUS;
    }

    default public int getWriteOpStatus() {
        return BulkOMaticUtils.DEFUALT_STATUS;
    }

    default public long getTaskCompletionTime() {
        return BulkOMaticUtils.DEFAULT_COMPLETION_TIME;
    }

    default public String getUnits() {
        return BulkOMaticUtils.DEFAULT_UNITS;
    }

    default public long getTableCount() {
        return BulkOMaticUtils.DEFAULT_TABLE_COUNT;
    }
}

