/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import static org.mockito.Mockito.when;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SwitchSessionKeyOF;

public class ErrorHandlerSimpleImplTest extends TestCase {

    ErrorHandler errorHandler = new ErrorHandlerSimpleImpl();

    @Mock
    SessionContext sessionContext;

    @Mock
    SwitchSessionKeyOF switchSessionKeyOF;

    @Before
    public void setup() {
        when(sessionContext.getSessionKey()).thenReturn(switchSessionKeyOF);
        when(switchSessionKeyOF.getId()).thenReturn(new byte[0]);
    }

    @Test
    public void testHandleException() throws Exception {
        ConnectionException connectionException = new ConnectionException("Exception for testing purpose only.");
        errorHandler.handleException(connectionException, sessionContext);

        Exception someException = new Exception("Exception for testing purpose only.");
        errorHandler.handleException(someException, sessionContext);

    }
}