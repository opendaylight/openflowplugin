/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.TcpUdpPort;
import org.opendaylight.util.packet.DhcpOption.Code;
import org.junit.Test;

import java.util.Map;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.StringUtils.EMPTY;
import static org.opendaylight.util.net.EthernetType.IPv4;
import static org.opendaylight.util.packet.Dhcp.Flag.BROADCAST;
import static org.opendaylight.util.packet.Dhcp.OpCode.BOOT_REQ;
import static org.opendaylight.util.packet.DhcpOption.Code.CLIENT_ID;
import static org.opendaylight.util.packet.DhcpOption.Code.PARAM_REQ;
import static org.opendaylight.util.packet.DhcpOption.MessageType.DISCOVER;
import static org.opendaylight.util.packet.ProtocolId.*;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * DHCP Discovery #3 packet unit tests.
 *
 * @author Frank Wood
 */
public class DhcpDisco3Test extends PacketTest {

    private static final String ETH2_DHCP_REQ = "eth2-ip-udp-dhcp-disco3.hex";
    private static final int ETH2_DHCP_REQ_LEN = 342;

    private static final MacAddress DST_MAC = mac("ff:ff:ff:ff:ff:ff");
    private static final MacAddress SRC_MAC = mac("00:1e:0b:ae:d3:be");

    private static final IpAddress ZERO_IP = ip("0.0.0.0");

    private static final IpAddress SRC_IP = ZERO_IP;
    private static final IpAddress DST_IP = ip("255.255.255.255");

    private static final TcpUdpPort SRC_PORT = DhcpCodec.BOOTPC;
    private static final TcpUdpPort DST_PORT = DhcpCodec.BOOTPS;

    private static final int IP_CHECK_SUM = 0x039a5;
    private static final int IP_IDENT = 1;
    private static final int TTL = 128;

    private static final int UDP_LEN = 308;

    private static final MacAddress CLIENT_MAC = mac("00:1e:0b:ae:d3:be");

    private static final Code[] PARAMS_REQ = new Code[] {
        Code.SUBNET_MASK, Code.HOST_NAME
    };

    private static final MacAddress CLIENT_ID_MAC = mac("06:05:04:03:02:01");

    private static final long TRANS_ID = 1;
    private static final long UDP_CHECK_SUM = 0x0890b;
    private static final int NUM_SECS = 0;
    private static final int HOP_COUNT = 0;

    private Packet decodeDhcpFile() {
        return Codec.decodeEthernet(getPacketReader(ETH2_DHCP_REQ));
    }

    private void verifyDhcp(Packet pkt) {
        assertEquals(4, pkt.size());
        assertEquals(DHCP, pkt.innermostId());

        Ip ip = pkt.get(IP);
        assertEquals(IP_CHECK_SUM, ip.checkSum());

        Udp udp = pkt.get(UDP);
        assertEquals(SRC_PORT, udp.srcPort());
        assertEquals(DST_PORT, udp.dstPort());
        assertEquals(UDP_LEN, udp.len());
        assertEquals(UDP_CHECK_SUM, udp.checkSum());

        Dhcp dhcp = pkt.get(DHCP);
        assertEquals(BOOT_REQ, dhcp.opCode());
        assertEquals(HardwareType.ETHERNET, dhcp.hwType());
        assertEquals(HOP_COUNT, dhcp.hopCount());
        assertEquals(TRANS_ID, dhcp.transId());
        assertEquals(NUM_SECS, dhcp.numSecs());
        assertEquals(BROADCAST, dhcp.flag());
        assertEquals(ZERO_IP, dhcp.clientAddr());
        assertEquals(ZERO_IP, dhcp.yourAddr());
        assertEquals(ZERO_IP, dhcp.serverAddr());
        assertEquals(ZERO_IP, dhcp.gatewayAddr());
        assertEquals(CLIENT_MAC, dhcp.clientHwAddr());
        assertEquals(EMPTY, dhcp.serverHostName());
        assertEquals(EMPTY, dhcp.bootFileName());

        Map<Code, DhcpOption> options = dhcp.options();
        assertEquals(3, options.size());

        assertEquals(DISCOVER, options.get(Code.MSG_TYPE).msgType());
        assertArrayEquals(PARAMS_REQ, options.get(Code.PARAM_REQ).codes());
        assertEquals(CLIENT_ID_MAC, options.get(Code.CLIENT_ID).macAddr());
    }

    private Packet createDhcp() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(IPv4)
            .build();

        Ip ip = new Ip.Builder()
            .ident(IP_IDENT)
            .ttl(TTL)
            .type(IpType.UDP)
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .build();

        Udp udp = new Udp.Builder()
            .srcPort(SRC_PORT)
            .dstPort(DST_PORT)
            .build();

        Dhcp dhcp = new Dhcp.Builder()
            .opCode(BOOT_REQ)
            .flag(BROADCAST)
            .hopCount(HOP_COUNT)
            .transId(TRANS_ID)
            .numSecs(NUM_SECS)
            .clientAddr(ZERO_IP)
            .yourAddr(ZERO_IP)
            .serverAddr(ZERO_IP)
            .gatewayAddr(ZERO_IP)
            .clientHwAddr(CLIENT_MAC)
            .options(new DhcpOption[] {
                new DhcpOption(DISCOVER),
                new DhcpOption(PARAM_REQ, PARAMS_REQ),
                new DhcpOption(CLIENT_ID, CLIENT_ID_MAC)
            })
            .build();

        return new Packet(eth, ip, udp, dhcp);
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

        PacketReader r = getPacketReader(ETH2_DHCP_REQ);
        byte[] expected = r.readBytes(ETH2_DHCP_REQ_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

}
