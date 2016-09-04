/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.reference.app;

import java.util.Set;

/**
 * interface to expose counter using MBean for added flows, removed flows and failed flows via ReferenceApp.
 */
public interface ReferenceAppMXBean {

    /**
     * counter for added flows for each switch
     *
     * @return
     */
    Set<String> getFlowAddCounter();

    /**
     * counter for removed flows for each switch
     *
     * @return
     */
    Set<String> getFlowRemoveCounter();

    /**
     * counter for failed flows for each switch
     *
     * @return
     */
    Set<String> getFlowFailedCounter();

    /**
     * reset all the counter value to zero
     */
    void resetCounters();

}