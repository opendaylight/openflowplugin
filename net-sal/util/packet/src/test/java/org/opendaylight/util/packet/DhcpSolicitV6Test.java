/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.packet.DhcpOptionV6.Type;
import org.opendaylight.util.packet.DhcpV6.MessageType;
import org.opendaylight.util.packet.PppEthernet.PppProtocolId;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.packet.ProtocolId.*;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * DHCPv6 Solicit packet unit tests.
 *
 * @author Frank Wood
 */
public class DhcpSolicitV6Test extends PacketTest {

    private static final String DHCP_DATA = "eth2-dhcpv6-solicit.hex";
    private static final int DHCP_DATA_LEN = 120;

    private static final MacAddress DST_MAC = mac("ca:01:0e:88:00:06");
    private static final MacAddress SRC_MAC = mac("cc:05:0e:88:00:00");

    private static final EthernetType ETH_TYPE_PPPOE_SESS =
            EthernetType.valueOf(0x08864);

    private static final int PPP_VER = 1;
    private static final int PPP_TYPE = 1;
    private static final PppEthernet.Code PPP_CODE =
            PppEthernet.Code.SESSION_DATA;
    private static final int PPP_SESS_ID = 0x0011;
    private static final int PPP_LEN = 100;

    private static final int IP_PAYLOAD_LEN = 58;
    private static final int IP_HOP_LIMIT = 255;

    private static final IpAddress SRC_IP = ip("fe80::ce05:eff:fe88:0");
    private static final IpAddress DST_IP = ip("ff02::1:2");

    private static final int UDP_LEN = 58;
    private static final int UDP_CHECK_SUM = 0x01a67;

    private static final int TRANS_ID = 0x0fc24ab;

    private static final int NUM_OPTIONS = 4;

    private static final byte[] ELAPSED_TIME_BYTES = new byte[] {
        0x05, (byte)0xe9
    };

    private static final byte[] CLIENT_ID_BYTES = new byte[] {
        0x00, 0x03, 0x00, 0x01, (byte)0xcc, 0x05, 0x0e, (byte)0x88, 0x00, 0x00
    };

    private static final byte[] OPTION_REQ_BYTES = new byte[] {
        0x00, 0x19, 0x00, 0x17, 0x00, 0x18
    };

    private static final byte[] IAPD_BYTES = new byte[] {
        0x00, 0x09, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    private Packet decodeDhcpFile() {
        return Codec.decodeEthernet(getPacketReader(DHCP_DATA));
    }

    private void verifyDhcp(Packet pkt) {
        assertEquals(5, pkt.size());
        assertEquals(DHCPV6, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(ETH_TYPE_PPPOE_SESS, eth.type());

        PppEthernet pppEth = pkt.get(PPP_ETHERNET);
        assertEquals(PPP_VER, pppEth.version());
        assertEquals(PPP_TYPE, pppEth.type());
        assertEquals(PPP_CODE, pppEth.code());
        assertEquals(PPP_SESS_ID, pppEth.sessionId());
        assertEquals(PPP_LEN, pppEth.len());
        assertEquals(PppProtocolId.PPP_IPV6, pppEth.pppProtocolId());

        IpV6 ip = pkt.get(IPV6);
        assertEquals(IpTosDsfc.CS7, ip.tosDsfc());
        assertEquals(IpTosEcn.NOT_ECT, ip.tosEcn());
        assertEquals(IP_PAYLOAD_LEN, ip.payloadLen());
        assertEquals(IpType.UDP, ip.nextHdr());
        assertEquals(IP_HOP_LIMIT, ip.hopLimit());
        assertEquals(SRC_IP, ip.srcAddr());
        assertEquals(DST_IP, ip.dstAddr());

        Udp udp = pkt.get(UDP);
        assertEquals(DhcpCodecV6.CLIENT_PORT, udp.srcPort());
        assertEquals(DhcpCodecV6.SERVER_PORT, udp.dstPort());
        assertEquals(UDP_LEN, udp.len());
        assertEquals(UDP_CHECK_SUM, udp.checkSum());

        DhcpV6 dhcp = pkt.get(DHCPV6);
        assertEquals(MessageType.SOLICIT, dhcp.msgType());
        assertEquals(TRANS_ID, dhcp.transId());

        DhcpOptionV6[] opts = dhcp.options();
        assertEquals(NUM_OPTIONS, opts.length);

        assertEquals(Type.ELAPSED_TIME, opts[0].type());
        assertArrayEquals(ELAPSED_TIME_BYTES, opts[0].bytes());

        assertEquals(Type.CLIENT_ID, opts[1].type());
        assertArrayEquals(CLIENT_ID_BYTES, opts[1].bytes());

        assertEquals(Type.OPTION_REQ, opts[2].type());
        assertArrayEquals(OPTION_REQ_BYTES, opts[2].bytes());

        assertEquals(Type.IDENT_ASSOC_PREFIX_DELEG, opts[3].type());
        assertArrayEquals(IAPD_BYTES, opts[3].bytes());
    }

    private Packet createDhcp() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(ETH_TYPE_PPPOE_SESS)
            .build();

        PppEthernet pppEth = new PppEthernet.Builder()
            .version(PPP_VER)
            .type(PPP_TYPE)
            .code(PPP_CODE)
            .sessionId(PPP_SESS_ID)
            .pppProtocolId(PppProtocolId.PPP_IPV6)
            .build();

        IpV6 ip = new IpV6.Builder()
            .tosDsfc(IpTosDsfc.CS7)
            .nextHdr(IpType.UDP)
            .hopLimit(IP_HOP_LIMIT)
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .build();

        Udp udp = new Udp.Builder()
            .srcPort(DhcpCodecV6.CLIENT_PORT)
            .dstPort(DhcpCodecV6.SERVER_PORT)
            .build();

        DhcpV6 dhcp = new DhcpV6.Builder()
            .msgType(MessageType.SOLICIT)
            .transId(TRANS_ID)
            .options(new DhcpOptionV6[] {
                new DhcpOptionV6(Type.ELAPSED_TIME, ELAPSED_TIME_BYTES),
                new DhcpOptionV6(Type.CLIENT_ID, CLIENT_ID_BYTES),
                new DhcpOptionV6(Type.OPTION_REQ, OPTION_REQ_BYTES),
                new DhcpOptionV6(Type.IDENT_ASSOC_PREFIX_DELEG, IAPD_BYTES)
            })
            .build();

        return new Packet(eth, pppEth, ip, udp, dhcp);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeDhcpFile();
        print(pkt.toDebugString());
        verifyDhcp(pkt);
    }

    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = createDhcp();
        print(pkt.toDebugString());

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(DHCP_DATA);
        byte[] expected = r.readBytes(DHCP_DATA_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

}
