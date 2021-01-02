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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

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

    private static final Uint8 DEFAULT_PREFIX = Uint8.valueOf(32);

    /*
     * The value 0xffff (OFP_VLAN_NONE) is used to indicate
     * that no VLAN ID is set for OF Flow.
     */
    private static final Uint16 OFP_VLAN_NONE = Uint16.MAX_VALUE;

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
            matchBuilder.setNwTos(ActionUtil.dscpToTos(ipMatch.getIpDscp().getValue().toJava()));
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
    private static void convertL3Ipv4DstMatch(final MatchV10Builder matchBuilder, final Ipv4Match ipv4) {
        final Ipv4Prefix destination = ipv4.getIpv4Destination();
        if (destination != null) {
            // TODO: consider using IetfInetUtil
            Iterator<String> addressParts = IpConversionUtil.PREFIX_SPLITTER.split( destination.getValue()).iterator();
            matchBuilder.setNwDst(new Ipv4Address(addressParts.next()));
            matchBuilder.setNwDstMask(buildPrefix(addressParts));
        }
    }

    /**
     * Method splits the IP address and its mask and set their respective values in MatchV10Builder instance.
     * Wildcard value of the IP mask will be determined by Openflow java encoding library.
     *
     * @param matchBuilder match builder
     * @param ipv4         ip v4 match
     */
    private static void convertL3Ipv4SrcMatch(final MatchV10Builder matchBuilder, final Ipv4Match ipv4) {
        final Ipv4Prefix source = ipv4.getIpv4Source();
        if (source != null) {
            // TODO: consider IetfInetUtil
            Iterator<String> addressParts = IpConversionUtil.PREFIX_SPLITTER.split(source.getValue()).iterator();
            matchBuilder.setNwSrc(new Ipv4Address(addressParts.next()));
            matchBuilder.setNwSrcMask(buildPrefix(addressParts));
        }
    }

    private static Uint8 buildPrefix(final Iterator<String> addressParts) {
        return addressParts.hasNext() ? Uint8.valueOf(addressParts.next()) : DEFAULT_PREFIX;
    }

    private static boolean convertDlVlanPcp(final MatchV10Builder matchBuilder, final VlanMatch vlanMatch) {
        final VlanPcp vlanPcp = vlanMatch.getVlanPcp();
        if (vlanPcp != null) {
            matchBuilder.setDlVlanPcp(vlanPcp.getValue());
            return false;
        }
        return true;
    }

    private static boolean convertDlVlan(final MatchV10Builder matchBuilder, final VlanMatch vlanMatch) {
        final VlanId id = vlanMatch.getVlanId();
        if (id != null) {
            final Uint16 vlanId = id.getVlanId().getValue();
            matchBuilder.setDlVlan(vlanId.toJava() == 0 ? OFP_VLAN_NONE : vlanId);
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
        MatchV10Builder matchBuilder = new MatchV10Builder()
                .setInPort(Uint16.ZERO)
                .setDlDst(ZERO_MAC)
                .setDlSrc(ZERO_MAC)
                .setDlType(Uint16.ZERO)
                .setDlVlan(OFP_VLAN_NONE)
                .setDlVlanPcp(Uint8.ZERO)
                .setNwDst(ZERO_IPV4)
                .setNwDstMask(Uint8.ZERO)
                .setNwSrc(ZERO_IPV4)
                .setNwSrcMask(Uint8.ZERO)
                .setNwProto(Uint8.ZERO)
                .setNwTos(Uint8.ZERO)
                .setTpSrc(Uint16.ZERO)
                .setTpDst(Uint16.ZERO);

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
                    Uint8 type = icmpv4Match.getIcmpv4Type();
                    if (type != null) {
                        matchBuilder.setTpSrc(Uint16.valueOf(type));
                        tpSrc = false;
                    }
                    Uint8 code = icmpv4Match.getIcmpv4Code();
                    if (code != null) {
                        matchBuilder.setTpDst(Uint16.valueOf(code));
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
