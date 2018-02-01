/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.ActionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;

/**
 * The type Match converter v 10.
 */
public class MatchV10Convertor extends Convertor<Match, MatchV10, VersionConvertorData> {

    private static final List<Class<?>> TYPES = Arrays.asList(
            Match.class,
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match.class,
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.mod.removed.Match.class,
            org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match.class,
            org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.in.message.Match.class,
            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature
                .prop.type.Match.class,
            SetField.class);

    /**
     * default MAC.
     */
    private static final MacAddress ZERO_MAC = new MacAddress("00:00:00:00:00:00");

    /**
     * default IPv4.
     */
    private static final Ipv4Address ZERO_IPV4 = new Ipv4Address("0.0.0.0");

    /*
     * The value 0xffff (OFP_VLAN_NONE) is used to indicate
     * that no VLAN ID is set for OF Flow.
     */
    private static final Integer OFP_VLAN_NONE = 0xffff;

    private static boolean convertL4UdpDstMatch(final MatchV10Builder matchBuilder,
                                                final UdpMatch udpMatch) {
        if (udpMatch.getUdpDestinationPort() != null) {
            matchBuilder.setTpDst(udpMatch.getUdpDestinationPort().getValue());
            return false;
        }
        return true;
    }

    private static boolean convertL4UdpSrcMatch(final MatchV10Builder matchBuilder,
                                                final UdpMatch udpMatch) {
        if (udpMatch.getUdpSourcePort() != null) {
            matchBuilder.setTpSrc(udpMatch.getUdpSourcePort().getValue());
            return false;
        }
        return true;
    }

    private static boolean convertL4TpDstMatch(final MatchV10Builder matchBuilder,
                                               final TcpMatch tcpMatch) {
        if (tcpMatch.getTcpDestinationPort() != null) {
            matchBuilder.setTpDst(tcpMatch.getTcpDestinationPort().getValue());
            return false;
        }
        return true;
    }

    private static boolean convertL4TpSrcMatch(final MatchV10Builder matchBuilder,
                                               final TcpMatch tcpMatch) {
        if (tcpMatch.getTcpSourcePort() != null) {
            matchBuilder.setTpSrc(tcpMatch.getTcpSourcePort().getValue());
            return false;
        }
        return true;
    }

    private static boolean convertNwTos(final MatchV10Builder matchBuilder,
                                        final IpMatch ipMatch) {
        if (ipMatch.getIpDscp() != null) {
            matchBuilder.setNwTos(ActionUtil.dscpToTos(ipMatch.getIpDscp().getValue()));
            return false;
        }
        return true;
    }

    private static boolean convertNwProto(final MatchV10Builder matchBuilder, final IpMatch ipMatch) {
        if (ipMatch.getIpProtocol() != null) {
            matchBuilder.setNwProto(ipMatch.getIpProtocol());
            return false;
        }
        return true;
    }

    /**
     * Method splits the IP address and its mask and set their respective values in MatchV10Builder instance.
     * Wildcard value of the IP mask will be determined by Openflow java encoding library.
     *
     * @param matchBuilder match builder
     * @param ipv4         ip v4 match
     */
    private static void convertL3Ipv4DstMatch(final MatchV10Builder matchBuilder,
                                              final Ipv4Match ipv4) {
        if (ipv4.getIpv4Destination() != null) {
            Iterator<String> addressParts = IpConversionUtil.PREFIX_SPLITTER.split(
                    ipv4.getIpv4Destination().getValue()).iterator();
            Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
            Integer prefix = buildPrefix(addressParts);
            matchBuilder.setNwDst(ipv4Address);
            matchBuilder.setNwDstMask(prefix.shortValue());
        }
    }

    /**
     * Method splits the IP address and its mask and set their respective values in MatchV10Builder instance.
     * Wildcard value of the IP mask will be determined by Openflow java encoding library.
     *
     * @param matchBuilder match builder
     * @param ipv4         ip v4 match
     */
    private static void convertL3Ipv4SrcMatch(final MatchV10Builder matchBuilder,
                                              final Ipv4Match ipv4) {
        if (ipv4.getIpv4Source() != null) {
            Iterator<String> addressParts = IpConversionUtil.PREFIX_SPLITTER.split(
                    ipv4.getIpv4Source().getValue()).iterator();
            Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
            int prefix = buildPrefix(addressParts);

            matchBuilder.setNwSrc(ipv4Address);
            matchBuilder.setNwSrcMask((short) prefix);
        }
    }

    private static int buildPrefix(final Iterator<String> addressParts) {
        int prefix = 32;
        if (addressParts.hasNext()) {
            prefix = Integer.parseInt(addressParts.next());
        }
        return prefix;
    }

    private static boolean convertDlVlanPcp(final MatchV10Builder matchBuilder,
                                            final VlanMatch vlanMatch) {
        if (vlanMatch.getVlanPcp() != null) {
            matchBuilder.setDlVlanPcp(vlanMatch.getVlanPcp().getValue());
            return false;
        }
        return true;
    }

    private static boolean convertDlVlan(final MatchV10Builder matchBuilder, final VlanMatch vlanMatch) {
        if (vlanMatch.getVlanId() != null) {
            int vlanId = vlanMatch.getVlanId().getVlanId().getValue();
            matchBuilder.setDlVlan(vlanId == 0 ? OFP_VLAN_NONE : vlanId);
            return false;
        }
        return true;
    }

    private static boolean convertEthernetDlType(final MatchV10Builder matchBuilder,
                                                 final EthernetMatch ethernetMatch) {
        if (ethernetMatch.getEthernetType() != null) {
            matchBuilder.setDlType(ethernetMatch.getEthernetType().getType().getValue().intValue());
            return false;
        }
        return true;
    }

    private static boolean convertEthernetDlSrc(final MatchV10Builder matchBuilder,
                                                final EthernetMatch ethernetMatch) {
        if (ethernetMatch.getEthernetSource() != null) {
            matchBuilder.setDlSrc(ethernetMatch.getEthernetSource().getAddress());
            return false;
        }
        return true;
    }

    private static boolean convertEthernetDlDst(final MatchV10Builder matchBuilder,
                                                final EthernetMatch ethernetMatch) {
        if (ethernetMatch.getEthernetDestination() != null) {
            matchBuilder.setDlDst(ethernetMatch.getEthernetDestination().getAddress());
            return false;
        }
        return true;
    }

    private static boolean convertInPortMatch(final MatchV10Builder matchBuilder, final NodeConnectorId inPort) {
        if (inPort != null) {
            matchBuilder.setInPort(InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF10,
                    inPort).intValue());
            return false;
        }
        return true;
    }

    /**
     * Create default empty match v10
     * Use this method, if result from converter is empty.
     */
    public static MatchV10 defaultResult() {
        return new MatchV10Builder().build();
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public MatchV10 convert(final Match source, final VersionConvertorData data) {
        MatchV10Builder matchBuilder = new MatchV10Builder();
        boolean dlDst = true;
        boolean dlSsc = true;
        boolean dlType = true;
        boolean dlVlan = true;
        boolean dlVlanPcp = true;
        boolean ipPort = true;
        boolean nwProto = true;
        boolean nwTos = true;
        boolean tpDst = true;
        boolean tpSrc = true;

        matchBuilder.setInPort(0);
        matchBuilder.setDlDst(ZERO_MAC);
        matchBuilder.setDlSrc(ZERO_MAC);
        matchBuilder.setDlType(0);
        matchBuilder.setDlVlan(OFP_VLAN_NONE);
        matchBuilder.setDlVlanPcp((short) 0);
        matchBuilder.setNwDst(ZERO_IPV4);
        matchBuilder.setNwDstMask((short) 0);
        matchBuilder.setNwSrc(ZERO_IPV4);
        matchBuilder.setNwSrcMask((short) 0);
        matchBuilder.setNwProto((short) 0);
        matchBuilder.setNwTos((short) 0);
        matchBuilder.setTpSrc(0);
        matchBuilder.setTpDst(0);

        if (source != null) {
            EthernetMatch ethernetMatch = source.getEthernetMatch();
            if (ethernetMatch != null) {
                dlDst = convertEthernetDlDst(matchBuilder, ethernetMatch);
                dlSsc = convertEthernetDlSrc(matchBuilder, ethernetMatch);
                dlType = convertEthernetDlType(matchBuilder, ethernetMatch);
            }
            VlanMatch vlanMatch = source.getVlanMatch();
            if (vlanMatch != null) {
                dlVlan = convertDlVlan(matchBuilder, vlanMatch);
                dlVlanPcp = convertDlVlanPcp(matchBuilder, vlanMatch);
            }
            NodeConnectorId inPort = source.getInPort();
            if (inPort != null) {
                ipPort = convertInPortMatch(matchBuilder, inPort);
            }
            Layer3Match l3Match = source.getLayer3Match();
            if (l3Match != null) {
                if (l3Match instanceof Ipv4Match) {
                    Ipv4Match ipv4 = (Ipv4Match) l3Match;
                    convertL3Ipv4SrcMatch(matchBuilder, ipv4);
                    convertL3Ipv4DstMatch(matchBuilder, ipv4);
                }
            }
            IpMatch ipMatch = source.getIpMatch();
            if (ipMatch != null) {
                nwProto = convertNwProto(matchBuilder, ipMatch);
                nwTos = convertNwTos(matchBuilder, ipMatch);
            }
            Layer4Match layer4Match = source.getLayer4Match();
            if (layer4Match != null) {
                if (layer4Match instanceof TcpMatch) {
                    TcpMatch tcpMatch = (TcpMatch) layer4Match;
                    tpSrc = convertL4TpSrcMatch(matchBuilder, tcpMatch);
                    tpDst = convertL4TpDstMatch(matchBuilder, tcpMatch);
                } else if (layer4Match instanceof UdpMatch) {
                    UdpMatch udpMatch = (UdpMatch) layer4Match;
                    tpSrc = convertL4UdpSrcMatch(matchBuilder, udpMatch);
                    tpDst = convertL4UdpDstMatch(matchBuilder, udpMatch);
                }
            } else {
                Icmpv4Match icmpv4Match = source.getIcmpv4Match();
                if (icmpv4Match != null) {
                    Short type = icmpv4Match.getIcmpv4Type();
                    if (type != null) {
                        matchBuilder.setTpSrc(type.intValue());
                        tpSrc = false;
                    }
                    Short code = icmpv4Match.getIcmpv4Code();
                    if (code != null) {
                        matchBuilder.setTpDst(code.intValue());
                        tpDst = false;
                    }
                }
            }
        }

        FlowWildcardsV10 wildCards = new FlowWildcardsV10(
                dlDst, dlSsc, dlType, dlVlan,
                dlVlanPcp, ipPort, nwProto, nwTos, tpDst, tpSrc);
        matchBuilder.setWildcards(wildCards);

        return matchBuilder.build();
    }
}
