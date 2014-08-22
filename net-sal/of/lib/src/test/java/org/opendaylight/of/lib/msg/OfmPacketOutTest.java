/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.VlanId;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.ActionFactory.createActionSetField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_OUT;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the OfmPacketOut message.
 *
 * @author Simon Hunt
 */
public class OfmPacketOutTest extends OfmTest {

    // test files
    private static final String TF_POUT_13 = "v13/packetOut";
    private static final String TF_POUT_10 = "v10/packetOut";

    private static final BufferId BUFFER_ID = BufferId.NO_BUFFER;
    private static final BigPortNumber IN_PORT = bpn(19);
    private static final VlanId ACT_VLAN = VlanId.valueOf(42);
    private static final int ACT_VPRI = 7;
    private static final MacAddress ACT_DST_MAC = mac("00001e:453411");
    private static final IpAddress ACT_DST_IP = ip("15.254.17.1");
    private static final BigPortNumber OUT_PORT = bpn(123);
    private static final int FRAME_LENGTH = 261;

    // frame data
    private static final String TF_LLDP = "lldpPacket";

    private MutableMessage mm;
    private OfmMutablePacketOut pout;

    // ========================================================= PARSING ====

    @Test
    public void packetOut13() {
        print(EOL + "packetOut13()");
        OfmPacketOut msg = (OfmPacketOut)
                verifyMsgHeader(TF_POUT_13, V_1_3, PACKET_OUT, 341);
        assertEquals(AM_NEQ, BUFFER_ID, msg.getBufferId());
        assertEquals(AM_NEQ, IN_PORT, msg.getInPort());
        Iterator<Action> ai = msg.getActions().iterator();
        verifyAction(ai.next(), ActionType.DEC_NW_TTL);
        verifyActionSetField(ai.next(), ETH_DST, ACT_DST_MAC);
        verifyActionSetField(ai.next(), IPV4_DST, ACT_DST_IP);
        verifyAction(ai.next(), ActionType.OUTPUT, OUT_PORT);
        assertFalse(AM_HUH, ai.hasNext());
        byte[] frameData = msg.getData();
        assertEquals(AM_UXS, FRAME_LENGTH, frameData.length);
        byte[] expData = getExpByteArray(TF_LLDP);
        assertArrayEquals(AM_NEQ, expData, frameData);
        assertEquals(AM_UXS, FRAME_LENGTH, msg.getDataLength());
    }

    @Test
    public void packetOut10() {
        print(EOL + "packetOut10()");
        OfmPacketOut msg = (OfmPacketOut)
                verifyMsgHeader(TF_POUT_10, V_1_0, PACKET_OUT, 309);
        assertEquals(AM_NEQ, BUFFER_ID, msg.getBufferId());
        assertEquals(AM_NEQ, IN_PORT, msg.getInPort());
        Iterator<Action> ai = msg.getActions().iterator();
        verifyActionSetField(ai.next(), VLAN_VID, ACT_VLAN);
        verifyActionSetField(ai.next(), VLAN_PCP, ACT_VPRI);
        verifyActionSetField(ai.next(), IPV4_DST, ACT_DST_IP);
        verifyAction(ai.next(), ActionType.OUTPUT, OUT_PORT);
        assertFalse(AM_HUH, ai.hasNext());
        byte[] frameData = msg.getData();
        assertEquals(AM_UXS, FRAME_LENGTH, frameData.length);
        byte[] expData = getExpByteArray(TF_LLDP);
        assertArrayEquals(AM_NEQ, expData, frameData);
        assertEquals(AM_UXS, FRAME_LENGTH, msg.getDataLength());
    }

    @Test
    public void clearActions() {
        ProtocolVersion pv = V_1_3;
        pout = createPout(pv);
        pout.addAction(createAction(pv, ActionType.DEC_NW_TTL))
                .addAction(createActionSetField(pv, ETH_DST, ACT_DST_MAC))
                .addAction(createActionSetField(pv, IPV4_DST, ACT_DST_IP))
                .addAction(createAction(pv, ActionType.OUTPUT, OUT_PORT));
        assertEquals(AM_UXS, 4, pout.getActions().size());
        pout.clearActions();
        assertEquals(AM_UXS, 0, pout.getActions().size());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodePacketOut13() {
        print(EOL + "encodePacketOut13()");
        final ProtocolVersion pv = V_1_3;

        mm = MessageFactory.create(pv, PACKET_OUT);
        mm.clearXid();
        verifyMutableHeader(mm, pv, PACKET_OUT, 0);

        // assemble the pieces
        pout = (OfmMutablePacketOut) mm;
        pout.bufferId(BUFFER_ID).inPort(IN_PORT);
        pout.addAction(createAction(pv, ActionType.DEC_NW_TTL))
                .addAction(createActionSetField(pv, ETH_DST, ACT_DST_MAC))
                .addAction(createActionSetField(pv, IPV4_DST, ACT_DST_IP))
                .addAction(createAction(pv, ActionType.OUTPUT, OUT_PORT));
        byte[] frameData = getExpByteArray(TF_LLDP);
        pout.data(frameData);

        // finally...
        encodeAndVerifyMessage(mm.toImmutable(), TF_POUT_13);
    }

    @Test
    public void encodePacketOut10() {
        print(EOL + "encodePacketOut10()");
        final ProtocolVersion pv = V_1_0;

        mm = MessageFactory.create(pv, PACKET_OUT);
        mm.clearXid();
        verifyMutableHeader(mm, pv, PACKET_OUT, 0);

        // assemble the pieces
        pout = (OfmMutablePacketOut) mm;
        pout.bufferId(BUFFER_ID).inPort(IN_PORT);
        pout.addAction(createActionSetField(pv, VLAN_VID, ACT_VLAN))
                .addAction(createActionSetField(pv, VLAN_PCP, ACT_VPRI))
                .addAction(createActionSetField(pv, IPV4_DST, ACT_DST_IP))
                .addAction(createAction(pv, ActionType.OUTPUT, OUT_PORT));
        byte[] frameData = getExpByteArray(TF_LLDP);
        pout.data(frameData);

        // finally...
        encodeAndVerifyMessage(mm.toImmutable(), TF_POUT_10);
    }
    
    
    // A set of tests for inPort(...) validation:
    // First, for 1.3
    
    private OfmMutablePacketOut createPout(ProtocolVersion pv) {
        return (OfmMutablePacketOut) MessageFactory.create(pv, PACKET_OUT);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void nonStandardPort13MaxPlusOne() {
        BigPortNumber maxPlusOne = bpn(Port.MAX.toLong() + 1);
        createPout(V_1_3).inPort(maxPlusOne);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void nonStandardPort13Zero() {
        BigPortNumber zero = bpn(0);
        createPout(V_1_3).inPort(zero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonStandardPort13None() {
        createPout(V_1_3).inPort(Port.NONE);
    }

    @Test
    public void standardPort13One() {
        print(EOL + "standardPort13One()");
        pout = createPout(V_1_3).inPort(bpn(1));
        print(pout.toDebugString());
        assertEquals(AM_NEQ, bpn(1), pout.getInPort());
    } 
    
    @Test
    public void standardPort13Max() {
        print(EOL + "standardPort13Max()");
        pout = createPout(V_1_3).inPort(Port.MAX);
        print(pout.toDebugString());
        assertEquals(AM_NEQ, Port.MAX, pout.getInPort());
    }

    // Repeat, for 1.0 : note that Port.NONE is valid

    @Test(expected = IllegalArgumentException.class)
    public void nonStandardPort10MaxPlusOne() {
        BigPortNumber maxPlusOne = bpn(Port.MAX.toLong() + 1);
        createPout(V_1_0).inPort(maxPlusOne);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonStandardPort10Zero() {
        BigPortNumber zero = bpn(0);
        createPout(V_1_0).inPort(zero);
    }

    @Test
    public void standardPort10None() {
        print(EOL + "standardPort10None()");
        pout = createPout(V_1_0).inPort(Port.NONE);
        print(pout.toDebugString());
        assertEquals(AM_NEQ, Port.NONE, pout.getInPort());
    }

    @Test
    public void standardPort10One() {
        print(EOL + "standardPort10One()");
        pout = createPout(V_1_0).inPort(bpn(1));
        print(pout.toDebugString());
        assertEquals(AM_NEQ, bpn(1), pout.getInPort());
    }

    @Test
    public void standardPort10Max() {
        print(EOL + "standardPort10Max()");
        pout = createPout(V_1_0).inPort(Port.MAX);
        print(pout.toDebugString());
        assertEquals(AM_NEQ, Port.MAX, pout.getInPort());
    }
}
