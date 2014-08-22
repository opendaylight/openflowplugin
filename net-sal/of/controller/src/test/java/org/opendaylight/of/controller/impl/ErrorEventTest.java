/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.Test;
import org.opendaylight.of.controller.ErrorEvent;
import org.opendaylight.of.lib.IncompleteMessageException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for ErrorEvent.
 *
 * @author Simon Hunt
 */
public class ErrorEventTest {

    @Test
    public void basic() {
        print(EOL + "basic()");
        ErrorEvent err = new ErrorEvt(null, null, null);
        print(err);
        assertEquals(AM_NEQ, "", err.text());
        assertNull(AM_HUH, err.cause());
        assertNull(AM_HUH, err.context());
    }

    @Test
    public void withMessageCauseAndContext() {
        print(EOL + "withMessageAndCause()");
        Throwable t = new IncompleteMessageException("foo");
        Object o = new Object();
        ErrorEvent err = new ErrorEvt("Foo", t, o);
        print(err);
        assertEquals(AM_NEQ, "Foo", err.text());
        assertEquals(AM_NEQ, t, err.cause());
        assertEquals(AM_NEQ, o, err.context());
    }
}
