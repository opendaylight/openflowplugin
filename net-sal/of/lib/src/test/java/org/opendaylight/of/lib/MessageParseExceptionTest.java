/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Unit tests for MessageParseException.
 *
 * @author Simon Hunt
 */
public class MessageParseExceptionTest extends AbstractExceptionTest {

    @Override
    protected Exception createWithNoArgs() {
        return new MessageParseException();
    }

    @Override
    protected Exception createWithMsg(String msg) {
        return new MessageParseException(msg);
    }

    @Override
    protected Exception createWithCause(Throwable cause) {
        return new MessageParseException(cause);
    }

    @Override
    protected Exception createWithMsgAndCause(String msg, Throwable cause) {
        return new MessageParseException(msg, cause);
    }
}
