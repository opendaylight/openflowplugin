/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.*;
import org.opendaylight.of.lib.msg.FlowModCommand;
import org.opendaylight.of.lib.msg.FlowModFlag;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.JSONUtils;
import org.opendaylight.util.net.*;

import static org.opendaylight.util.JSONUtils.fromKey;
import static org.opendaylight.util.JSONUtils.toKey;

/**
 * A JSON codec capable of encoding and decoding commonly used open flow data
 * objects.
 *
 * @author Shaila Shree
 * @author Simon Hunt
 */
public class CodecUtils {

    /**
     * Encodes the given big port number (for the given protocol version) as
     * either an integer or a string (for reserved logical ports), and adds
     * that value to the given JSON node with the given key.
     *
     * @param node the JSON node
     * @param key the JSON property key
     * @param bpn the big port number
     * @param pv the protocol version
     * @return the supplied object node
     */
    public static ObjectNode encodeBigPort(ObjectNode node, String key,
                                           BigPortNumber bpn,
                                           ProtocolVersion pv) {
        String special = Port.logicalName(bpn, pv);
        if (special != null)
            node.put(key, special);
        else
            node.put(key, bpn.toLong());
        return node;
    }

    /**
     * Converts JsonNode into BigPortNumber object.
     *
     * @param value the JsonNode
     * @return the BigPortNumber
     */
    public static BigPortNumber decodeBigPort(JsonNode value) {
        return Port.getBigPortNumber(value.asText());
    }

    /**
     * Converts PortNumber into int that can be used as a JSON value.
     *
     * @param port the PortNumber
     * @return the int representation of PortNumber
     */
    public static int encodePort(PortNumber port) {
        return port.toInt();
    }

    /**
     * Converts JsonNode into PortNumber object.
     *
     * @param value the JsonNode
     * @return the PortNumber
     */
    public static PortNumber decodePort(JsonNode value) {
        return Port.getPortNumber(value.asText());
    }

    /**
     * Converts MacAddress into String that can be used as a JSON value.
     *
     * @param mac the MacAddress
     * @return the String representation of MacAddress
     */
    public static String encodeMac(MacAddress mac) {
        return mac.toString();
    }

    /**
     * Converts JsonNode into MacAddress object.
     *
     * @param value the JsonNode
     * @return the MacAddress
     */
    public static MacAddress decodeMac(JsonNode value) {
        return MacAddress.valueOf(value.textValue());
    }

    /**
     * Converts IpAddress into String.
     *
     * @param ip the IpAddress
     * @return the String representation of IpAddress
     */
    public static String encodeIp(IpAddress ip) {
        return ip.toString();
    }

    /**
     * Converts JsonNode into IpAddress object.
     *
     * @param value the JsonNode
     * @return the IpAddress
     */
    public static IpAddress decodeIp(JsonNode value) {
        return IpAddress.valueOf(value.textValue());
    }

    /**
     * Converts IpProtocol into String.
     *
     * @param ipProto the IpProtocol
     * @return the String representation of IpProtocol
     */
    public static String encodeIpProto(IpProtocol ipProto) {
        return ipProto.getShortName().toLowerCase();
    }

    /**
     * Converts JsonNode into IpProtocol object.
     *
     * @param value the JsonNode
     * @return the IpProtocol
     */
    public static IpProtocol decodeIpProto(JsonNode value) {
        return IpProtocol.valueOf(value.textValue());
    }

    /**
     * Converts EthernetType into String.
     *
     * @param ethType the EthernetType
     * @return the String representation of EthernetType
     */
    public static String encodeEthType(EthernetType ethType) {
        return ethType.getShortName().toLowerCase();
    }

    /**
     * Converts JsonNode into EthernetType object.
     *
     * @param value the JsonNode
     * @return the EthernetType
     */
    public static EthernetType decodeEthType(JsonNode value) {
        return EthernetType.valueOf(value.textValue());
    }

    /**
     * Converts ICMPv4Type into String that can be used as a JSON value.
     *
     * @param type the ICMPv4Type
     * @return the String representation of ICMPv4Type
     */
    public static String encodeICMPv4Type(ICMPv4Type type) {
        return type.getName().toLowerCase();
    }

    /**
     * Converts JsonNode into ICMPv4Type object.
     *
     * @param value the JsonNode
     * @return the ICMPv4Type
     */
    public static ICMPv4Type decodeICMPV4Type(JsonNode value) {
        return ICMPv4Type.valueOf(value.asText());
    }

    /**
     * Converts ICMPv6Type into String that can be used as a JSON value.
     *
     * @param type the ICMPv6Type
     * @return the String representation of ICMPv6Type
     */
    public static String encodeICMPv6Type(ICMPv6Type type) {
        return type.getName().toLowerCase();
    }

    /**
     * Converts JsonNode into ICMPv6Type object.
     *
     * @param value the JsonNode
     * @return the ICMPv6Type
     */
    public static ICMPv6Type decodeICMPV6Type(JsonNode value) {
        return ICMPv6Type.valueOf(value.asText());
    }

    /**
     * Converts FlowModFlag into String that can be used as a JSON value.
     *
     * @param flowModFlag the FlowModFlag
     * @return the String representation of FlowModFlag
     */
    public static String encodeFlowModFlag(FlowModFlag flowModFlag) {
        return toKey(flowModFlag);
    }

    /**
     * Converts JsonNode into FlowModFlag object.
     *
     * @param value the JsonNode
     * @return the FlowModFlag
     */
    public static FlowModFlag decodeFlowModFlag(JsonNode value) {
        return fromKey(FlowModFlag.class, value.asText());
    }

    /**
     * Converts BufferId into String that can be used as a JSON value.
     *
     * @param bufferId the BufferId
     * @return the long representation of BufferId
     */
    public static long encodeBufferId(BufferId bufferId) {
        return bufferId.toLong();
    }

    /**
     * Converts JsonNode into BufferId object.
     *
     * @param value the JsonNode
     * @return the BufferId
     */
    public static BufferId decodeBufferId(JsonNode value) {
        return (value == null) ? BufferId.NO_BUFFER : BufferId.valueOf(value
            .asLong());
    }

    /**
     * Converts GroupId into String that can be used as a JSON value.
     *
     * @param groupId the GroupId
     * @return the long representation of GroupId
     */
    public static long encodeGroupId(GroupId groupId) {
        return groupId.toLong();
    }

    /**
     * Converts JsonNode into GroupId object.
     *
     * @param value the JsonNode
     * @return the GroupId
     */
    public static GroupId decodeGroupId(JsonNode value) {
        return GroupId.valueOf(value.asLong());
    }

    /**
     * Converts FlowModCommand into String that can be used as a JSON value.
     *
     * @param flowModCmd the FlowModCommand
     * @return the String representation of FlowModCommand
     */
    public static String encodeFlowModCmd(FlowModCommand flowModCmd) {
        return JSONUtils.toKey(flowModCmd);
    }

    /**
     * Converts JsonNode into FlowModCommand object.
     *
     * @param value the JsonNode
     * @return the FlowModCommand
     */
    public static FlowModCommand decodeFlowModCmd(JsonNode value) {
        return JSONUtils.fromKey(FlowModCommand.class, value.asText());
    }

    /**
     * Converts ProtocolVersion into String that can be used as a JSON value.
     *
     * @param pv the ProtocolVersion
     * @return the String representation of ProtocolVersion
     */
    public static String encodeProtocolVersion(ProtocolVersion pv) {
        return pv.toDisplayString();
    }

    /**
     * Converts JsonNode into ProtocolVersion object.
     *
     * @param value the JsonNode
     * @return the ProtocolVersion
     */
    public static ProtocolVersion decodeProtocolVersion(JsonNode value) {
        return ProtocolVersion.fromString(value.asText());
    }

    /**
     * Converts QueueId into String that can be used as a JSON value.
     *
     * @param id the QueueId
     * @return the long representation of QueueId
     */
    public static long encodeQueueId(QueueId id) {
        return id.toLong();
    }

    /**
     * Converts JsonNode into QueueId object.
     *
     * @param value the JsonNode
     * @return the QueueId
     */
    public static QueueId decodeQueueId(JsonNode value) {
        return QueueId.valueOf(value.asLong());
    }

    /**
     * Converts TableId into String that can be used as a JSON value.
     *
     * @param id the TableId
     * @return the int representation of TableId
     */
    public static int encodeTableId(TableId id) {
        return id.toInt();
    }

    /**
     * Converts JsonNode into TableId object.
     *
     * @param value the JsonNode
     * @return the TableId
     */
    public static TableId decodeTableId(JsonNode value) {
        return TableId.valueOf(value.asInt());
    }

    /**
     * Converts MeterId into String that can be used as a JSON value.
     *
     * @param id the MeterId
     * @return the long representation of MeterId
     */
    public static long encodeMeterId(MeterId id) {
        return id.toLong();
    }

    /**
     * Converts JsonNode into MeterId object.
     *
     * @param value the JsonNode
     * @return the MeterId
     */
    public static MeterId decodeMeterId(JsonNode value) {
        return MeterId.valueOf(value.asLong());
    }
}
