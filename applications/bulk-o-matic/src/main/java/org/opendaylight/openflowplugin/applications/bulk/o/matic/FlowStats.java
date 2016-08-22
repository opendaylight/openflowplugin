/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import java.util.concurrent.atomic.LongAdder;

/**
 *
 * This is the implementation of the MBean which provides the successful and failed transaction count.
 * Couple of counters are been used to keep track of the onSuccess and onFailure acknowledgment of the transaction commit.
 * Created by Vijay Daniel on 8/16/2016.
 */
public class FlowStats implements FlowStatsMBean {

    private LongAdder successCount = new LongAdder();
    private LongAdder failureCount = new LongAdder();

    /**
     * To get the successful transaction count
     * @return - success count
     */
    @Override
    public long getSuccessCount() {
        return successCount.longValue();
    }

    /**
     * To get the failed transaction count
     * @return - failure count
     */
    @Override
    public long getFailureCount() {
        return failureCount.longValue();
    }

    /**
     * To reset the counter. For every rest invocation, the reset counter is invoked
     * so that the previous values will be flushed.
     */
    @Override
    public void resetCount() {
        successCount.reset();
        failureCount.reset();
    }

    /**
     * Method to increment the success count.
     */
    public void incrementSuccessCount() {
        successCount.increment();
        failureCount.decrement();
    }

    /**
    * Method to set the initial error count
    */
    public void setReverseCounter(long reverseValue) {
        failureCount.add(reverseValue);
    }

}
