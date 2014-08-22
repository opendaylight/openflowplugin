/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.OfPacketWriter;
import org.opendaylight.util.ByteUtils;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.MPLS_LABEL;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Additional, miscellaneous unit tests of the FieldFactory.
 *
 * @author Simon Hunt
 */
public class FieldFactoryTestAdditional {

    private static final int B = 256;
    private static final int PKT_SIZE = 10;
    private static final byte FF = 0xff-B;
    private static final int MPLS_VALUE = 13;

    private static final byte[] MPLS_3_BYTE_ENCODING = {
            0x80-B, 0x00, 0x44, 0x03,      // BASIC, TYPE=MPLS (0x22<<1), LEN
            0x00, 0x00, 0x0d,              // Value encoded in 3 bytes
            0xff-B,                        // End of structure marker
            0x00, 0x00
    };

    private static final byte[] MPLS_4_BYTE_ENCODING = {
            0x80-B, 0x00, 0x44, 0x04,      // BASIC, TYPE=MPLS (0x22<<1), LEN
            0x00, 0x00, 0x00, 0x0d,        // Value encoded in 4 bytes
            0xff-B,                        // End of structure marker
            0x00
    };


    @Test
    public void mplsLabelEncoding() {
        print(EOL + "mplsLabelEncoding()");
        MatchField mf = createBasicField(V_1_3, MPLS_LABEL, MPLS_VALUE);
        OfPacketWriter pkt = new OfPacketWriter(PKT_SIZE);
        FieldFactory.encodeField(mf, pkt);
        pkt.writeByte(FF); // mark the end of the structure
        print(ByteUtils.toHexString(pkt.array()));
        assertArrayEquals(AM_NEQ, MPLS_3_BYTE_ENCODING, pkt.array());

        FieldFactory._MPLS_LABEL_ENCODE_IN_4_BYTES = true;
        mf = createBasicField(V_1_3, MPLS_LABEL, MPLS_VALUE);
        pkt = new OfPacketWriter(PKT_SIZE);
        FieldFactory.encodeField(mf, pkt);
        pkt.writeByte(FF); // mark the end of the structure
        print(ByteUtils.toHexString(pkt.array()));
        assertArrayEquals(AM_NEQ, MPLS_4_BYTE_ENCODING, pkt.array());
        FieldFactory._MPLS_LABEL_ENCODE_IN_4_BYTES = false; // be sure to reset
    }

    @Test
    public void mplsLabelDecoding() {
        print(EOL + "mplsLabelDecoding()");
        OfPacketReader pkt = new OfPacketReader(MPLS_3_BYTE_ENCODING);
        try {
            MatchField mf = FieldFactory.parseField(pkt, V_1_3);
            print(mf.toDebugString());
            assertEquals(AM_NEQ, 3, mf.header.length);
            assertEquals(AM_NEQ, MPLS_LABEL, mf.getFieldType());
            MfbMplsLabel lab = (MfbMplsLabel) mf;
            assertEquals(AM_NEQ, MPLS_VALUE, lab.getValue());
        } catch (MessageParseException e) {
            fail(AM_UNEX);
        }

        FieldFactory._MPLS_LABEL_ENCODE_IN_4_BYTES = true;
        pkt = new OfPacketReader(MPLS_4_BYTE_ENCODING);
        try {
            MatchField mf = FieldFactory.parseField(pkt, V_1_3);
            print(mf.toDebugString());
            assertEquals(AM_NEQ, 4, mf.header.length);
            assertEquals(AM_NEQ, MPLS_LABEL, mf.getFieldType());
            MfbMplsLabel lab = (MfbMplsLabel) mf;
            assertEquals(AM_NEQ, MPLS_VALUE, lab.getValue());
        } catch (MessageParseException e) {
            fail(AM_UNEX);
        }
        FieldFactory._MPLS_LABEL_ENCODE_IN_4_BYTES = false; // be sure to reset
    }

}
