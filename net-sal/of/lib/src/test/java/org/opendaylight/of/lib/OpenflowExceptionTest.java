/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Unit tests for OpenflowException.
 *
 * @author Simon Hunt
 */
public class OpenflowExceptionTest extends AbstractExceptionTest {

    @Override
    protected Exception createWithNoArgs() {
        return new OpenflowException();
    }

    @Override
    protected Exception createWithMsg(String msg) {
        return new OpenflowException(msg);
    }

    @Override
    protected Exception createWithCause(Throwable cause) {
        return new OpenflowException(cause);
    }

    @Override
    protected Exception createWithMsgAndCause(String msg, Throwable cause) {
        return new OpenflowException(msg, cause);
    }
}
