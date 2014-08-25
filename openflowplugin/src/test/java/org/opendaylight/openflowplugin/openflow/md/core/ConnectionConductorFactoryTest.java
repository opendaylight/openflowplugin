/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import org.junit.Test;

/**
 * Created by Martin Bobak mbobak@cisco.com on 8/25/14.
 */
public class ConnectionConductorFactoryTest extends ConnectionConductorImplTest{

    @Test
    /**
     * Test for ConnectionConductorFactory#createConductor
     */
    public void testCreateConductor() {
        ConnectionConductor connectionConductor = ConnectionConductorFactory.createConductor(adapter, queueProcessor);
    }
}
