/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opendaylight.util.ErrorHandler;
import org.opendaylight.util.LoggerErrorHandler;
import org.slf4j.Logger;

/**
 * {@link org.opendaylight.util.LoggerErrorHandler} tests
 * 
 * @author Fabiel Zuniga
 */
public class LoggerErrorHandlerTest {

    @Test
    public void testErrorOccurred() {
        Object source = new Object();
        Exception errorMock = EasyMock.createMock(Exception.class);
        Logger loggerMock = EasyMock.createMock(Logger.class);

        loggerMock.error(EasyMock.anyObject(String.class), EasyMock.same(errorMock));

        EasyMock.replay(loggerMock);

        ErrorHandler<Object, Exception> errorHandler = new LoggerErrorHandler<Object>(loggerMock);
        errorHandler.errorOccurred(source, errorMock);

        EasyMock.verify(loggerMock);
    }
}
