/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.PORT_STATUS;
import static org.opendaylight.of.lib.msg.PortConfig.*;
import static org.opendaylight.of.lib.msg.PortFeature.*;
import static org.opendaylight.of.lib.msg.PortState.LINK_DOWN;
import static org.opendaylight.of.lib.msg.PortState.STP_LISTEN;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the OfmPortStatus message.
 *
 * @author Radhika Hegde
 * @author Simon Hunt
 */
public class OfmPortStatusTest extends OfmTest {

    // test files
    private static final String TF_PSTATUS_10 = "v10/portStatus";
    private static final String TF_PSTATUS_13 = "v13/portStatus";
    private static final String TF_PORT_V10 = "struct/portV0";
    private static final String TF_PORT_V13 = "struct/portV123";

    // expected values for the port from msg/struct/portV0 file
    private static final BigPortNumber EXP_0_PORTNUM = bpn(1);
    private static final MacAddress EXP_0_HW = mac("114477:010101");
    private static final String EXP_0_NAME = "One";
    private static final PortConfig[] EXP_0_CONFIG = {NO_RECV, NO_FLOOD, NO_FWD};
    private static final PortState[] EXP_0_STATE = {LINK_DOWN, STP_LISTEN};
    private static final PortFeature[] EXP_0_CURR = {RATE_1GB_FD, FIBER, AUTONEG};
    private static final PortFeature[] EXP_0_ADV = {RATE_1GB_FD, FIBER};
    private static final PortFeature[] EXP_0_SUPP = {RATE_1GB_FD, FIBER, AUTONEG};
    private static final PortFeature[] EXP_0_PEER = {RATE_100MB_FD};

    // expected values for the port from msg/struct/portV123.hex
    private static final BigPortNumber EXP_13_PORTNUM = bpn(258);
    private static final MacAddress EXP_13_HW = mac("114477:112233");
    private static final String EXP_13_NAME = "Two";
    private static final PortConfig[] EXP_13_CONFIG = {NO_RECV, NO_FWD, NO_PACKET_IN};
    private static final PortState[] EXP_13_STATE = {LINK_DOWN};
    private static final PortFeature[] EXP_13_CURR = {RATE_1GB_FD, FIBER, AUTONEG};
    private static final PortFeature[] EXP_13_ADV = {RATE_1GB_FD, FIBER};
    private static final PortFeature[] EXP_13_SUPP = {RATE_1GB_FD, FIBER, AUTONEG};
    private static final long EXP_13_CURR_SPEED = 1100000;
    private static final long EXP_13_MAX_SPEED = 3000000;

    // default value for attributes not applicable to any protocol version
    private static final long NA = 0;

    private MutableMessage mm;

    // Exception related messages
    private static final String E_PORT_ENC = "Failed to encode port.";
    private static final String E_PORT_PARSE = "Failed to parse port.";


    // ========================================================= PARSING ====

    private void verifyPortStatus13(OfmPortStatus msg, PortReason reason) {
        assertEquals(AM_NEQ, reason, msg.getReason());
        Port port = msg.getPort();
        assertEquals(AM_NEQ, EXP_13_PORTNUM, port.getPortNumber());
        assertEquals(AM_NEQ, EXP_13_HW, port.getHwAddress());
        assertEquals(AM_NEQ, EXP_13_NAME, port.getName());
        verifyFlags(port.getConfig(), EXP_13_CONFIG);
        verifyFlags(port.getState(), EXP_13_STATE);
        verifyFlags(port.getCurrent(), EXP_13_CURR);
        verifyFlags(port.getAdvertised(), EXP_13_ADV);
        verifyFlags(port.getSupported(), EXP_13_SUPP);
        assertNull("not supported peer", port.getPeer());
        assertEquals(AM_NEQ, EXP_13_CURR_SPEED, port.getCurrentSpeed());
        assertEquals(AM_NEQ, EXP_13_MAX_SPEED, port.getMaxSpeed());
    }

    private void verifyPortStatus10(OfmPortStatus msg, PortReason reason) {
        assertEquals(AM_NEQ, reason, msg.getReason());
        Port port = msg.getPort();
        assertEquals(AM_NEQ, EXP_0_PORTNUM, port.getPortNumber());
        assertEquals(AM_NEQ, EXP_0_HW, port.getHwAddress());
        assertEquals(AM_NEQ, EXP_0_NAME, port.getName());
        verifyFlags(port.getConfig(), EXP_0_CONFIG);
        verifyFlags(port.getState(), EXP_0_STATE);
        verifyFlags(port.getCurrent(), EXP_0_CURR);
        verifyFlags(port.getAdvertised(), EXP_0_ADV);
        verifyFlags(port.getSupported(), EXP_0_SUPP);
        verifyFlags(port.getPeer(), EXP_0_PEER);
        // not available in v1.0
        assertEquals(AM_NEQ, NA, port.getCurrentSpeed());
        assertEquals(AM_NEQ, NA, port.getMaxSpeed());
    }


    @Test
    public void portStatus13() {
        print(EOL + "portStatus13()");
        OfmPortStatus msg = (OfmPortStatus)
                verifyMsgHeader(TF_PSTATUS_13, V_1_3, PORT_STATUS, 80);
        verifyPortStatus13(msg, PortReason.ADD);
    }

    @Test
    public void portStatus10() {
        print(EOL + "portStatus10()");
        OfmPortStatus msg = (OfmPortStatus)
                verifyMsgHeader(TF_PSTATUS_10, V_1_0, PORT_STATUS, 64);
        verifyPortStatus10(msg, PortReason.ADD);
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodePortStatus13() {
        print(EOL + "encodePortStatus13()");
        mm = MessageFactory.create(V_1_3, PORT_STATUS);
        mm.clearXid();
        // assembling pieces
        try {
            verifyMutableHeader(mm, V_1_3, PORT_STATUS, 0);
            OfmMutablePortStatus pstat = (OfmMutablePortStatus) mm;
            // creating port from test file
            // since Port structure is tested already elsewhere
            pstat.port(createPort13()).reason(PortReason.ADD);
            encodeAndVerifyMessage(pstat.toImmutable(), TF_PSTATUS_13);
        } catch (IncompleteStructureException e) {
            print(e);
            fail(E_PORT_ENC);
        } catch (Exception e) {
            print(e);
            fail(AM_UNEX);
        }
    }

    @Test
    public void encodePortStatus13WithReason()
            throws IncompleteStructureException {
        print(EOL + "encodePortStatus13WithReason()");
        mm = MessageFactory.create(V_1_3, PORT_STATUS, PortReason.ADD);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, PORT_STATUS, 0);
        OfmMutablePortStatus pstat = (OfmMutablePortStatus) mm;
        pstat.port(createPort13());
        encodeAndVerifyMessage(pstat.toImmutable(), TF_PSTATUS_13);
    }

    @Test
    public void encodePortStatus10() {
        print(EOL + "encodePortStatus10()");
        mm = MessageFactory.create(V_1_0, PORT_STATUS);
        mm.clearXid();
        // assemble the pieces
        try {
            verifyMutableHeader(mm, V_1_0, PORT_STATUS, 0);
            OfmMutablePortStatus pstat = (OfmMutablePortStatus) mm;
            // Instead of creating a Port object afresh, will read
            // from test file
            pstat.port(createPort10()).reason(PortReason.ADD);
            encodeAndVerifyMessage(pstat.toImmutable(), TF_PSTATUS_10);
        } catch (IncompleteStructureException e) {
            print(e);
            fail(E_PORT_ENC);
        } catch (Exception e) {
            print(e);
            fail(AM_UNEX);
        }
    }

    @Test
    public void encodePortStatus10WithReason()
            throws IncompleteStructureException {
        print(EOL + "encodePortStatus10WithReason()");
        mm = MessageFactory.create(V_1_0, PORT_STATUS, PortReason.ADD);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, PORT_STATUS, 0);
        OfmMutablePortStatus pstat = (OfmMutablePortStatus) mm;
        pstat.port(createPort10());
        encodeAndVerifyMessage(pstat.toImmutable(), TF_PSTATUS_10);
    }

    @Test
    public void createWithReason() {
        print(EOL + "createWithReason()");
        OfmMutablePortStatus m = (OfmMutablePortStatus)
                MessageFactory.create(V_1_3, PORT_STATUS, PortReason.DELETE);
        m.clearXid();
        verifyMutableHeader(m, V_1_3, PORT_STATUS, 0);
        assertEquals(AM_NEQ, PortReason.DELETE, m.getReason());
    }

    // ===========

    // Helper method to create a Port object; v1.0.
    private Port createPort10() {
        //TODO: may be this could be moved to a common place
        OfPacketReader pkt = getMsgPkt(TF_PORT_V10);
        // pkt.resetIndex();
        Port port = null;
        try {
            port = PortFactory.parsePort(pkt, V_1_0);
        } catch (MessageParseException e) {
            print(e);
            fail(E_PORT_PARSE);
        }
        print(port.toDebugString());
        return port;
    }

    // Helper method to create a Port object; v1.3.
    private Port createPort13() {
        OfPacketReader pkt = getMsgPkt(TF_PORT_V13);
        // pkt.resetIndex();
        Port port = null;
        try {
            port = PortFactory.parsePort(pkt, V_1_3);
        } catch (MessageParseException e) {
            print(e);
            fail(E_PORT_PARSE);
        }
        print(port.toDebugString());
        return port;
    }
}
