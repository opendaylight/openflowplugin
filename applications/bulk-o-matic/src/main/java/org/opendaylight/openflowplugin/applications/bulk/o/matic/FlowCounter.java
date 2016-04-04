/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

public class FlowCounter implements FlowCounterMBean {
    private FlowCounterMBean reader;
    private FlowCounterMBean writer;

    public enum OperationStatus {INIT, SUCCESS, FAILURE, INPROGRESS}

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
        return 0;
    }

    @Override
    public int getReadOpStatus() {
        if(reader != null) {
            return reader.getReadOpStatus();
        }
        return OperationStatus.INIT.ordinal();
    }

    @Override
    public int getWriteOpStatus() {
        if(writer != null) {
            return writer.getWriteOpStatus();
        }
        return OperationStatus.INIT.ordinal();
    }
}
