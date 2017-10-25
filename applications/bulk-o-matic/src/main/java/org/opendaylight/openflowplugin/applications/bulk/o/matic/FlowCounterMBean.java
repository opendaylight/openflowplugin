/*
 * Copyright (c) 2016, 2017 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

public interface FlowCounterMBean {

    default long getFlowCount() {
        return BulkOMaticUtils.DEFAULT_FLOW_COUNT;
    }

    default int getReadOpStatus() {
        return BulkOMaticUtils.DEFUALT_STATUS;
    }

    default int getWriteOpStatus() {
        return BulkOMaticUtils.DEFUALT_STATUS;
    }

    default long getTaskCompletionTime() {
        return BulkOMaticUtils.DEFAULT_COMPLETION_TIME;
    }

    default String getUnits() {
        return BulkOMaticUtils.DEFAULT_UNITS;
    }

    default long getTableCount() {
        return BulkOMaticUtils.DEFAULT_TABLE_COUNT;
    }
}

