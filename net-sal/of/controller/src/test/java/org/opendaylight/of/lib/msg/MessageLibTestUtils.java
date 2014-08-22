/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.slf4j.Logger;

/**
 * Provides package access to message library for unit testing purposes.
 *
 * @author Simon Hunt
 */
public class MessageLibTestUtils {
    private static final String E_ALREADY_SET =
            "a test logger has already been set";
    private static final String E_NONE_SET = "no test logger has been set";

    private Logger messageParserProductionLogger;

    /** Pushes an alternate logger on {@link MessageParser}, presumably one
     * which is instrumented for testing. The original logger is preserved.
     *
     * @param logger the test logger
     */
    protected void setMessageParserLogger(Logger logger) {
        if (messageParserProductionLogger != null)
            throw new IllegalStateException(E_ALREADY_SET);
        messageParserProductionLogger = MessageParser.log;
        MessageParser.log = logger;
    }

    /** Restores the production logger on the {@link MessageParser}. */
    protected void restoreMessageParserLogger() {
        if (messageParserProductionLogger == null)
            throw new IllegalStateException(E_NONE_SET);
        MessageParser.log = messageParserProductionLogger;
        messageParserProductionLogger = null;
    }
}
