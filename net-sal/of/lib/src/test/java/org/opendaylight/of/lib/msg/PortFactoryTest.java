/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.util.ByteArrayGenerator;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import java.util.*;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.PortConfig.*;
import static org.opendaylight.of.lib.msg.PortFeature.*;
import static org.opendaylight.of.lib.msg.PortState.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for PortFactory.
 *
 * @author Simon Hunt
 */
public class PortFactoryTest extends OfmTest {

    // test files
    private static final String TF_PORT_V10 = "struct/portV0";
    private static final String TF_PORT_V123 = "struct/portV123";

    private static final String E_PARSE_FAIL = "Failed to parse port.";

    private static final long NA = 0;
    private static final int B = 256;

    // see ~msg/struct/portV0.hex
    private static final BigPortNumber EXP_0_PORTNUM = bpn(1);
    private static final MacAddress EXP_0_HW = mac("114477:010101");
    private static final String EXP_0_NAME = "One";
    private static final PortConfig[] EXP_0_CONFIG = {NO_RECV, NO_FLOOD, NO_FWD};
    private static final PortState[] EXP_0_STATE = {LINK_DOWN, STP_LISTEN};
    private static final PortFeature[] EXP_0_CURR = {RATE_1GB_FD, FIBER, AUTONEG};
    private static final PortFeature[] EXP_0_ADV = {RATE_1GB_FD, FIBER};
    private static final PortFeature[] EXP_0_SUPP = {RATE_1GB_FD, FIBER, AUTONEG};
    private static final PortFeature[] EXP_0_PEER = {RATE_100MB_FD};

    // see ~msg/struct/portV123.hex
    private static final BigPortNumber EXP_123_PORTNUM = bpn(258);
    private static final MacAddress EXP_123_HW = mac("114477:112233");
    private static final String EXP_123_NAME = "Two";
    private static final PortConfig[] EXP_123_CONFIG = {NO_RECV, NO_FWD, NO_PACKET_IN};
    private static final PortState[] EXP_123_STATE = {LINK_DOWN};
    private static final PortFeature[] EXP_123_CURR = {RATE_1GB_FD, FIBER, AUTONEG};
    private static final PortFeature[] EXP_123_ADV = {RATE_1GB_FD, FIBER};
    private static final PortFeature[] EXP_123_SUPP = {RATE_1GB_FD, FIBER, AUTONEG};
//    private static final PortFeature[] EXP_123_PEER = null;
    private static final long EXP_123_CURR_SPEED = 1100000;
    private static final long EXP_123_MAX_SPEED = 3000000;


    // ========================================================= PARSING ====

    @Test
    public void port123() {
        print(EOL + "port123()");
        OfPacketReader pkt = getMsgPkt(TF_PORT_V123);
        for (ProtocolVersion pv: PV_123) {
            print(pv);
            pkt.resetIndex();
            try {
                Port port = PortFactory.parsePort(pkt, pv);
                print(port.toDebugString());

                assertEquals(AM_NEQ, EXP_123_PORTNUM, port.getPortNumber());
                assertEquals(AM_NEQ, EXP_123_HW, port.getHwAddress());
                assertEquals(AM_NEQ, EXP_123_NAME, port.getName());
                verifyFlags(port.getConfig(), EXP_123_CONFIG);
                assertTrue(AM_HUH, port.isEnabled());
                verifyFlags(port.getState(), EXP_123_STATE);
                assertFalse(AM_HUH, port.isLinkUp());
                assertFalse(AM_HUH, port.isBlocked());
                verifyFlags(port.getCurrent(), EXP_123_CURR);
                verifyFlags(port.getAdvertised(), EXP_123_ADV);
                verifyFlags(port.getSupported(), EXP_123_SUPP);
                assertNull("peer", port.getPeer());
                assertEquals(AM_NEQ, EXP_123_CURR_SPEED, port.getCurrentSpeed());
                assertEquals(AM_NEQ, EXP_123_MAX_SPEED, port.getMaxSpeed());

            } catch (MessageParseException e) {
                print(e);
                fail(E_PARSE_FAIL);
            }
            checkEOBuffer(pkt);
        }
    }

    @Test
    public void port10() {
        print(EOL + "port10()");
        OfPacketReader pkt = getMsgPkt(TF_PORT_V10);
        try {
            Port port = PortFactory.parsePort(pkt, V_1_0);
            print(port.toDebugString());

            assertEquals(AM_NEQ, EXP_0_PORTNUM, port.getPortNumber());
            assertEquals(AM_NEQ, EXP_0_HW, port.getHwAddress());
            assertEquals(AM_NEQ, EXP_0_NAME, port.getName());
            verifyFlags(port.getConfig(), EXP_0_CONFIG);
            assertTrue(AM_HUH, port.isEnabled());
            verifyFlags(port.getState(), EXP_0_STATE);
            assertFalse(AM_HUH, port.isLinkUp());
            assertFalse(AM_HUH, port.isBlocked());
            verifyFlags(port.getCurrent(), EXP_0_CURR);
            verifyFlags(port.getAdvertised(), EXP_0_ADV);
            verifyFlags(port.getSupported(), EXP_0_SUPP);
            verifyFlags(port.getPeer(), EXP_0_PEER);
            assertEquals(AM_NEQ, NA, port.getCurrentSpeed());
            assertEquals(AM_NEQ, NA, port.getMaxSpeed());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private void checkPortNum(ProtocolVersion pv, byte[] bytes,
                              BigPortNumber expPort) {
        OfPacketReader pkt = getPacketReader(bytes);
        BigPortNumber bpn = PortFactory.parsePortNumber(pkt, pv);
        checkEOBuffer(pkt);
        print("{}: {} => {}", pv, ByteUtils.toHexArrayString(bytes),
                Port.portNumberToString(bpn));
        if (expPort != null) {
            assertEquals(AM_NEQ, expPort, bpn);
            validatePortNumEncoding(pv, expPort, bytes);
        }
    }

    private void validatePortNumEncoding(ProtocolVersion pv,
                                         BigPortNumber bpn, byte[] expBytes) {
        OfPacketWriter pkt = new OfPacketWriter(expBytes.length);
        PortFactory.encodePortNumber(bpn, pkt, pv);
        byte[] output = pkt.array();
        print("  {} encoding: {} => {}", pv, Port.portNumberToString(bpn),
                ByteUtils.toHexArrayString(output));
        assertArrayEquals(AM_NEQ, expBytes, output);
    }

    @Test
    public void portNumbersV10() {
        print(EOL + "portNumbersV10()");
        checkPortNum(V_1_0, new byte[]{0x00, 0x01}, bpn(1));
        checkPortNum(V_1_0, new byte[]{0x00, 0x42}, bpn(66));
        checkPortNum(V_1_0, new byte[]{0x01, 0x01}, bpn(257));
        checkPortNum(V_1_0, new byte[]{0xfe-B, 0xff-B}, bpn(0xfeff));
        // TODO: Review - this next one is debatable as to whether SPECIAL xn should be done
        checkPortNum(V_1_0, new byte[]{0xff-B, 0x00}, Port.MAX);
        checkPortNum(V_1_0, new byte[]{0xff-B, 0xf8-B}, Port.IN_PORT);
        checkPortNum(V_1_0, new byte[]{0xff-B, 0xf9-B}, Port.TABLE);
        checkPortNum(V_1_0, new byte[]{0xff-B, 0xfa-B}, Port.NORMAL);
        checkPortNum(V_1_0, new byte[]{0xff-B, 0xfb-B}, Port.FLOOD);
        checkPortNum(V_1_0, new byte[]{0xff-B, 0xfc-B}, Port.ALL);
        checkPortNum(V_1_0, new byte[]{0xff-B, 0xfd-B}, Port.CONTROLLER);
        checkPortNum(V_1_0, new byte[]{0xff-B, 0xfe-B}, Port.LOCAL);
        checkPortNum(V_1_0, new byte[]{0xff-B, 0xff-B}, Port.ANY);
    }

    @Test
    public void parseBadPortNumbersV10() {
        print(EOL + "parseBadPortNumbersV10()");
        ByteArrayGenerator bag = ByteArrayGenerator.createFromHex("ff:01-f7");
        Iterator<byte[]> iter = bag.iterator();
        while (iter.hasNext()) {
            try {
                checkPortNum(V_1_0, iter.next(), null);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print(FMT_EX, e);
            } catch (Exception e) {
                print(e);
                fail(AM_WREX);
            }
        }
    }

    @Test
    public void parseGoodPortNumbersV13() {
        print(EOL + "parseGoodPortNumbersV13()");
        ByteArrayGenerator bag = ByteArrayGenerator.createFromHex("00:00:ff:*");
        Iterator<byte[]> iter = bag.iterator();
        while (iter.hasNext()) {
            try {
                checkPortNum(V_1_3, iter.next(), null);
            } catch (Exception e) {
                print(e);
                fail(AM_UNEX);
            }
        }
    }

    @Test
    public void portNumbersV13() {
        print(EOL + "portNumbersV13()");
        checkPortNum(V_1_3, new byte[]{0x00, 0x00, 0x00, 0x01}, bpn(1));
        checkPortNum(V_1_3, new byte[]{0x00, 0x00, 0x00, 0x42}, bpn(66));
        checkPortNum(V_1_3, new byte[]{0x00, 0x00, 0x01, 0x01}, bpn(257));
        checkPortNum(V_1_3, new byte[]{0x00, 0x00, 0xfe-B, 0xff-B}, bpn(0xfeff));
        // TODO: Review - this next one is debatable as to whether SPECIAL xn should be done
        checkPortNum(V_1_3, new byte[]{0x00, 0x00, 0xff-B, 0x00}, bpn(0xff00));
        checkPortNum(V_1_3, new byte[]{0x00, 0x00, 0xff-B, 0x23}, bpn(0xff23));
        checkPortNum(V_1_3, new byte[]{0x00, 0x00, 0xff-B, 0xfa-B}, bpn(0xfffa));
        checkPortNum(V_1_3, new byte[]{0x00, 0x00, 0xff-B, 0xff-B}, bpn(0xffff));
        checkPortNum(V_1_3, new byte[]{0x00, 0x01, 0xff-B, 0xff-B}, bpn(0x1ffff));
        checkPortNum(V_1_3, new byte[]{0xff-B, 0xff-B, 0xff-B, 0x00}, Port.MAX);
        checkPortNum(V_1_3, new byte[]{0xff-B, 0xff-B, 0xff-B, 0xf8-B}, Port.IN_PORT);
        checkPortNum(V_1_3, new byte[]{0xff-B, 0xff-B, 0xff-B, 0xf9-B}, Port.TABLE);
        checkPortNum(V_1_3, new byte[]{0xff-B, 0xff-B, 0xff-B, 0xfa-B}, Port.NORMAL);
        checkPortNum(V_1_3, new byte[]{0xff-B, 0xff-B, 0xff-B, 0xfb-B}, Port.FLOOD);
        checkPortNum(V_1_3, new byte[]{0xff-B, 0xff-B, 0xff-B, 0xfc-B}, Port.ALL);
        checkPortNum(V_1_3, new byte[]{0xff-B, 0xff-B, 0xff-B, 0xfd-B}, Port.CONTROLLER);
        checkPortNum(V_1_3, new byte[]{0xff-B, 0xff-B, 0xff-B, 0xfe-B}, Port.LOCAL);
        checkPortNum(V_1_3, new byte[]{0xff-B, 0xff-B, 0xff-B, 0xff-B}, Port.ANY);
    }

    @Test
    public void parseBadPortNumbersV13() {
        print(EOL + "parseBadPortNumbersV13()");
        ByteArrayGenerator bag = ByteArrayGenerator.createFromHex("ff:ff:ff:01-f7");
        Iterator<byte[]> iter = bag.iterator();
        while (iter.hasNext()) {
            try {
                checkPortNum(V_1_3, iter.next(), null);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print(FMT_EX, e);
            } catch (Exception e) {
                print(e);
                fail(AM_WREX);
            }
        }
    }


    // ============================================= CREATING / ENCODING ====

    @Test
    public void writePort13() {
        print(EOL + "writePort13()");
        ProtocolVersion pv = V_1_3;
        print(EOL + pv + "........");
        // first, construct the port
        MutablePort port = PortFactory.createPort(pv);
        // then, recreate the port, to match the test hex file
        port.portNumber(EXP_123_PORTNUM).hwAddress(EXP_123_HW).name(EXP_123_NAME)
            .config(new HashSet<PortConfig>(Arrays.asList(EXP_123_CONFIG)))
            .state(new HashSet<PortState>(Arrays.asList(EXP_123_STATE)))
            .current(new HashSet<PortFeature>(Arrays.asList(EXP_123_CURR)))
            .advertised(new HashSet<PortFeature>(Arrays.asList(EXP_123_ADV)))
            .supported(new HashSet<PortFeature>(Arrays.asList(EXP_123_SUPP)));
        // leave peer features as null (.peer())
        port.currentSpeed(EXP_123_CURR_SPEED).maxSpeed(EXP_123_MAX_SPEED);
        // Get an immutable copy
        Port copy = (Port) port.toImmutable();
        print(copy.toDebugString());
        // get expected data
        byte[] expData = getExpByteArray(TF_PORT_V123);
        // encode the port into a buffer the same size
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        try {
            PortFactory.encodePort(copy, pkt);
        } catch (IncompleteStructureException e) {
            print(e);
            fail(AM_UNEX);
        }
        byte[] encoded = pkt.array();
        printHexArray(encoded);
        // check we got what we expected
        assertArrayEquals(AM_NEQ, expData, encoded);
    }

    @Test
    public void writePort10() throws IncompleteStructureException {
        print(EOL + "writePort10()");
        // first, construct the port, using package private methods
        // then recreate the port, to match the test hex file
        MutablePort port = PortFactory.createPort(V_1_0)
            .portNumber(EXP_0_PORTNUM).hwAddress(EXP_0_HW).name(EXP_0_NAME)
            .config(new HashSet<PortConfig>(Arrays.asList(EXP_0_CONFIG)))
            .state(new HashSet<PortState>(Arrays.asList(EXP_0_STATE)))
            .current(new HashSet<PortFeature>(Arrays.asList(EXP_0_CURR)))
            .advertised(new HashSet<PortFeature>(Arrays.asList(EXP_0_ADV)))
            .supported(new HashSet<PortFeature>(Arrays.asList(EXP_0_SUPP)))
            .peer(new HashSet<PortFeature>(Arrays.asList(EXP_0_PEER)));
        // and get an immutable copy
        Port copy = (Port) port.toImmutable();
        print(copy.toDebugString());
        // get expected data
        byte[] expData = getExpByteArray(TF_PORT_V10);
        // encode the port into a buffer the same size
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        PortFactory.encodePort(copy, pkt);
        byte[] encoded = pkt.array();
        printHexArray(encoded);
        // check we got what we expected
        assertArrayEquals(AM_NEQ, expData, encoded);
    }


    // =========== Convenience method testing

    private void testIsEnabled(MutablePort p) {
        // config starts as null
        assertFalse(AM_HUH, p.isEnabled());

        // set a config without PORT_DOWN
        Set<PortConfig> cfg = new TreeSet<PortConfig>();
        p.config(cfg);
        assertTrue(AM_HUH, p.isEnabled());

        // set PORT_DOWN
        cfg.add(PortConfig.PORT_DOWN);
        p.config(cfg);
        assertFalse(AM_HUH, p.isEnabled());
    }

    @Test
    public void testIsEnabled() {
        print(EOL + "testIsEnabled()");
        testIsEnabled(PortFactory.createPort(V_1_0));
        testIsEnabled(PortFactory.createPort(V_1_3));
    }

    private void testIsLinkUp(MutablePort p) {
        // state starts as null
        assertFalse(AM_HUH, p.isLinkUp());

        // set a state without LINK_DOWN
        Set<PortState> st = new TreeSet<PortState>();
        p.state(st);
        assertTrue(AM_HUH, p.isLinkUp());

        // set LINK_DOWN
        st.add(PortState.LINK_DOWN);
        p.state(st);
        assertFalse(AM_HUH, p.isLinkUp());
    }

    @Test
    public void testIsLinkUp() {
        print(EOL + "testIsLinkUp()");
        testIsLinkUp(PortFactory.createPort(V_1_0));
        testIsLinkUp(PortFactory.createPort(V_1_3));
    }

    private void testIsBlocked(MutablePort p, PortState block) {
        // state starts as null
        assertFalse(AM_HUH, p.isBlocked());

        // set a state without the blocked flag
        Set<PortState> st = new TreeSet<PortState>();
        p.state(st);
        assertFalse(AM_HUH, p.isBlocked());

        // set blocked flag
        st.add(block);
        p.state(st);
        assertTrue(AM_HUH, p.isBlocked());
    }

    @Test
    public void testIsBlocked() {
        print(EOL + "testIsBlocked()");
        testIsBlocked(PortFactory.createPort(V_1_0), STP_BLOCK);
        testIsBlocked(PortFactory.createPort(V_1_3), BLOCKED);
    }
}
