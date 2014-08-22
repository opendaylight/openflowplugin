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
import org.opendaylight.util.packet.Dhcp.Flag;
import org.opendaylight.util.packet.DhcpOption.Code;
import org.junit.Test;

import java.util.Map;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.StringUtils.EMPTY;
import static org.opendaylight.util.net.EthernetType.IPv4;
import static org.opendaylight.util.packet.Dhcp.Flag.UNICAST;
import static org.opendaylight.util.packet.Dhcp.OpCode.BOOT_REQ;
import static org.opendaylight.util.packet.DhcpOption.MessageType.DISCOVER;
import static org.opendaylight.util.packet.ProtocolId.DHCP;
import static org.opendaylight.util.packet.ProtocolId.UDP;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * DHCP Discovery packet unit tests.
 *
 * @author Frank Wood
 */
public class DhcpDiscoTest extends PacketTest {

    private static final String ETH2_DHCP_DISCO = "eth2-ip-udp-dhcp-disco.hex";
    private static final int ETH2_DHCP_DISCO_LEN = 342;

    private static final MacAddress DST_MAC = mac("ff:ff:ff:ff:ff:ff");
    private static final MacAddress SRC_MAC = mac("00:24:a8:49:40:00");

    private static final IpAddress ZERO_IP = ip("0.0.0.0");

    private static final IpAddress SRC_IP = ZERO_IP;
    private static final IpAddress DST_IP = ip("255.255.255.255");

    private static final TcpUdpPort SRC_PORT = DhcpCodec.BOOTPC;
    private static final TcpUdpPort DST_PORT = DhcpCodec.BOOTPS;

    private static final int IP_IDENT = 37722;
    private static final int TTL = 64;

    private static final int UDP_LEN = 308;

    private static final MacAddress CLIENT_MAC = mac("00:24:a8:49:40:00");
    private static final int MAX_MSG_SIZE = 9156;
    private static final String VENDOR_CLASS_ID = "HP J8697A Switch 5406zl";

    private static final Code[] PARAMS_REQ = new Code[] {
        Code.SUBNET_MASK, Code.ROUTER, Code.TIME_SERVER, Code.DEFAULT_IP_TTL,
        Code.VENDOR_SPECIFIC, Code.DOMAIN_SERVER, Code.DOMAIN_NAME,
        Code.DOMAIN_SERACH
    };

    private static final long TRANS_ID = 0x017936667;
    private static final long UDP_CHECK_SUM = 0x0f333;
    private static final int NUM_SECS = 16;
    private static final int HOP_COUNT = 0;

    private static final int FLAGS = 0x08000;

    private Packet decodeDhcpFile() {
        return Codec.decodeEthernet(getPacketReader(ETH2_DHCP_DISCO));
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
        assertEquals(UNICAST, dhcp.flag());
        assertEquals(HOP_COUNT, dhcp.hopCount());
        assertEquals(TRANS_ID, dhcp.transId());
        assertEquals(NUM_SECS, dhcp.numSecs());
        assertEquals(UNICAST, dhcp.flag());
        assertEquals(ZERO_IP, dhcp.clientAddr());
        assertEquals(ZERO_IP, dhcp.yourAddr());
        assertEquals(ZERO_IP, dhcp.serverAddr());
        assertEquals(ZERO_IP, dhcp.gatewayAddr());
        assertEquals(CLIENT_MAC, dhcp.clientHwAddr());
        assertEquals(EMPTY, dhcp.serverHostName());
        assertEquals(EMPTY, dhcp.bootFileName());

        Map<Code, DhcpOption> options = dhcp.options();
        assertEquals(4, options.size());

        assertEquals(DISCOVER, options.get(Code.MSG_TYPE).msgType());
        assertEquals(MAX_MSG_SIZE,
                     (int) options.get(Code.MAX_MSG_SIZE).number());
        assertEquals(VENDOR_CLASS_ID, options.get(Code.VENDOR_CLASS_ID).name());
        assertArrayEquals(PARAMS_REQ, options.get(Code.PARAM_REQ).codes());
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
            .doNotFrag(true)
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
            .clientAddr(ZERO_IP)
            .yourAddr(ZERO_IP)
            .serverAddr(ZERO_IP)
            .gatewayAddr(ZERO_IP)
            .clientHwAddr(CLIENT_MAC)
            .options(new DhcpOption[] {
                new DhcpOption(DISCOVER),
                new DhcpOption(Code.MAX_MSG_SIZE, MAX_MSG_SIZE),
                new DhcpOption(Code.VENDOR_CLASS_ID, VENDOR_CLASS_ID),
                new DhcpOption(Code.PARAM_REQ, PARAMS_REQ)
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

        PacketReader r = getPacketReader(ETH2_DHCP_DISCO);
        byte[] expected = r.readBytes(ETH2_DHCP_DISCO_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

    @Test
    public void flags() {
        print(EOL + "flags()");

        Dhcp dhcp = new Dhcp.Builder()
            .opCode(BOOT_REQ)
            .flag(Flag.BROADCAST)
            .clientAddr(ZERO_IP)
            .yourAddr(ZERO_IP)
            .serverAddr(ZERO_IP)
            .gatewayAddr(ZERO_IP)
            .clientHwAddr(CLIENT_MAC)
            .build();

        assertEquals(FLAGS, dhcp.flag().code());
    }

}
