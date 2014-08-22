/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.pkt.*;
import org.opendaylight.of.lib.msg.OfmMutablePacketOut;
import org.opendaylight.of.lib.msg.OfmPacketIn;
import org.opendaylight.util.net.*;
import org.opendaylight.util.packet.*;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.dt.BufferId.NO_BUFFER;
import static org.opendaylight.of.lib.msg.MessageFactory.createPacketOutFromPacketIn;
import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.packet.ProtocolId.ETHERNET;

/**
 * Unit tests for {@link MsgContext}.
 *
 * @author Simon Hunt
 */
public class MsgContextTest extends AbstractSequencerTest {

    private static final int EXPER_TYPE = -5;

    private static final MacAddress DST_MAC = mac("01:80:c2:00:00:0e");
    private static final MacAddress SRC_MAC = mac("08:2e:5f:69:c4:7b");
    
    private MessageContext ctx;
    private MsgContext mctx;

    @Rule
    public ExpectedException expect = ExpectedException.none();

    @Test
    public void basic() {
        print(EOL + "basic()");
        mctx = new MsgContext(SOME_EVENT);
        ctx = mctx;
        print(ctx);
        print("===");
        print(ctx.toDebugString());
        assertEquals(AM_NEQ, SOME_EVENT, ctx.srcEvent());
        assertEquals(AM_NEQ, PI_10, ctx.getPacketIn());
        assertArrayEquals(AM_NEQ, EXP_PROTOCOLS, ctx.getProtocols().toArray());
        verifyDecodedPacket(ctx.decodedPacket());
        assertEquals(AM_UXS, 0, ctx.getHints().size());
        BigPortNumber inPort = ((OfmPacketIn)ctx.srcEvent().msg()).getInPort();
        assertEquals(AM_NEQ, inPort, mctx.getMutablePacketOut().getInPort());
    }

    private static TcpUdpPort tcpPort(int n) {
        return TcpUdpPort.valueOf(n, IpProtocol.TCP);
    }

    private void verifyDecodedPacket(Packet packet) {
        Protocol p = packet.innermost();
        assertEquals(AM_NEQ, ProtocolId.TCP, p.id());
        Tcp tcp = (Tcp) p;
        assertEquals(AM_NEQ, tcpPort(6633), tcp.dstPort());
        assertEquals(AM_NEQ, tcpPort(64088), tcp.srcPort());
    }

    @Test
    public void addSomeHints() {
        print(EOL + "addSomeHints()");
        Hint h2 = HintFactory.createHint(EXPER_TYPE);
        mctx = (MsgContext) new MsgContext(SOME_EVENT).addHint(h2);
        ctx = mctx;
        print(ctx);
        print("===");
        print(ctx.toDebugString());

        assertEquals(AM_NEQ, SOME_EVENT, ctx.srcEvent());
        assertEquals(AM_UXS, 1, ctx.getHints().size());
        Iterator<Hint> iter = ctx.getHints().iterator();

        Hint h = iter.next();
        assertEquals(AM_NEQ, null, h.getType());
        assertEquals(AM_NEQ, EXPER_TYPE, h.getEncodedType());
    }

    @Test
    public void addTestPacketHint() {
        print(EOL + "addTestPacketHint()");
        Hint h2 = HintFactory.createHint(EXPER_TYPE);
        Hint th = HintFactory.createHint(HintType.TEST_PACKET);
        mctx = (MsgContext) new MsgContext(SOME_EVENT).addHint(h2);
        assertFalse(AM_HUH, mctx.isTestPacket());
        mctx.addHint(th);
        assertTrue(AM_HUH, mctx.isTestPacket());
        ctx = mctx;
        print(ctx);
        print("===");
        print(ctx.toDebugString());
    }

    private static class TestSPL extends SequencedPacketAdapter { }

    @Test
    public void sent() {
        print(EOL + "sent()");
        mctx = new MsgContext(SOME_EVENT);
        mctx.enablePacketOut(true);
        assertFalse(AM_HUH, mctx.isSent());
        assertFalse(AM_HUH, mctx.isHandled());
        mctx.tagHandler(TestSPL.class);
        assertFalse(AM_HUH, mctx.isHandled());
        mctx.packetOut().send();
        assertTrue(AM_HUH, mctx.isSent());
        assertTrue(AM_HUH, mctx.isHandled());
    }

    @Test
    public void blocked() {
        print(EOL + "blocked()");
        mctx = new MsgContext(SOME_EVENT);
        mctx.enablePacketOut(true);
        assertFalse(AM_HUH, mctx.isBlocked());
        assertFalse(AM_HUH, mctx.isHandled());
        mctx.tagHandler(TestSPL.class);
        assertFalse(AM_HUH, mctx.isHandled());
        mctx.packetOut().block();
        assertTrue(AM_HUH, mctx.isBlocked());
        assertTrue(AM_HUH, mctx.isHandled());
    }

    @Test
    public void badAddAction() {
        print(EOL + "badAddAction()");
        mctx = new MsgContext(SOME_EVENT);
        mctx.enablePacketOut(true);
        mctx.packetOut().block();
        expect.expect(IllegalStateException.class);
        expect.expectMessage("blocked");
        mctx.packetOut().addAction(null);
    }

    @Test
    public void badClearActions() {
        print(EOL + "badClearActions()");
        mctx = new MsgContext(SOME_EVENT);
        mctx.enablePacketOut(true);
        mctx.packetOut().block();
        expect.expect(IllegalStateException.class);
        expect.expectMessage("blocked");
        mctx.packetOut().clearActions();
    }

    @Test
    public void badBlock() {
        print(EOL + "badBlock()");
        mctx = new MsgContext(SOME_EVENT);
        mctx.enablePacketOut(true);
        mctx.tagHandler(TestSPL.class);
        mctx.packetOut().send();
        expect.expect(IllegalStateException.class);
        expect.expectMessage("sent already");
        mctx.packetOut().block();
    }

    @Test
    public void unauthorizedAddAction() {
        print(EOL + "unauthorizedAddAction()");
        mctx = new MsgContext(SOME_EVENT);
        expect.expect(IllegalStateException.class);
        expect.expectMessage("only for DIRECTOR");
        mctx.packetOut().addAction(null);
    }

    @Test
    public void unauthorizedClearActions() {
        print(EOL + "unauthorizedClearActions()");
        mctx = new MsgContext(SOME_EVENT);
        expect.expect(IllegalStateException.class);
        expect.expectMessage("only for DIRECTOR");
        mctx.packetOut().clearActions();
    }

    @Test
    public void unauthorizedBlock() {
        print(EOL + "unauthorizedBlock()");
        mctx = new MsgContext(SOME_EVENT);
        expect.expect(IllegalStateException.class);
        expect.expectMessage("only for DIRECTOR");
        mctx.packetOut().block();
    }

    @Test
    public void partialPacketIn() {
        print(EOL + "partialPacketIn()");
        MessageEvent event = new TestMessageEvent(DPID, 0, V_1_3,
                PARTIAL_PKT_IN);
        ctx = new MsgContext(event);
        OfmPacketIn pktIn = (OfmPacketIn) event.msg();
        assertTrue(AM_HUH, NO_BUFFER != pktIn.getBufferId());
        Packet pkt = ctx.decodedPacket();
        assertEquals(AM_NEQ, 1, pkt.size());
        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(AM_NEQ, DST_MAC, eth.dstAddr());
        assertEquals(AM_NEQ, SRC_MAC, eth.srcAddr());
        assertEquals(AM_NEQ, EthernetType.LLDP, eth.type());
    }

    @Test
    public void packetInToPacketOut() {
        print(EOL + "packetInToPacketOut()");
        OfmMutablePacketOut po = createPacketOutFromPacketIn(PI_10);
        assertEquals(AM_NEQ, IN_PORT, po.getInPort());
        assertArrayEquals(AM_NEQ, PI_10.getData(), po.getData());
    }

}
