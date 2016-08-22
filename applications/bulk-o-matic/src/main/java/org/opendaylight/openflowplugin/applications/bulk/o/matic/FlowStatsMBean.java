/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic;

/**
 *
 * This is a MBean interface to get the success and failure count of the submitted transactions.
 * Created by Vijay Daniel on 8/16/2016.
 */
public interface FlowStatsMBean {

    /**
     * To get the successful transaction count
     * @return - success count
     */
    public long getSuccessCount();

    /**
     * To get the failed transaction count
     * @return - failure count
     */
    public long getFailureCount();

    /**
     * To reset the counter.
     */
    public void resetCount();

}
