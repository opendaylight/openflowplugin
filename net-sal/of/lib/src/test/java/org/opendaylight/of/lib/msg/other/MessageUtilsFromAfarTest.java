/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg.other;

import org.junit.Test;
import org.opendaylight.of.lib.msg.MessageUtils;
import org.opendaylight.of.lib.msg.OfmMutablePacketOut;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_OUT;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;

/**
 * Unit test of MessageUtils methods, intentionally in a package other than
 * org.opendaylight.of.lib.msg.
 *
 * @author Simon Hunt
 */
public class MessageUtilsFromAfarTest {

    private static final byte[] ORIG_BYTES = { 0, 1, 2, 3 };

    @Test
    public void packetOutDataReference() {
        byte[] myData = { 0, 1, 2, 3 };

        OfmMutablePacketOut mpo = (OfmMutablePacketOut) create(V_1_3, PACKET_OUT);
        // setting the data makes a copy internally...
        mpo.data(myData);
        assertArrayEquals(AM_NEQ, ORIG_BYTES, mpo.getData());
        // which means that changing our original bytes should have no effect..
        myData[0] = 0x7f;
        assertArrayEquals(AM_NEQ, ORIG_BYTES, mpo.getData());

        // but, we can get a reference to the backing bytes...
        byte[] backing = MessageUtils.getPacketBytes(mpo);
        // currently the same as before
        assertArrayEquals(AM_NEQ, ORIG_BYTES, mpo.getData());

        // but now we could change them
        backing[0] = 0x7f;
        assertEquals(AM_NEQ, 0x7f, mpo.getData()[0]);
    }

}
