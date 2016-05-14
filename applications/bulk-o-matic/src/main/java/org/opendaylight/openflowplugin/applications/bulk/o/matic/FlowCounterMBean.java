/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

public interface FlowCounterMBean {

    public long getFlowCount();

    public int getReadOpStatus();

    public int getWriteOpStatus();

    public long getTaskCompletionTime();

    public String getUnits();
}

