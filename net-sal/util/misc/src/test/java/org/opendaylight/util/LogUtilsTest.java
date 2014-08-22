/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JDK logging test utilities.
 *
 * @author Frank Wood
 */
public class LogUtilsTest {

    /**
     * Verify that the logging state, loggers and handlers are working correctly.
     */
    @Test
    public void loggingState() {
        String state = LogUtils.getJdkLogState();

        assertTrue("Logger not found []", state.contains("Logger name:  []"));
        assertTrue("Logger not found [global]", state.contains("Logger name:  [global]"));

        Logger log = LoggerFactory.getLogger("test");

        state = LogUtils.getJdkLogState();

        assertTrue("Logger not found [test]", state.contains("Logger name:  [test]"));

        log.debug("Should not be logged #1.");

        System.setProperty(LogUtils.JDK_LOG_LEVEL_PROP_KEY, Level.FINEST.toString());
        LogUtils.configureJdkLoggingFromProperty();

        log.debug("Verify logging for tests #1 [{}].", "backed by JDK");

        LogUtils.configureJdkLogging(Level.INFO);

        log.debug("Should not be logged #2.");
    }

    /**
     * Verify that the logging state, loggers and handlers are working correctly.
     */
    @Test
    public void logger() {
        Logger log = Log.BOOTSTRAP.getLogger();
        assertEquals(AM_NEQ, "hp.bootstrap", log.getName());
    }
}
