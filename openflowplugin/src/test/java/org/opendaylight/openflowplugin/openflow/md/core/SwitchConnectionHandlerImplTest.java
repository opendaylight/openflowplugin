/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Created by Martin Bobak mbobak@cisco.com on 8/29/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class SwitchConnectionHandlerImplTest {


    @Mock
    private ErrorHandler errorHandler;
    @Mock
    private MessageSpy<DataContainer> messageSpy;
    @Mock
    private ConnectionAdapter connectionAdapter;

    /**
     * Test method for SwitchConnectionHandlerImpl initialization.
     */
    @Test
    public void basicInitializationTest() {
        SwitchConnectionHandlerImpl switchConnectionHandler = new SwitchConnectionHandlerImpl();
        assertNotNull(switchConnectionHandler);
        switchConnectionHandler.setMessageSpy(messageSpy);
        switchConnectionHandler.setErrorHandler(errorHandler);
    }

}
