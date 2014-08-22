/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.FlowModCommand.ADD;
import static org.opendaylight.of.lib.msg.FlowModFlag.SEND_FLOW_REM;
import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.EthernetType.IPv4;
import static org.opendaylight.util.net.IpProtocol.UDP;

/**
 * Unit tests for {@link CodecUtils}.
 *
 * @author Shaila Shree
 * @author Simon Hunt
 */
public class CodecUtilsTest extends AbstractCodecTest {

    private static ObjectMapper mapper = new ObjectMapper();

    private static final String KEY = "SoMeKeY";
    private static final String VALUE = "value";
    private static final String MAC_STR = "66:55:44:32:22:11";
    private static final MacAddress EXP_MAC = mac(MAC_STR);
    private static final String IP_STR = "15.255.124.23";
    private static final IpAddress EXP_IP = ip(IP_STR);

    private JsonNode createJsonNode(int value) {
        ObjectNode node = mapper.createObjectNode();
        node.put(VALUE, value);
        return node.get(VALUE);
    }

    private JsonNode createJsonNode(String value) {
        ObjectNode node = mapper.createObjectNode();
        node.put(VALUE, value);
        return node.get(VALUE);
    }

    @Test
    public void encodeBigPort() {
        ObjectNode node = mapper.createObjectNode();
        assertEquals(AM_UXS, 0, node.size());
        CodecUtils.encodeBigPort(node, KEY, bpn(10), V_1_3);
        assertEquals(AM_UXS, 1, node.size());
        print(node);
        JsonNode value = node.get(KEY);
        assertTrue("wrong node type", value.isLong());
        long number = value.asLong();
        assertEquals(AM_NEQ, 10, number);
    }

    @Test
    public void encodeBigPortSpecial() {
        ObjectNode node = mapper.createObjectNode();
        assertEquals(AM_UXS, 0, node.size());
        CodecUtils.encodeBigPort(node, KEY, Port.CONTROLLER, V_1_3);
        assertEquals(AM_UXS, 1, node.size());
        print(node);
        JsonNode value = node.get(KEY);
        assertTrue("wrong node type", value.isTextual());
        String logicalName = value.asText();
        assertEquals(AM_NEQ, "CONTROLLER", logicalName);
    }

    @Test
    public void decodeBigPort() {
        JsonNode value = createJsonNode(10);
        assertEquals(AM_NEQ, bpn(10), CodecUtils.decodeBigPort(value));

        value = createJsonNode("flood");
        assertEquals(AM_NEQ, Port.FLOOD, CodecUtils.decodeBigPort(value));
    }

    @Test
    public void encodePort() {
        int value = CodecUtils.encodePort(pn(12));
        assertEquals(AM_NEQ, 12, value);
    }

    @Test
    public void decodePort() {
        JsonNode value = createJsonNode(12);
        assertEquals(AM_NEQ, pn(12), CodecUtils.decodePort(value));

        value = createJsonNode("controller");
        assertEquals(AM_NEQ, 0xfffd, CodecUtils.decodePort(value).toInt());
    }

    @Test
    public void encodeMac() {
        String value = CodecUtils.encodeMac(EXP_MAC);
        assertEquals(AM_NEQ, MAC_STR, value);
    }

    @Test
    public void decodeMac() {
        JsonNode value = createJsonNode(MAC_STR);
        assertEquals(AM_NEQ, EXP_MAC, CodecUtils.decodeMac(value));
    }

    @Test
    public void encodeIp() {
        String value = CodecUtils.encodeIp(EXP_IP);
        assertEquals(AM_NEQ, IP_STR, value);
    }

    @Test
    public void decodeIp() {
        JsonNode value = createJsonNode(IP_STR);
        assertEquals(AM_NEQ, EXP_IP, CodecUtils.decodeIp(value));
    }

    @Test
    public void encodeIpProto() {
        String value = CodecUtils.encodeIpProto(UDP);
        assertEquals(AM_NEQ, "udp", value);
    }

    @Test
    public void decodeIpProto() {
        JsonNode value = createJsonNode("udp");
        assertEquals(AM_NEQ, UDP, CodecUtils.decodeIpProto(value));
    }

    @Test
    public void encodeEthType() {
        String value = CodecUtils.encodeEthType(IPv4);
        assertEquals(AM_NEQ, "ipv4", value);
    }

    @Test
    public void decodeEthType() {
        JsonNode value = createJsonNode("ipv4");
        assertEquals(AM_NEQ, IPv4, CodecUtils.decodeEthType(value));
    }

    @Test
    public void encodeICMPV4Type() {
        String value = CodecUtils.encodeICMPv4Type(icmpv4Type(17));
        assertEquals(AM_NEQ, "addr_mask_req", value);
    }

    @Test
    public void decodeICMPV4Type() {
        JsonNode value = createJsonNode("addr_mask_req");
        assertEquals(AM_NEQ, icmpv4Type(17), CodecUtils.decodeICMPV4Type(value));
    }

    @Test
    public void encodeICMPV6Type() {
        String value = CodecUtils.encodeICMPv6Type(icmpv6Type(2));
        assertEquals(AM_NEQ, "pkt_big", value);
    }

    @Test
    public void decodeICMPV6Type() {
        JsonNode value = createJsonNode("pkt_big");
        assertEquals(AM_NEQ, icmpv6Type(2), CodecUtils.decodeICMPV6Type(value));
    }

    @Test
    public void encodeFlowModFlag() {
        String value = CodecUtils.encodeFlowModFlag(SEND_FLOW_REM);
        assertEquals(AM_NEQ, "send_flow_rem", value);
    }

    @Test
    public void decodeFlowModFlag() {
        JsonNode value = createJsonNode("send_flow_rem");
        assertEquals(AM_NEQ, SEND_FLOW_REM, CodecUtils.decodeFlowModFlag(value));
    }

    @Test
    public void encodeBufferId() {
        long value = CodecUtils.encodeBufferId(bid(7));
        assertEquals(AM_NEQ, 7, value);
    }

    @Test
    public void decodeBufferId() {
        JsonNode value = createJsonNode(7);
        assertEquals(AM_NEQ, bid(7), CodecUtils.decodeBufferId(value));
    }

    @Test
    public void encodeGroupId() {
        long value = CodecUtils.encodeGroupId(gid(8));
        assertEquals(AM_NEQ, 8, value);
    }

    @Test
    public void decodeGroupId() {
        JsonNode value = createJsonNode(8);
        assertEquals(AM_NEQ, gid(8), CodecUtils.decodeGroupId(value));
    }

    @Test
    public void encodeFlowModCmd() {
        String value = CodecUtils.encodeFlowModCmd(ADD);
        assertEquals(AM_NEQ, "add", value);
    }

    @Test
    public void decodeFlowModCmd() {
        JsonNode value = createJsonNode("add");
        assertEquals(AM_NEQ, ADD, CodecUtils.decodeFlowModCmd(value));
    }

    @Test
    public void encodeProtocolVersion() {
        String value = CodecUtils.encodeProtocolVersion(V_1_3);
        assertEquals(AM_NEQ, "1.3.0", value);
    }

    @Test
    public void decodeProtocolVersion() {
        JsonNode value = createJsonNode("1.3.0");
        assertEquals(AM_NEQ, V_1_3, CodecUtils.decodeProtocolVersion(value));
    }

    @Test
    public void encodeQueueId() {
        long value = CodecUtils.encodeQueueId(qid(5));
        assertEquals(AM_NEQ, 5, value);
    }

    @Test
    public void decodeQueueId() {
        JsonNode value = createJsonNode(5);
        assertEquals(AM_NEQ, qid(5), CodecUtils.decodeQueueId(value));
    }

    @Test
    public void encodeTableId() {
        long value = CodecUtils.encodeTableId(tid(9));
        assertEquals(AM_NEQ, 9, value);
    }

    @Test
    public void decodeTableId() {
        JsonNode value = createJsonNode(9);
        assertEquals(AM_NEQ, tid(9), CodecUtils.decodeTableId(value));
    }

    @Test
    public void encodeMeterId() {
        long value = CodecUtils.encodeMeterId(mid(3));
        assertEquals(AM_NEQ, 3, value);
    }

    @Test
    public void decodeMeterId() {
        JsonNode value = createJsonNode(3);
        assertEquals(AM_NEQ, mid(3), CodecUtils.decodeMeterId(value));
    }
}
