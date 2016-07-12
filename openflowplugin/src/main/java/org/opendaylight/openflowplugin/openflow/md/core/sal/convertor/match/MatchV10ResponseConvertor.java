/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.ActionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Converts Openflow 1.0 specific flow match to MD-SAL format flow
 * match
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(version);
 * data.setDatapathId(datapathId);
 * Optional<MatchBuilder> salMatch = convertorManager.convert(ofMatchV10, data);
 * }
 * </pre>
 */
public class MatchV10ResponseConvertor extends Convertor<MatchV10, MatchBuilder, VersionDatapathIdConvertorData> {
    private static final short PROTO_TCP = 6;
    private static final short PROTO_UDP = 17;
    private static final short PROTO_ICMPV4 = 1;
    private static final String NO_IP = "0.0.0.0/0";
    private static final Set<Class<? extends DataContainer>> TYPES = Collections.singleton(MatchV10.class);

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return TYPES;
    }

    @Override
    public MatchBuilder convert(MatchV10 source, VersionDatapathIdConvertorData datapathIdConvertorData) {
        MatchBuilder matchBuilder = new MatchBuilder();
        EthernetMatchBuilder ethMatchBuilder = new EthernetMatchBuilder();
        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        IpMatchBuilder ipMatchBuilder = new IpMatchBuilder();
        OpenflowVersion ofVersion = OpenflowVersion.get(datapathIdConvertorData.getVersion());
        BigInteger datapathid = datapathIdConvertorData.getDatapathId();

        if (!source.getWildcards().isINPORT() && source.getInPort() != null) {
            matchBuilder.setInPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathid,
                    (long) source.getInPort(), ofVersion));
        }

        if (!source.getWildcards().isDLSRC() && source.getDlSrc() != null) {
            EthernetSourceBuilder ethSrcBuilder = new EthernetSourceBuilder();
            ethSrcBuilder.setAddress(source.getDlSrc());
            ethMatchBuilder.setEthernetSource(ethSrcBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }
        if (!source.getWildcards().isDLDST() && source.getDlDst() != null) {
            EthernetDestinationBuilder ethDstBuilder = new EthernetDestinationBuilder();
            ethDstBuilder.setAddress(source.getDlDst());
            ethMatchBuilder.setEthernetDestination(ethDstBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }
        if (!source.getWildcards().isDLTYPE() && source.getDlType() != null) {
            EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
            ethTypeBuilder.setType(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType(
                    (long) source.getDlType()));
            ethMatchBuilder.setEthernetType(ethTypeBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }
        if (!source.getWildcards().isDLVLAN() && source.getDlVlan() != null) {
            VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
            int vlanId = (source.getDlVlan() == (0xffff)) ? 0 : source.getDlVlan();
            vlanIdBuilder.setVlanId(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(vlanId));
            vlanIdBuilder.setVlanIdPresent(vlanId != 0);
            vlanMatchBuilder.setVlanId(vlanIdBuilder.build());
            matchBuilder.setVlanMatch(vlanMatchBuilder.build());
        }
        if (!source.getWildcards().isDLVLANPCP() && source.getDlVlanPcp() != null) {
            vlanMatchBuilder.setVlanPcp(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp(
                    source.getDlVlanPcp()));
            matchBuilder.setVlanMatch(vlanMatchBuilder.build());
        }
        if (!source.getWildcards().isDLTYPE() && source.getNwSrc() != null) {
            final Ipv4Prefix prefix;
            if (source.getNwSrcMask() != null) {
                prefix = IetfInetUtil.INSTANCE.ipv4PrefixFor(source.getNwSrc(), source.getNwSrcMask());
            } else {
                //Openflow Spec : 1.3.2
                //An all-one-bits oxm_mask is equivalent to specifying 0 for oxm_hasmask and omitting oxm_mask.
                // So when user specify 32 as a mast, switch omit that mast and we get null as a mask in flow
                // statistics response.
                prefix = IetfInetUtil.INSTANCE.ipv4PrefixFor(source.getNwSrc());
            }
            if (!NO_IP.equals(prefix.getValue())) {
                ipv4MatchBuilder.setIpv4Source(prefix);
                matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
            }
        }
        if (!source.getWildcards().isDLTYPE() && source.getNwDst() != null) {
            final Ipv4Prefix prefix;
            if (source.getNwDstMask() != null) {
                prefix = IetfInetUtil.INSTANCE.ipv4PrefixFor(source.getNwDst(), source.getNwDstMask());
            } else {
                //Openflow Spec : 1.3.2
                //An all-one-bits oxm_mask is equivalent to specifying 0 for oxm_hasmask and omitting oxm_mask.
                // So when user specify 32 as a mast, switch omit that mast and we get null as a mask in flow
                // statistics response.
                prefix = IetfInetUtil.INSTANCE.ipv4PrefixFor(source.getNwDst());
            }
            if (!NO_IP.equals(prefix.getValue())) {
                ipv4MatchBuilder.setIpv4Destination(prefix);
                matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
            }
        }
        if (!source.getWildcards().isNWPROTO() && source.getNwProto() != null) {
            Short nwProto = source.getNwProto();
            ipMatchBuilder.setIpProtocol(nwProto);
            matchBuilder.setIpMatch(ipMatchBuilder.build());

            int proto = nwProto.intValue();
            if (proto == PROTO_TCP) {
                TcpMatchBuilder tcpMatchBuilder = new TcpMatchBuilder();
                boolean hasTcp = false;
                if (!source.getWildcards().isTPSRC() && source.getTpSrc() != null) {
                    tcpMatchBuilder
                            .setTcpSourcePort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber(
                                    source.getTpSrc()));
                    hasTcp = true;
                }
                if (!source.getWildcards().isTPDST() && source.getTpDst() != null) {
                    tcpMatchBuilder
                            .setTcpDestinationPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber(
                                    source.getTpDst()));
                    hasTcp = true;
                }

                if (hasTcp) {
                    matchBuilder.setLayer4Match(tcpMatchBuilder.build());
                }
            } else if (proto == PROTO_UDP) {
                UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder();
                boolean hasUdp = false;
                if (!source.getWildcards().isTPSRC() && source.getTpSrc() != null) {
                    udpMatchBuilder
                            .setUdpSourcePort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber(
                                    source.getTpSrc()));
                    hasUdp = true;
                }
                if (!source.getWildcards().isTPDST() && source.getTpDst() != null) {
                    udpMatchBuilder
                            .setUdpDestinationPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber(
                                    source.getTpDst()));
                    hasUdp = true;
                }

                if (hasUdp) {
                    matchBuilder.setLayer4Match(udpMatchBuilder.build());
                }
            } else if (proto == PROTO_ICMPV4) {
                Icmpv4MatchBuilder icmpv4MatchBuilder = new Icmpv4MatchBuilder();
                boolean hasIcmpv4 = false;
                if (!source.getWildcards().isTPSRC()) {
                    Integer type = source.getTpSrc();
                    if (type != null) {
                        icmpv4MatchBuilder.setIcmpv4Type(type.shortValue());
                        hasIcmpv4 = true;
                    }
                }
                if (!source.getWildcards().isTPDST()) {
                    Integer code = source.getTpDst();
                    if (code != null) {
                        icmpv4MatchBuilder.setIcmpv4Code(code.shortValue());
                        hasIcmpv4 = true;
                    }
                }

                if (hasIcmpv4) {
                    matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build());
                }
            }
        }
        if (!source.getWildcards().isNWTOS() && source.getNwTos() != null) {
            Short dscp = ActionUtil.tosToDscp(source.getNwTos());
            ipMatchBuilder.setIpDscp(new Dscp(dscp));
            matchBuilder.setIpMatch(ipMatchBuilder.build());
        }

        return matchBuilder;
    }
}
