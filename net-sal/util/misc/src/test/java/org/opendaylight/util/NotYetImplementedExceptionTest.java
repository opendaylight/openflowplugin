/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * This class unit tests the NotYetImplementedException.
 *
 * @author Simon Hunt
 */
public class NotYetImplementedExceptionTest {

    private static final String MSG = "Some message";

    private NotYetImplementedException ex;

    @Test
    public void noArgs() {
        print(EOL + "noArgs()");
        ex = new NotYetImplementedException();
        print(ex);
        assertNull(AM_HUH, ex.getMessage());
        assertNull(AM_HUH, ex.getCause());
    }

    @Test
    public void message() {
        print(EOL + "message()");
        ex = new NotYetImplementedException(MSG);
        print(ex);
        assertEquals(AM_NEQ, MSG, ex.getMessage());
        assertNull(AM_HUH, ex.getCause());
    }
}
