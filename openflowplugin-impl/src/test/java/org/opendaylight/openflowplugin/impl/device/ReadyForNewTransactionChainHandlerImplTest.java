/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;

/**
 * Test for {@link ReadyForNewTransactionChainHandlerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadyForNewTransactionChainHandlerImplTest {

    @Mock
    private DeviceManager deviceManager;
    @Mock
    private ConnectionContext connectionContext;

    private ReadyForNewTransactionChainHandlerImpl chainHandler;

    @Before
    public void setUp() throws Exception {
        chainHandler = new ReadyForNewTransactionChainHandlerImpl(deviceManager, connectionContext);
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verifyNoMoreInteractions(deviceManager, connectionContext);
    }

    @Test
    public void testOnReadyForNewTransactionChain() throws Exception {
        chainHandler.onReadyForNewTransactionChain();
        Mockito.verify(deviceManager).deviceConnected(connectionContext);
    }
}