/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

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
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Converts Openflow 1.0 specific flow match to MD-SAL format flow match.
 *
 * <p>
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
    private static final Set<Class<?>> TYPES = Collections.singleton(MatchV10.class);

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public MatchBuilder convert(final MatchV10 source, final VersionDatapathIdConvertorData datapathIdConvertorData) {
        MatchBuilder matchBuilder = new MatchBuilder();
        EthernetMatchBuilder ethMatchBuilder = new EthernetMatchBuilder();
        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        IpMatchBuilder ipMatchBuilder = new IpMatchBuilder();
        OpenflowVersion ofVersion = OpenflowVersion.get(datapathIdConvertorData.getVersion());
        Uint64 datapathid = datapathIdConvertorData.getDatapathId();

        if (!source.getWildcards().getINPORT() && source.getInPort() != null) {
            matchBuilder.setInPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathid,
                    Uint32.valueOf(source.getInPort()), ofVersion));
        }

        if (!source.getWildcards().getDLSRC() && source.getDlSrc() != null) {
            EthernetSourceBuilder ethSrcBuilder = new EthernetSourceBuilder();
            ethSrcBuilder.setAddress(source.getDlSrc());
            ethMatchBuilder.setEthernetSource(ethSrcBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }
        if (!source.getWildcards().getDLDST() && source.getDlDst() != null) {
            EthernetDestinationBuilder ethDstBuilder = new EthernetDestinationBuilder();
            ethDstBuilder.setAddress(source.getDlDst());
            ethMatchBuilder.setEthernetDestination(ethDstBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }
        if (!source.getWildcards().getDLTYPE() && source.getDlType() != null) {
            EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
            ethTypeBuilder.setType(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType(
                    Uint32.valueOf(source.getDlType())));
            ethMatchBuilder.setEthernetType(ethTypeBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }
        if (!source.getWildcards().getDLVLAN() && source.getDlVlan() != null) {
            Uint16 vlanId = source.getDlVlan();
            if (vlanId.toJava() == 0xffff) {
                vlanId = Uint16.ZERO;
            }

            vlanMatchBuilder.setVlanId(new VlanIdBuilder()
                .setVlanId(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(vlanId))
                .setVlanIdPresent(!Uint16.ZERO.equals(vlanId))
                .build());
            matchBuilder.setVlanMatch(vlanMatchBuilder.build());
        }
        if (!source.getWildcards().getDLVLANPCP() && source.getDlVlanPcp() != null) {
            vlanMatchBuilder.setVlanPcp(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp(
                    source.getDlVlanPcp()));
            matchBuilder.setVlanMatch(vlanMatchBuilder.build());
        }
        if (!source.getWildcards().getDLTYPE() && source.getNwSrc() != null) {
            final Ipv4Prefix prefix;
            if (source.getNwSrcMask() != null) {
                prefix = IetfInetUtil.INSTANCE.ipv4PrefixFor(source.getNwSrc(), source.getNwSrcMask().toJava());
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
        if (!source.getWildcards().getDLTYPE() && source.getNwDst() != null) {
            final Ipv4Prefix prefix;
            if (source.getNwDstMask() != null) {
                prefix = IetfInetUtil.INSTANCE.ipv4PrefixFor(source.getNwDst(), source.getNwDstMask().toJava());
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
        if (!source.getWildcards().getNWPROTO() && source.getNwProto() != null) {
            Uint8 nwProto = source.getNwProto();
            ipMatchBuilder.setIpProtocol(nwProto);
            matchBuilder.setIpMatch(ipMatchBuilder.build());

            int proto = nwProto.intValue();
            if (proto == PROTO_TCP) {
                TcpMatchBuilder tcpMatchBuilder = new TcpMatchBuilder();
                boolean hasTcp = false;
                if (!source.getWildcards().getTPSRC() && source.getTpSrc() != null) {
                    tcpMatchBuilder.setTcpSourcePort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                            .inet.types.rev130715.PortNumber(source.getTpSrc()));
                    hasTcp = true;
                }
                if (!source.getWildcards().getTPDST() && source.getTpDst() != null) {
                    tcpMatchBuilder.setTcpDestinationPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang
                            .ietf.inet.types.rev130715.PortNumber(source.getTpDst()));
                    hasTcp = true;
                }

                if (hasTcp) {
                    matchBuilder.setLayer4Match(tcpMatchBuilder.build());
                }
            } else if (proto == PROTO_UDP) {
                UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder();
                boolean hasUdp = false;
                if (!source.getWildcards().getTPSRC() && source.getTpSrc() != null) {
                    udpMatchBuilder.setUdpSourcePort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                            .inet.types.rev130715.PortNumber(source.getTpSrc()));
                    hasUdp = true;
                }
                if (!source.getWildcards().getTPDST() && source.getTpDst() != null) {
                    udpMatchBuilder.setUdpDestinationPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang
                            .ietf.inet.types.rev130715.PortNumber(source.getTpDst()));
                    hasUdp = true;
                }

                if (hasUdp) {
                    matchBuilder.setLayer4Match(udpMatchBuilder.build());
                }
            } else if (proto == PROTO_ICMPV4) {
                Icmpv4MatchBuilder icmpv4MatchBuilder = new Icmpv4MatchBuilder();
                boolean hasIcmpv4 = false;
                if (!source.getWildcards().getTPSRC()) {
                    Uint16 type = source.getTpSrc();
                    if (type != null) {
                        icmpv4MatchBuilder.setIcmpv4Type(type.toUint8());
                        hasIcmpv4 = true;
                    }
                }
                if (!source.getWildcards().getTPDST()) {
                    Uint16 code = source.getTpDst();
                    if (code != null) {
                        icmpv4MatchBuilder.setIcmpv4Code(code.toUint8());
                        hasIcmpv4 = true;
                    }
                }

                if (hasIcmpv4) {
                    matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build());
                }
            }
        }
        if (!source.getWildcards().getNWTOS() && source.getNwTos() != null) {
            ipMatchBuilder.setIpDscp(new Dscp(ActionUtil.tosToDscp(source.getNwTos())));
            matchBuilder.setIpMatch(ipMatchBuilder.build());
        }

        return matchBuilder;
    }
}
