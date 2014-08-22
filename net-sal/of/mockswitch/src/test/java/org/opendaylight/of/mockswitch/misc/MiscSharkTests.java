/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch.misc;

import org.junit.Test;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.ByteUtils;

import static org.opendaylight.util.junit.TestTools.print;

/**
 * Miscellaneous tests for analysing wireshark hex data of openflow
 * conversations.
 *
 * @author Simon Hunt
 */
public class MiscSharkTests {
    private static final String DIR_ROOT = "org/opendaylight/of/mockswitch/misc/";
    private static final String FILE_EXT = ".ofshark";

    private OfPacketReader pktRdr(String hex) {
        return new OfPacketReader(ByteUtils.parseHex(hex));
    }

    private String path(String name) {
        return DIR_ROOT + name + FILE_EXT;
    }

    @Test
    public void bug139951() {
        SharkReader sr = new SharkReader(path("bug139951"));
        print(sr.toDebugString());
    }

    private static final String FRAME_14 =
            "0413002000090b5a000b0000000000000000007e000000030000000b02080000";

    @Test
    public void bug139951Frame14() throws MessageParseException {
        try {
            OpenflowMessage m = MessageFactory.parseMessage(pktRdr(FRAME_14));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            // Yeah, we know this one fails.
            print("EX> {}", e);
        }
    }

    private static final String ERR_FRAME_11 =
            "0401004c000001e200040006040e0068000001e20000000000001234000000" +
                    "000000ffff6400000000000bb8ffffffff19999999ffffffff0019" +
                    "000000010020800000040000001780000606";

    private static final String ERR_FRAME_37 =
            "0401004c000001e300030001040e0068000001e30000000000001234000000" +
                    "000000ffff6400000000000bb8ffffffff19999999ffffffff0019" +
                    "000000010020800000040000001780000606";

    @Test
    public void ofmErrorMessage() {
        SharkReader sr = new SharkReader(path("ofmErrorPcap"));
        print(sr.toDebugString());
    }

    @Test
    public void ofmErrorMessagesInDetail() throws MessageParseException {
        OpenflowMessage m = MessageFactory.parseMessage(pktRdr(ERR_FRAME_11));
        print(m.toDebugString());
        print("---");
        m = MessageFactory.parseMessage(pktRdr(ERR_FRAME_37));
        print(m.toDebugString());
        print("---");
    }

    @Test
    public void cr140089() {
        SharkReader sr = new SharkReader(path("cr140089"));
        print(sr.toDebugString());
    }

}
