/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Base class for unit testing exceptions.
 *
 * @author Simon Hunt
 */
public abstract class AbstractExceptionTest {

    private static final String MSG = "Some Message";
    private static final Throwable CAUSE = new Throwable("Some Cause");

    private Exception ex;

    protected abstract Exception createWithNoArgs();
    protected abstract Exception createWithMsg(String msg);
    protected abstract Exception createWithCause(Throwable cause);
    protected abstract Exception createWithMsgAndCause(String msg,
                                                       Throwable cause);

    @Test
    public void noArgs() {
        print(EOL + "noArgs()");
        ex = createWithNoArgs();
        print(ex);
        assertNull(AM_HUH, ex.getCause());
        assertNull(AM_HUH, ex.getMessage());
    }

    @Test
    public void msg() {
        print(EOL + "msg()");
        ex = createWithMsg(MSG);
        print(ex);
        assertEquals(AM_NEQ, MSG, ex.getMessage());
        assertNull(AM_HUH, ex.getCause());
    }

    @Test
    public void cause() {
        print(EOL + "cause()");
        ex = createWithCause(CAUSE);
        print(ex);
        assertEquals(AM_NEQ, CAUSE.toString(), ex.getMessage());
        assertEquals(AM_NEQ, CAUSE, ex.getCause());
    }

    @Test
    public void msgAndCause() {
        print(EOL + "msgAndCause()");
        ex = createWithMsgAndCause(MSG, CAUSE);
        print(ex);
        assertEquals(AM_NEQ, MSG, ex.getMessage());
        assertEquals(AM_NEQ, CAUSE, ex.getCause());
    }
}
