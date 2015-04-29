/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import java.math.BigInteger;
import java.util.StringTokenizer;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.Pbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public final class HashUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HashUtil.class);
    private static final int BASE_16 = 16;
    private static final int BASE_10 = 10;
    private static final long IPV6_TOKENS_COUNT = 8;
    public static final String IPV6_TOKEN = ":0000";

    private HashUtil() {

        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static long calculateEthernetMatchHash(EthernetMatch ethernetMatch) {
        long hash = 0;

        EthernetType ethernetType = ethernetMatch.getEthernetType();
        if (null != ethernetType) {
            hash += ethernetType.getType().getValue();
        }

        EthernetDestination ethernetDestination = ethernetMatch.getEthernetDestination();
        if (null != ethernetDestination) {
            hash += calculateEthernetDestinationHash(ethernetDestination);
        }

        EthernetSource ethernetSource = ethernetMatch.getEthernetSource();
        if (null != ethernetSource) {
            hash += calculateEthenetSourceHash(ethernetSource);
        }

        return hash;
    }

    public static long calculateEthenetSourceHash(EthernetSource ethernetSource) {
        long hash = calculateMacAddressHash(ethernetSource.getAddress());
        hash += calculateMacAddressHash(ethernetSource.getMask());
        return hash;
    }

    public static long calculateEthernetDestinationHash(EthernetDestination ethernetDestination) {
        long hash = calculateMacAddressHash(ethernetDestination.getAddress());
        hash += calculateMacAddressHash(ethernetDestination.getMask());
        return hash;
    }

    public static long calculateMacAddressHash(MacAddress macAddress) {

        long hash = 0;
        if (null != macAddress) {
            StringTokenizer stringTokenizer = new StringTokenizer(macAddress.getValue(), ":");
            hash = parseTokens(stringTokenizer, BASE_16, 8);
        }
        return hash;
    }

    public static long calculateMatchHash(final Match match, DeviceContext deviceContext) {
        long hash = 0;
        long subHash = 0;
        long base = 0;
        if (null != match) {
            if (null != match.getEthernetMatch()) {
                hash = 1 << base;
                subHash += calculateEthernetMatchHash(match.getEthernetMatch());
            }
            base++;
            if (null != match.getIcmpv4Match()) {
                hash = 1 << base;
                subHash += calculateIcmpV4MatchHash(match.getIcmpv4Match());
            }
            base++;
            if (null != match.getIcmpv6Match()) {
                hash = 1 << base;
                subHash += calculateIcmpV6MatchHash(match.getIcmpv6Match());
            }
            base++;
            if (null != match.getInPhyPort()) {
                hash = 1 << base;
                subHash += calculateNodeConnectorIdHash(match.getInPhyPort(), deviceContext);
            }
            base++;
            if (null != match.getInPort()) {
                hash = 1 << base;
                subHash += calculateNodeConnectorIdHash(match.getInPort(), deviceContext);
            }
            base++;
            if (null != match.getIpMatch()) {
                hash = 1 << base;
                subHash += calculateIpMatchHash(match.getIpMatch());
            }
            base++;
            if (null != match.getLayer3Match()) {
                hash = 1 << base;
                subHash += calculateLayer3MatchHash(match.getLayer3Match());
            }
            base++;
            if (null != match.getLayer4Match()) {
                hash = 1 << base;
                subHash += calculateLayer4MatchHash(match.getLayer4Match());
            }
            base++;
            if (null != match.getIcmpv6Match()) {
                hash = 1 << base;
                subHash += calculateIcmpv6MatchHash(match.getIcmpv6Match());
            }
            base++;
            if (null != match.getMetadata()) {
                hash = 1 << base;
                subHash += calculateMetadataHash(match.getMetadata());
            }
            base++;
            if (null != match.getProtocolMatchFields()) {
                hash = 1 << base;
                subHash += calculateProtocolMatchFieldsHash(match.getProtocolMatchFields());
            }
            base++;
            if (null != match.getTcpFlagMatch()) {
                hash = 1 << base;
                subHash += calculateTcpFlagMatch(match.getTcpFlagMatch());
            }
            base++;
            if (null != match.getVlanMatch()) {
                hash = 1 << base;
                subHash += calculateVlanMatchHash(match.getVlanMatch());
            }
            base++;
            if (null != match.getTunnel()) {
                hash = 1 << base;
                subHash += calculateTunnelHash(match.getTunnel());
            }
        }
        return hash + subHash;
    }

    private static long calculateTunnelHash(final Tunnel tunnel) {
        long hash = 0;
        BigInteger tunnelId = tunnel.getTunnelId();
        if (null != tunnelId) {
            hash += tunnelId.intValue();
        }

        BigInteger tunnelMask = tunnel.getTunnelMask();
        if (null != tunnelMask) {
            hash += tunnelMask.intValue();
        }
        return hash;
    }

    private static long calculateVlanMatchHash(final VlanMatch vlanMatch) {
        long hash = 0;

        VlanId vlanId = vlanMatch.getVlanId();
        if (null != vlanId) {
            hash += vlanId.getVlanId().getValue().intValue();
        }

        VlanPcp vlanPcp = vlanMatch.getVlanPcp();
        if (null != vlanPcp) {
            hash += vlanPcp.getValue().shortValue();
        }

        return hash;
    }

    private static long calculateTcpFlagMatch(final TcpFlagMatch tcpFlagMatch) {
        long hash = tcpFlagMatch.getTcpFlag().intValue();
        return hash;
    }

    private static long calculateProtocolMatchFieldsHash(final ProtocolMatchFields protocolMatchFields) {
        long hash = 0;
        Short mplsBos = protocolMatchFields.getMplsBos();
        if (null != mplsBos) {
            hash += mplsBos.intValue();
        }
        Short mplsTc = protocolMatchFields.getMplsTc();
        if (null != mplsTc) {
            hash += mplsTc.intValue();
        }
        Pbb pbb = protocolMatchFields.getPbb();
        if (null != pbb) {
            if (null != pbb.getPbbIsid()) {
                hash += pbb.getPbbIsid().intValue();
            }
            if (null != pbb.getPbbMask()) {
                hash += pbb.getPbbMask().intValue();
            }
        }
        Long mplsLabel = protocolMatchFields.getMplsLabel();
        if (null != mplsLabel) {
            hash += mplsLabel.intValue();
        }
        return hash;
    }

    private static long calculateMetadataHash(final Metadata metadata) {
        long hash = metadata.getMetadata().intValue();
        if (null != metadata.getMetadataMask()) {
            hash += metadata.getMetadataMask().intValue();
        }
        return hash;
    }

    private static long calculateIcmpv6MatchHash(final Icmpv6Match icmpv6Match) {
        long hash = icmpv6Match.getIcmpv6Code().intValue();
        hash += icmpv6Match.getIcmpv6Type().intValue();
        return hash;
    }

    private static long calculateLayer4MatchHash(final Layer4Match layer4Match) {
        long hash = 0;
        if (layer4Match instanceof SctpMatch) {
            hash += calculateSctpMatchHash((SctpMatch) layer4Match);
        }

        if (layer4Match instanceof TcpMatch) {
            hash += calculateTcpMatchHash((TcpMatch) layer4Match);
        }
        if (layer4Match instanceof UdpMatch) {
            hash += calculateUdpMatchHash((UdpMatch) layer4Match);
        }
        return hash;
    }

    private static long calculateUdpMatchHash(final UdpMatch layer4Match) {
        long hash = 0;
        return hash;
    }

    private static long calculateTcpMatchHash(final TcpMatch layer4Match) {
        long hash = 0;
        PortNumber sourcePort = layer4Match.getTcpSourcePort();
        if (null != sourcePort) {
            hash += sourcePort.getValue().intValue();
        }

        PortNumber destinationPort = layer4Match.getTcpDestinationPort();
        if (null != destinationPort) {
            hash += destinationPort.getValue().intValue();
        }
        return hash;
    }

    private static long calculateSctpMatchHash(final SctpMatch layer4Match) {
        long hash = 0;

        PortNumber portNumber = layer4Match.getSctpDestinationPort();
        if (null != portNumber) {
            hash += portNumber.getValue().intValue();
        }

        PortNumber sourcePort = layer4Match.getSctpSourcePort();
        if (null != sourcePort) {
            hash += sourcePort.getValue().intValue();
        }
        return hash;
    }

    private static long calculateLayer3MatchHash(final Layer3Match layer3Match) {
        long hash = 0;
        if (layer3Match instanceof ArpMatch) {
            hash += calculateArpMatchHash((ArpMatch) layer3Match);
        }
        if (layer3Match instanceof Ipv4Match) {
            hash += calculateIpv4MatchHash((Ipv4Match) layer3Match);
        }
        if (layer3Match instanceof Ipv6Match) {
            hash += calculateIpv6MatchHash((Ipv6Match) layer3Match);

        }
        if (layer3Match instanceof TunnelIpv4Match) {
            hash += calculateTunnelIpv4Hash((TunnelIpv4Match) layer3Match);
        }
        return hash;
    }

    private static long calculateTunnelIpv4Hash(final TunnelIpv4Match layer3Match) {
        Ipv4Prefix tunnelIpv4Destination = layer3Match.getTunnelIpv4Destination();
        long hash = calculateIpv4PrefixHash(tunnelIpv4Destination);
        Ipv4Prefix tunnelIpv4Source = layer3Match.getTunnelIpv4Source();
        hash += calculateIpv4PrefixHash(tunnelIpv4Source);
        return hash;
    }

    private static long calculateIpv6MatchHash(final Ipv6Match layer3Match) {
        long hash = 0;
        Ipv6Prefix ipv6Destination = layer3Match.getIpv6Destination();
        if (null != ipv6Destination) {
            hash += calculateIpv6PrefixHash(ipv6Destination);
        }

        if (null != layer3Match.getIpv6Source()) {
            hash += calculateIpv6PrefixHash(layer3Match.getIpv6Source());
        }

        if (null != layer3Match.getIpv6ExtHeader()) {
            hash += layer3Match.getIpv6ExtHeader().getIpv6Exthdr();
            hash += layer3Match.getIpv6ExtHeader().getIpv6ExthdrMask();
        }

        if (null != layer3Match.getIpv6NdSll()) {
            hash += calculateMacAddressHash(layer3Match.getIpv6NdSll());
        }
        if (null != layer3Match.getIpv6NdTll()) {
            hash += calculateMacAddressHash(layer3Match.getIpv6NdTll());
        }
        if (null != layer3Match.getIpv6NdTarget()) {
            hash += calculateIpv6AddressHash(layer3Match.getIpv6NdTarget());
        }
        return hash;
    }


    public static long calculateIpv6PrefixHash(final Ipv6Prefix ipv6Prefix) {

        StringTokenizer stringTokenizer = getStringTokenizerWithFullAddressString(ipv6Prefix.getValue());

        long hash = parseTokens(stringTokenizer, BASE_16, 16);
        return hash;
    }

    public static long calculateIpv6AddressHash(final Ipv6Address ipv6Address) {

        StringTokenizer stringTokenizer = getStringTokenizerWithFullAddressString(ipv6Address.getValue());

        long hash = parseTokens(stringTokenizer, BASE_16, 16);
        return hash;
    }

    private static StringTokenizer getStringTokenizerWithFullAddressString(String value) {
        String ipv6Value = value.replace("::", ":0000:");
        StringTokenizer stringTokenizer = new StringTokenizer(ipv6Value, ":");

        long delta = IPV6_TOKENS_COUNT - stringTokenizer.countTokens();

        StringBuffer additions = new StringBuffer();

        if (delta > 0) {
            while (delta > 0) {
                additions.append(IPV6_TOKEN);
                delta--;
            }
            if (ipv6Value.contains("/")) {
                ipv6Value = ipv6Value.replace("/", additions.toString() + "/");
            } else {
                ipv6Value += additions.toString();
            }
            stringTokenizer = new StringTokenizer(ipv6Value, ":");
        }
        return stringTokenizer;
    }

    private static long calculateStopperBasedOnMaskValue(final Ipv6Prefix ipv6Prefix, long bitsBase) {
        double maskValue = extractMask(ipv6Prefix);
        double bitCount = maskValue / bitsBase;
        return (int) Math.ceil(bitCount);
    }

    private static long extractMask(final Ipv6Prefix ipv6Prefix) {
        StringTokenizer maskTokenizer = new StringTokenizer(ipv6Prefix.getValue(), "/");
        long mask = 0;
        if (maskTokenizer.countTokens() > 1) {
            maskTokenizer.nextToken();
            mask = Integer.parseInt(maskTokenizer.nextToken());
        }
        return mask;
    }

    private static long parseTokens(final StringTokenizer stringTokenizer, int base, int bitShift) {
        return parseTokens(stringTokenizer, 0, base, bitShift);
    }

    private static long parseTokens(final StringTokenizer stringTokenizer, long stopper, int base, int bitShift) {
        long hash = 0;
        if (stringTokenizer.countTokens() > 0) {
            long step = 0;
            while (stringTokenizer.hasMoreTokens()) {
                String token = stringTokenizer.nextToken();
                step++;

                if (token.equals("")) {
                    token = "0";
                }

                if (token.contains("/")) {
                    StringTokenizer tokenizer = new StringTokenizer(token, "/");
                    hash += parseTokens(tokenizer, stopper, base, bitShift);
                } else {
                    hash += Long.parseLong(token, base) << (bitShift * step);
                    if (stopper > 0 && step == stopper) {
                        break;
                    }
                }
            }
        }
        return hash;
    }

    private static long calculateIpv4MatchHash(final Ipv4Match layer3Match) {
        long hash = 0;
        Ipv4Prefix ipv4Destination = layer3Match.getIpv4Destination();
        if (null != ipv4Destination) {
            hash += calculateIpv4PrefixHash(ipv4Destination);
        }

        Ipv4Prefix ipv4Source = layer3Match.getIpv4Source();

        if (null != ipv4Source) {
            hash += calculateIpv4PrefixHash(ipv4Source);
        }

        //TODO : add calculation of hashes for augmentations
        return hash;
    }

    private static long calculateArpMatchHash(final ArpMatch layer3Match) {
        long hash = 0;
        Integer arpOp = layer3Match.getArpOp();
        if (null != arpOp) {
            hash += arpOp.intValue();
        }
        ArpSourceHardwareAddress arpSourceHardwareAddress = layer3Match.getArpSourceHardwareAddress();
        if (null != arpSourceHardwareAddress) {
            hash += calculateMacAddressHash(arpSourceHardwareAddress.getAddress());
            hash += calculateMacAddressHash(arpSourceHardwareAddress.getMask());
        }

        Ipv4Prefix sourceTransportAddress = layer3Match.getArpSourceTransportAddress();
        if (null != sourceTransportAddress) {
            hash += calculateIpv4PrefixHash(sourceTransportAddress);
        }

        ArpTargetHardwareAddress arpTargetHardwareAddress = layer3Match.getArpTargetHardwareAddress();
        if (null != arpTargetHardwareAddress) {
            hash += calculateMacAddressHash(arpTargetHardwareAddress.getAddress());
            hash += calculateMacAddressHash(arpTargetHardwareAddress.getMask());
        }

        Ipv4Prefix targetTransportAddress = layer3Match.getArpTargetTransportAddress();
        if (null != targetTransportAddress) {
            hash += calculateIpv4PrefixHash(targetTransportAddress);
        }

        return hash;
    }

    public static long calculateIpv4PrefixHash(final Ipv4Prefix ipv4Prefix) {
        long hash = 0;
        StringTokenizer prefixAsArray = new StringTokenizer(ipv4Prefix.getValue(), "/");
        if (prefixAsArray.countTokens() == 2) {
            String address = prefixAsArray.nextToken();
            Long mask = Long.parseLong(prefixAsArray.nextToken());
            long numberOfAddressPartsToUse = (int) Math.ceil(mask.doubleValue() / 8);
            hash += calculateIpAdressHash(address, numberOfAddressPartsToUse, BASE_10);
            hash += mask.shortValue();
        }
        return hash;
    }


    private static long calculateIpAdressHash(final String address, long numberOfParts, int base) {
        StringTokenizer stringTokenizer = new StringTokenizer(address, ".");
        long hash = parseTokens(stringTokenizer, numberOfParts, base, 8);
        return hash;
    }

    private static long calculateIpMatchHash(final IpMatch ipMatch) {
        long hash = 0;
        Short ipEcn = ipMatch.getIpEcn();
        if (null != ipEcn) {
            hash += ipEcn.shortValue();
        }
        Short ipProtocol = ipMatch.getIpProtocol();
        if (null != ipProtocol) {
            hash += ipProtocol;
        }

        Short ipDscp = ipMatch.getIpDscp().getValue();
        if (null != ipDscp) {
            hash += ipDscp;
        }

        IpVersion ipVersion = ipMatch.getIpProto();
        if (null != ipVersion) {
            hash += ipVersion.getIntValue();
        }
        return hash;
    }

    private static long calculateNodeConnectorIdHash(final NodeConnectorId inPhyPort, DeviceContext deviceContext) {
        long hash = 0;
        short version = deviceContext.getDeviceState().getVersion();
        Long portFromLogicalName = OpenflowPortsUtil.getPortFromLogicalName(OpenflowVersion.get(version), inPhyPort.getValue());
        hash += portFromLogicalName.intValue();
        return hash;
    }

    private static long calculateIcmpV6MatchHash(final Icmpv6Match icmpv6Match) {
        long hash = 0;
        if (null != icmpv6Match.getIcmpv6Code()) {
            hash += icmpv6Match.getIcmpv6Code();
        }
        if (null != icmpv6Match.getIcmpv6Type()) {
            hash += icmpv6Match.getIcmpv6Type();
        }
        return hash;
    }

    public static long calculateIcmpV4MatchHash(final Icmpv4Match icmpv4Match) {
        long hash = 0;
        if (null != icmpv4Match.getIcmpv4Code()) {
            hash += icmpv4Match.getIcmpv4Code();
        }
        if (null != icmpv4Match.getIcmpv4Type()) {
            hash += icmpv4Match.getIcmpv4Type();
        }
        return hash;
    }


}
