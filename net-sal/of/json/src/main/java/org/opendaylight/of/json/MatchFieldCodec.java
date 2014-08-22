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
import org.opendaylight.of.lib.match.*;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.VlanId;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static org.opendaylight.of.json.CodecUtils.*;
import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.util.JSONUtils.fromKey;
import static org.opendaylight.util.JSONUtils.toKey;

/**
 * A JSON codec capable of encoding and decoding {@link MatchField} objects.
 *
 * @author Shaila Shree
 * @author Simon Hunt
 */
public class MatchFieldCodec extends OfJsonCodec<MatchField> {
    // unit test access
    static final String ROOTS = "flow_match_fields";
    static final String ROOT = "flow_match_field";

    private static final String MASK = "mask";

    /**
     * Constructs a MatchField Codec.
     */
    protected MatchFieldCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(MatchField mf) {
        ObjectNode node = null;
        OxmBasicFieldType type = (OxmBasicFieldType) mf.getFieldType();
        String name = toKey(type);

        switch (type) {
            case IN_PORT:
            case IN_PHY_PORT:
                node = encodeBigPort(name, mf);
                break;

            case METADATA:
            case TUNNEL_ID:
                node = encodeLong(name, mf);
                break;

            case ETH_DST:
            case ETH_SRC:
            case ARP_SHA:
            case ARP_THA:
            case IPV6_ND_SLL:
            case IPV6_ND_TLL:
                node = encodeMacAddr(name, mf);
                break;

            case ETH_TYPE:
                node = encodeEth(name, mf);
                break;

            case VLAN_VID:
                node = encodeVlan(name, mf);
                break;

            case VLAN_PCP:
            case IP_DSCP:
            case IP_ECN:
            case ICMPV4_CODE:
            case ICMPV6_CODE:
            case MPLS_TC:
            case MPLS_BOS:
            case ARP_OP:
                node = encodeInt(name, mf);
                break;

            case IPV6_FLABEL:
            case MPLS_LABEL:
            case PBB_ISID:
                node = encodeHex(name, mf);
                break;

            case IP_PROTO:
                node = encodeIpProtocol(name, mf);
                break;

            case IPV4_SRC:
            case IPV4_DST:
            case ARP_SPA:
            case ARP_TPA:
            case IPV6_SRC:
            case IPV6_DST:
            case IPV6_ND_TARGET:
                node = encodeIP(name, mf);
                break;

            case TCP_SRC:
            case TCP_DST:
            case UDP_SRC:
            case UDP_DST:
            case SCTP_SRC:
            case SCTP_DST:
                node = encodePort(name, mf);
                break;

            case ICMPV4_TYPE:
                node = encodeICMPv4(name, mf);
                break;

            case ICMPV6_TYPE:
                node = encodeICMPv6(name, mf);
                break;

            case IPV6_EXTHDR:
                node = encodeIPV6ExtHdr(name, mf);
                break;

            default:
                break;
        }
        return node;
    }

    private ObjectNode encodeBigPort(String name, MatchField mf) {
        MFieldBasicBigPort bp = (MFieldBasicBigPort) mf;
        return CodecUtils.encodeBigPort(objectNode(), name, bp.getPort(),                bp.getVersion());
    }

    private ObjectNode encodePort(String name, MatchField mf) {
        MFieldBasicPort bp = (MFieldBasicPort) mf;
        return objectNode().put(name, CodecUtils.encodePort(bp.getPort()));
    }

    private ObjectNode encodeLong(String name, MatchField mf) {
        MFieldBasicLong basicLong = (MFieldBasicLong) mf;
        ObjectNode node = objectNode().put(name, hex(basicLong.getValue()));
        if (basicLong.hasMask())
            node.put(MASK, hex(basicLong.getMask()));
        return node;
    }

    private ObjectNode encodeMacAddr(String name, MatchField mf) {
        MFieldBasicMac mac = (MFieldBasicMac) mf;
        ObjectNode node = objectNode().put(name, encodeMac(mac.getMacAddress()));
        if (mac.hasMask())
            node.put(MASK, encodeMac(mac.getMask()));
        return node;
    }

    private ObjectNode encodeInt(String name, MatchField mf) {
        MFieldBasicInt basicInt = (MFieldBasicInt) mf;
        ObjectNode node = objectNode().put(name, basicInt.getValue());
        if (basicInt.hasMask())
            node.put(MASK, hex(basicInt.getMask()));
        return node;
    }

    private ObjectNode encodeHex(String name, MatchField mf) {
        MFieldBasicInt basicInt = (MFieldBasicInt) mf;
        ObjectNode node = objectNode().put(name, hex(basicInt.getValue()));
        if (basicInt.hasMask())
            node.put(MASK, hex(basicInt.getMask()));
        return node;
    }

    private ObjectNode encodeIP(String name, MatchField mf) {
        MFieldBasicIp ip = (MFieldBasicIp) mf;
        ObjectNode node = objectNode().put(name, encodeIp(ip.getIpAddress()));
        if (mf.hasMask())
            node.put(MASK, encodeIp(ip.getMask()));
        return node;
    }

    private ObjectNode encodeEth(String name, MatchField mf) {
        MfbEthType type = (MfbEthType) mf;
        return objectNode().put(name, encodeEthType(type.getEthernetType()));
    }

    private ObjectNode encodeVlan(String name, MatchField mf) {
        MfbVlanVid vlanVid = (MfbVlanVid) mf;
        VlanId vlanId = vlanVid.getVlanId();
        ObjectNode node = objectNode();
        if (vlanId.equals(VlanId.NONE) || vlanId.equals(VlanId.PRESENT))
            node.put(name, vlanId.toString().toLowerCase(Locale.getDefault()));
        else
            node.put(name, vlanId.toInt());
        return node;
    }

    private ObjectNode encodeIpProtocol(String name, MatchField mf) {
        MfbIpProto proto = (MfbIpProto) mf;
        return objectNode().put(name, encodeIpProto(proto.getIpProtocol()));
    }

    private ObjectNode encodeICMPv4(String name, MatchField mf) {
        MfbIcmpv4Type type = (MfbIcmpv4Type) mf;
        return objectNode().put(name, encodeICMPv4Type(type.getICMPv4Type()));
    }

    private ObjectNode encodeICMPv6(String name, MatchField mf) {
        MfbIcmpv6Type type = (MfbIcmpv6Type) mf;
        return objectNode().put(name, encodeICMPv6Type(type.getICMPv6Type()));
    }

    private ObjectNode encodeIPV6ExtHdr(String name, MatchField mf) {
        MfbIpv6Exthdr ipv6Exthdr = (MfbIpv6Exthdr) mf;
        ObjectNode oNode = objectNode();
        Map<IPv6ExtHdr, Boolean> flags = ipv6Exthdr.getFlags();
        for (Map.Entry<IPv6ExtHdr, Boolean> entry : flags.entrySet())
            oNode.put(toKey(entry.getKey()), entry.getValue());
        ObjectNode node = objectNode();
        node.put(name, oNode);
        return node;
    }

    @Override
    public MatchField decode(ObjectNode node) {
        String field = null;
        JsonNode version = null;
        JsonNode value = null;
        JsonNode mask = null;

        Iterator<Map.Entry<String, JsonNode>> nodes = node.fields();

        while (nodes.hasNext()) {
            Map.Entry<String, JsonNode> entry = nodes.next();
            if (entry.getKey().equals(VERSION)) {
                version = entry.getValue();
            } else if (entry.getKey().equals(MASK)) {
                mask = entry.getValue();
            } else {
                field = entry.getKey();
                value = entry.getValue();
            }
        }

        ProtocolVersion pv  = decodeProtocolVersion(version);
        OxmBasicFieldType type = fromKey(OxmBasicFieldType.class, field);

        MatchField mf = null;

        switch (type) {
            case IN_PORT:
            case IN_PHY_PORT:
                mf = decodeBigPort(pv, type, value);
                break;

            case METADATA:
            case TUNNEL_ID:
                mf = decodeLong(pv, type, value, mask);
                break;

            case ETH_DST:
            case ETH_SRC:
            case ARP_SHA:
            case ARP_THA:
            case IPV6_ND_SLL:
            case IPV6_ND_TLL:
                mf = decodeMacAddr(pv, type, value, mask);
                break;

            case ETH_TYPE:
                mf = decodeEth(pv, type, value);
                break;

            case VLAN_VID:
                mf = decodeVlan(pv, type, value);
                break;

            case VLAN_PCP:
            case IP_DSCP:
            case IP_ECN:
            case ICMPV4_CODE:
            case ICMPV6_CODE:
            case MPLS_TC:
            case MPLS_BOS:
            case ARP_OP:
                mf = decodeInt(pv, type, value, mask);
                break;

            case IPV6_FLABEL:
            case MPLS_LABEL:
            case PBB_ISID:
                mf = decodeHex(pv, type, value, mask);
                break;

            case IP_PROTO:
                mf = decodeIpProtocol(pv, type, value);
                break;

            case IPV4_SRC:
            case IPV4_DST:
            case ARP_SPA:
            case ARP_TPA:
            case IPV6_SRC:
            case IPV6_DST:
            case IPV6_ND_TARGET:
                mf = decodeIP(pv, type, value, mask);
                break;

            case TCP_SRC:
            case TCP_DST:
            case UDP_SRC:
            case UDP_DST:
            case SCTP_SRC:
            case SCTP_DST:
                mf = decodePort(pv, type, value);
                break;

            case ICMPV4_TYPE:
                mf = decodeICMPv4Type(pv, type, value);
                break;

            case ICMPV6_TYPE:
                mf = decodeICMP6Type(pv, type, value);
                break;

            case IPV6_EXTHDR:
                mf = decodeIPV6ExtHdr(pv, type, value);
                break;

            default:
                break;
        }
        return mf;
    }

    private MatchField decodeBigPort(ProtocolVersion pv, OxmBasicFieldType type,
                                     JsonNode value) {
        return createBasicField(pv, type, CodecUtils.decodeBigPort(value));
    }

    private MatchField decodeLong(ProtocolVersion pv, OxmBasicFieldType type,
                                  JsonNode value, JsonNode mask) {
        long mVal = mask != null ? parseHexLong(mask.asText()) : 0;
        return createBasicField(pv, type, parseHexLong(value.asText()), mVal);
    }

    private MatchField decodeMacAddr(ProtocolVersion pv, OxmBasicFieldType type,
                                     JsonNode value, JsonNode mask) {
        MacAddress maskMac = mask != null ? decodeMac(mask) : null;
        return createBasicField(pv, type, decodeMac(value), maskMac);
    }

    private MatchField decodeInt(ProtocolVersion pv, OxmBasicFieldType type,
                                 JsonNode value, JsonNode mask) {
        int mVal = mask != null ? parseHexInt(mask.asText()) : 0;
        return createBasicField(pv, type, value.asInt(), mVal);
    }

    private MatchField decodeHex(ProtocolVersion pv, OxmBasicFieldType type,
                                 JsonNode value, JsonNode mask) {
        int val = parseHexInt(value.asText());
        int mVal = mask != null ? parseHexInt(mask.asText()) : 0;
        return createBasicField(pv, type, val, mVal);
    }

    private MatchField decodeIP(ProtocolVersion pv, OxmBasicFieldType type,
                                JsonNode value, JsonNode mask) {
        IpAddress maskIp = mask != null ? decodeIp(mask) : null;
        return createBasicField(pv, type, decodeIp(value), maskIp);
    }

    private MatchField decodeEth(ProtocolVersion pv, OxmBasicFieldType type,
                                 JsonNode value) {
        return createBasicField(pv, type, decodeEthType(value));
    }

    private MatchField decodeVlan(ProtocolVersion pv, OxmBasicFieldType type,
                                  JsonNode value) {
        return createBasicField(pv, type, VlanId.valueOf(value.asText()));
    }

    private MatchField decodeIpProtocol(ProtocolVersion pv,
                                        OxmBasicFieldType type,
                                        JsonNode value) {
        return createBasicField(pv, type, decodeIpProto(value));
    }

    private MatchField decodePort(ProtocolVersion pv, OxmBasicFieldType type,
                                  JsonNode value) {
        return createBasicField(pv, type, CodecUtils.decodePort(value));
    }

    private MatchField decodeICMPv4Type(ProtocolVersion pv,
                                        OxmBasicFieldType type, JsonNode value) {
        return createBasicField(pv, type, decodeICMPV4Type(value));
    }

    private MatchField decodeICMP6Type(ProtocolVersion pv,
                                       OxmBasicFieldType type, JsonNode value) {
        return createBasicField(pv, type, decodeICMPV6Type(value));
    }

    private MatchField decodeIPV6ExtHdr(ProtocolVersion pv,
                                        OxmBasicFieldType type, JsonNode flags) {
        Map<IPv6ExtHdr, Boolean> map = new HashMap<>();

        Iterator<String> iterator = flags.fieldNames();
        while (iterator.hasNext()) {
            String key = iterator.next();
            map.put(fromKey(IPv6ExtHdr.class, key), flags.get(key).booleanValue());
        }
        return createBasicField(pv, type, map);
    }
}
