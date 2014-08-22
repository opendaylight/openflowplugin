/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.junit.Assert.assertEquals;

/**
 * Simple unit test for AbstractMessage.
 *
 * @author Simon Hunt
 */
public class AbstractMessageTest {
    private static class MyMessage extends AbstractMessage { }

    @Test
    public void basic() {
        Message m = new MyMessage();
        assertEquals(AM_NEQ, 0, m.length());
    }

    @Test
    public void returnLengthField() {
        Message m = new MyMessage();
        ((MyMessage) m).length = 5;
        assertEquals(AM_NEQ, 5, m.length());
    }

}
