/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.packet.PppEthernet.Code;
import org.opendaylight.util.packet.PppEthernet.PppProtocolId;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.packet.ProtocolId.ETHERNET;
import static org.opendaylight.util.packet.ProtocolId.PPP_ETHERNET;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * PPP-over-Ethernet packet unit tests.
 *
 * @author Frank Wood
 */
public class PppEthernetTest extends PacketTest {

    private static final String PADI_DATA = "eth2-ppp-padi.hex";
    private static final int PADI_DATA_LEN = 60;

    private static final MacAddress PADI_DST_MAC = mac("ff:ff:ff:ff:ff:ff");
    private static final MacAddress PADI_SRC_MAC = mac("cc:05:0e:88:00:00");

    private static final EthernetType ETH_TYPE_PPPOE_DISCO =
            EthernetType.valueOf(0x08863);

    private static final int PPP_VER = 1;
    private static final int PPP_TYPE = 1;

    private static final int PADI_SESSION_ID = 0;
    private static final int PADI_LEN = 12;
    private static final byte[] PADI_TAGS = new byte[] {
        0x01, 0x01, 0x00, 0x00, 0x01, 0x03, 0x00, 0x04,
        0x64, 0x13, (byte)0x85, 0x18
    };

    private static final String PADO_DATA = "eth2-ppp-pado.hex";
    private static final int PADO_DATA_LEN = 60;

    private static final MacAddress PADO_DST_MAC = mac("cc:05:0e:88:00:00");
    private static final MacAddress PADO_SRC_MAC = mac("ca:01:0e:88:00:06");

    private static final int PADO_SESSION_ID = 0;
    private static final int PADO_LEN = 40;
    private static final byte[] PADO_TAGS = new byte[] {
        0x01, 0x01, 0x00, 0x00, 0x01, 0x03, 0x00, 0x04,
        0x64, 0x13, (byte)0x85, 0x18, 0x01, 0x02, 0x00,
        0x04, 0x42, 0x52, 0x41, 0x53, 0x01, 0x04, 0x00,
        0x10, 0x3d, 0x0f, 0x05, (byte)0x87, 0x06, 0x24,
        (byte)0x84, (byte)0xf2, (byte)0xdf, 0x32,
        (byte)0xb9, (byte)0xdd, (byte)0xfd, (byte)0x77,
        (byte)0xbd, 0x5b
    };

    private static final String SESS_REQ_DATA = "eth2-ppp-session-req.hex";
    private static final int SESS_REQ_DATA_LEN = 60;

    private static final EthernetType ETH_TYPE_PPPOE_SESS =
            EthernetType.valueOf(0x08864);

    private static final MacAddress SESS_REQ_DST_MAC = mac("ca:01:0e:88:00:06");
    private static final MacAddress SESS_REQ_SRC_MAC = mac("cc:05:0e:88:00:00");

    private static final int SESS_REQ_SESSION_ID = 0x0011;
    private static final int SESS_REQ_LEN = 12;
    private static final byte[] SESS_REQ_BYTES = new byte[] {
        0x01, 0x01, 0x00, 0x0a,
        0x05, 0x06, 0x05, (byte)0xfc, (byte)0xd4, 0x59
    };

    private Packet decodePadiFile() {
        return Codec.decodeEthernet(getPacketReader(PADI_DATA));
    }

    private void verifyPadi(Packet pkt) {
        assertEquals(2, pkt.size());
        assertEquals(PPP_ETHERNET, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(PADI_DST_MAC, eth.dstAddr());
        assertEquals(PADI_SRC_MAC, eth.srcAddr());
        assertEquals(ETH_TYPE_PPPOE_DISCO, eth.type());

        PppEthernet pppEth = pkt.get(PPP_ETHERNET);
        assertEquals(PPP_VER, pppEth.version());
        assertEquals(PPP_TYPE, pppEth.type());
        assertEquals(Code.PADI, pppEth.code());
        assertEquals(PADI_SESSION_ID, pppEth.sessionId());
        assertEquals(PADI_LEN, pppEth.len());
        assertArrayEquals(PADI_TAGS, pppEth.bytes());
    }

    private Packet createPadi() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(PADI_DST_MAC)
            .srcAddr(PADI_SRC_MAC)
            .type(ETH_TYPE_PPPOE_DISCO)
            .build();

        PppEthernet pppEth = new PppEthernet.Builder()
            .code(Code.PADI)
            .bytes(PADI_TAGS)
            .build();

        return new Packet(eth, pppEth);
    }

    @Test
    public void decodePadi() {
        print(EOL + "decodePadi()");
        Packet pkt = decodePadiFile();
        print(pkt.toDebugString());
        verifyPadi(pkt);
    }

    @Test
    public void encodePadi() {
        print(EOL + "encodePadi()");
        Packet pkt = createPadi();
        print(pkt.toDebugString());

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(PADI_DATA);
        byte[] expected = r.readBytes(PADI_DATA_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

    private Packet decodePadoFile() {
        return Codec.decodeEthernet(getPacketReader(PADO_DATA));
    }

    private void verifyPado(Packet pkt) {
        assertEquals(2, pkt.size());
        assertEquals(PPP_ETHERNET, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(PADO_DST_MAC, eth.dstAddr());
        assertEquals(PADO_SRC_MAC, eth.srcAddr());
        assertEquals(ETH_TYPE_PPPOE_DISCO, eth.type());

        PppEthernet pppEth = pkt.get(PPP_ETHERNET);
        assertEquals(PPP_VER, pppEth.version());
        assertEquals(PPP_TYPE, pppEth.type());
        assertEquals(Code.PADO, pppEth.code());
        assertEquals(PADO_SESSION_ID, pppEth.sessionId());
        assertEquals(PADO_LEN, pppEth.len());
        assertArrayEquals(PADO_TAGS, pppEth.bytes());
    }

    private Packet createPado() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(PADO_DST_MAC)
            .srcAddr(PADO_SRC_MAC)
            .type(ETH_TYPE_PPPOE_DISCO)
            .build();

        PppEthernet pppEth = new PppEthernet.Builder()
            .code(Code.PADO)
            .bytes(PADO_TAGS)
            .build();

        return new Packet(eth, pppEth);
    }

    @Test
    public void decodePado() {
        print(EOL + "decodePado()");
        Packet pkt = decodePadoFile();
        print(pkt.toDebugString());
        verifyPado(pkt);
    }

    @Test
    public void encodePado() {
        print(EOL + "encodePado()");
        Packet pkt = createPado();
        print(pkt.toDebugString());

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(PADO_DATA);
        byte[] expected = r.readBytes(PADO_DATA_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

    private Packet decodeSessReqFile() {
        return Codec.decodeEthernet(getPacketReader(SESS_REQ_DATA));
    }

    private void verifySessReq(Packet pkt) {
        assertEquals(2, pkt.size());
        assertEquals(PPP_ETHERNET, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(SESS_REQ_DST_MAC, eth.dstAddr());
        assertEquals(SESS_REQ_SRC_MAC, eth.srcAddr());
        assertEquals(ETH_TYPE_PPPOE_SESS, eth.type());

        PppEthernet pppEth = pkt.get(PPP_ETHERNET);
        assertEquals(PPP_VER, pppEth.version());
        assertEquals(PPP_TYPE, pppEth.type());
        assertEquals(Code.SESSION_DATA, pppEth.code());
        assertEquals(SESS_REQ_SESSION_ID, pppEth.sessionId());
        assertEquals(SESS_REQ_LEN, pppEth.len());
        assertArrayEquals(SESS_REQ_BYTES, pppEth.bytes());
    }

    private Packet createSessReq() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(SESS_REQ_DST_MAC)
            .srcAddr(SESS_REQ_SRC_MAC)
            .type(ETH_TYPE_PPPOE_SESS)
            .build();

        PppEthernet pppEth = new PppEthernet.Builder()
            .code(Code.SESSION_DATA)
            .sessionId(SESS_REQ_SESSION_ID)
            .pppProtocolId(PppProtocolId.PPP_LCP)
            .bytes(SESS_REQ_BYTES)
            .build();

        return new Packet(eth, pppEth);
    }

    @Test
    public void decodeSessReq() {
        print(EOL + "decodeSessReq()");
        Packet pkt = decodeSessReqFile();
        print(pkt.toDebugString());
        verifySessReq(pkt);
    }

    @Test
    public void encodeSessReq() {
        print(EOL + "encodeSessReq()");
        Packet pkt = createSessReq();
        print(pkt.toDebugString());

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(SESS_REQ_DATA);
        byte[] expected = r.readBytes(SESS_REQ_DATA_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

}
