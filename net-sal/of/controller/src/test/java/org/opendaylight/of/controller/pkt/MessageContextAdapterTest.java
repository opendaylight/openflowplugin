/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.junit.Assert.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MessageContextAdapter.
 *
 * @author Simon Hunt
 */
public class MessageContextAdapterTest {

    private static class Adapter extends MessageContextAdapter {
        private final String s;

        private Adapter(String s) {
            this.s = s;
        }

        @Override
        public String toDebugString() {
            return s;
        }

        @Override
        public ProtocolVersion getVersion() {
            return ProtocolVersion.V_1_0;
        }
    }

    private static final String SOME_STRING = "SoMeStRiNg!";

    @Test
    public void basic() {
        print(EOL + "basic()");
        MessageContext m = new Adapter(SOME_STRING);
        assertEquals(AM_NEQ, SOME_STRING, m.toDebugString());
        assertEquals(AM_NEQ, ProtocolVersion.V_1_0, m.getVersion());
        assertNull(AM_HUH, m.getPacketIn());
        assertFalse(AM_HUH, m.failedToSend());
    }
}
