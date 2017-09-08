/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import junit.framework.TestCase;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;

public class ErrorHandlerSimpleImplTest extends TestCase {

    ErrorHandler errorHandler = new ErrorHandlerSimpleImpl();

    @Test
    public void testHandleException() throws Exception {
        ConnectionException connectionException = new ConnectionException("Exception for testing purpose only.");
        errorHandler.handleException(connectionException);

        Exception someException = new Exception("Exception for testing purpose only.");
        errorHandler.handleException(someException);

    }
}