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
import static org.opendaylight.util.packet.Dhcp.Flag.UNICAST;
import static org.opendaylight.util.packet.Dhcp.OpCode.BOOT_REQ;
import static org.opendaylight.util.packet.DhcpOption.MessageType.INFORM;
import static org.opendaylight.util.packet.ProtocolId.*;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * DHCP Inform packet unit tests.
 *
 * @author Frank Wood
 */
public class DhcpInformTest extends PacketTest {

    private static final String ETH2_DHCP_REQ = "eth2-ip-udp-dhcp-inform.hex";
    private static final int ETH2_DHCP_REQ_LEN = 342;

    private static final MacAddress DST_MAC = mac("ff:ff:ff:ff:ff:ff");
    private static final MacAddress SRC_MAC = mac("f4:ce:46:22:52:b3");

    private static final IpAddress ZERO_IP = ip("0.0.0.0");

    private static final IpAddress SRC_IP = ip("15.255.125.61");
    private static final IpAddress DST_IP = ip("255.255.255.255");

    private static final TcpUdpPort SRC_PORT = DhcpCodec.BOOTPC;
    private static final TcpUdpPort DST_PORT = DhcpCodec.BOOTPS;

    private static final int IP_IDENT = 18758;
    private static final int TTL = 128;

    private static final int IP_LEN = 328;
    private static final int UDP_LEN = 308;

    private static final IpAddress CLIENT_IP = SRC_IP;
    private static final MacAddress CLIENT_ID = SRC_MAC;
    private static final MacAddress CLIENT_MAC = mac("f4:ce:46:22:52:b3");
    private static final String VENDOR_CLASS_ID = "MS-UC-Client";
    private static final String HOST_NAME = "ddawson5";

    private static final Code[] PARAMS_REQ = new Code[] {
        Code.SIP_SERVERS, Code.VENDOR_SPECIFIC
    };

    private static final long TRANS_ID = 0x0a1d6660bL;
    private static final long UDP_CHECK_SUM = 0x4656;
    private static final int NUM_SECS = 0;
    private static final int HOP_COUNT = 0;

    private Packet decodeDhcpFile() {
        return Codec.decodeEthernet(getPacketReader(ETH2_DHCP_REQ));
    }

    private void verifyDhcp(Packet pkt) {
        assertEquals(4, pkt.size());
        assertEquals(DHCP, pkt.innermostId());

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
        assertEquals(UNICAST, dhcp.flag());
        assertEquals(CLIENT_IP, dhcp.clientAddr());
        assertEquals(ZERO_IP, dhcp.yourAddr());
        assertEquals(ZERO_IP, dhcp.serverAddr());
        assertEquals(ZERO_IP, dhcp.gatewayAddr());
        assertEquals(CLIENT_MAC, dhcp.clientHwAddr());
        assertEquals(EMPTY, dhcp.serverHostName());
        assertEquals(EMPTY, dhcp.bootFileName());

        Map<Code, DhcpOption> options = dhcp.options();
        assertEquals(5, options.size());

        assertEquals(INFORM, options.get(Code.MSG_TYPE).msgType());
        assertEquals(CLIENT_ID, options.get(Code.CLIENT_ID).macAddr());
        assertEquals(HOST_NAME, options.get(Code.HOST_NAME).name());
        assertArrayEquals(PARAMS_REQ, options.get(Code.PARAM_REQ).codes());
        assertEquals(VENDOR_CLASS_ID, options.get(Code.VENDOR_CLASS_ID).name());
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
            .hopCount(HOP_COUNT)
            .transId(TRANS_ID)
            .numSecs(NUM_SECS)
            .clientAddr(CLIENT_IP)
            .yourAddr(ZERO_IP)
            .serverAddr(ZERO_IP)
            .gatewayAddr(ZERO_IP)
            .clientHwAddr(CLIENT_MAC)
            .options(new DhcpOption[] {
                new DhcpOption(INFORM),
                new DhcpOption(Code.CLIENT_ID, CLIENT_ID),
                new DhcpOption(Code.HOST_NAME, HOST_NAME),
                new DhcpOption(Code.PARAM_REQ, PARAMS_REQ),
                new DhcpOption(Code.VENDOR_CLASS_ID, VENDOR_CLASS_ID)
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

        Packet pkt2 = Codec.decodeEthernet(encoding);

        Ip ip = pkt2.get(IP);
        Udp udp = pkt2.get(UDP);

        assertEquals(IP_LEN, ip.totalLen());
        assertEquals(UDP_LEN, udp.len());

        assertArrayEquals(expected, encoding);
    }

}
