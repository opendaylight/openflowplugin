/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

public class FlowCounter implements FlowCounterMBean {
    private FlowCounterMBean reader;
    private FlowCounterMBean writer;

    public enum OperationStatus {
        INIT (0),
        SUCCESS (2),
        FAILURE (-1),
        IN_PROGRESS (1);

        private final int status;

        OperationStatus(int status) {
            this.status = status;
        }

        public int status() {
            return this.status;
        }
    }

    public void setReader(FlowCounterMBean reader) {
        this.reader = reader;
    }

    public void setWriter(FlowCounterMBean writer) {
        this.writer = writer;
    }

    @Override
    public long getFlowCount() {
        if(reader != null) {
            return reader.getFlowCount();
        }
        return BulkOMaticUtils.DEFAULT_FLOW_COUNT;
    }

    @Override
    public int getReadOpStatus() {
        if(reader != null) {
            return reader.getReadOpStatus();
        }
        return OperationStatus.INIT.status();
    }

    @Override
    public int getWriteOpStatus() {
        if(writer != null) {
            return writer.getWriteOpStatus();
        }
        return OperationStatus.INIT.status();
    }

    @Override
    public long getTaskCompletionTime() {
        if(writer != null) {
            return writer.getTaskCompletionTime();
        }
        return BulkOMaticUtils.DEFAULT_COMPLETION_TIME;
    }

    @Override
    public String getUnits() {
        if (reader != null) {
            return reader.getUnits();
        } else if (writer != null) {
            return writer.getUnits();
        } else {
            return BulkOMaticUtils.DEFAULT_UNITS;
        }
    }

    @Override
    public long getTableCount() {
        if (writer != null) {
            return writer.getTableCount();
        }
        return BulkOMaticUtils.DEFAULT_TABLE_COUNT;
    }
}
