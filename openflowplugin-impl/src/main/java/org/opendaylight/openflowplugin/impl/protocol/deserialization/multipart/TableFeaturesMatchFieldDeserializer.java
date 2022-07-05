/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class TableFeaturesMatchFieldDeserializer {
    /**
     * Mapping of match entry code to match set field class.
     */
    private final ImmutableMap<MatchEntryDeserializerKey, MatchField> codeToFieldMap = ImmutableMap
        .<MatchEntryDeserializerKey, MatchField>builder()
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ARP_OP), ArpOp.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ARP_SHA), ArpSha.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ARP_SPA), ArpSpa.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ARP_THA), ArpTha.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ARP_TPA), ArpTpa.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ETH_DST), EthDst.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ETH_SRC), EthSrc.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ICMPV4_CODE), Icmpv4Code.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ICMPV4_TYPE), Icmpv4Type.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ICMPV6_CODE), Icmpv6Code.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ICMPV6_TYPE), Icmpv6Type.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IN_PHY_PORT), InPhyPort.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IN_PORT), InPort.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IP_DSCP), IpDscp.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IP_ECN), IpEcn.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IP_PROTO), IpProto.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV4_DST), Ipv4Dst.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV4_SRC), Ipv4Src.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_DST), Ipv6Dst.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_SRC), Ipv6Src.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_EXTHDR), Ipv6Exthdr.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_FLABEL), Ipv6Flabel.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_ND_SLL), Ipv6NdSll.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_ND_TLL), Ipv6NdTll.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_ND_TARGET), Ipv6NdTarget.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.METADATA), Metadata.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.MPLS_BOS), MplsBos.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.MPLS_LABEL), MplsLabel.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.MPLS_TC), MplsTc.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.PBB_ISID), PbbIsid.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.SCTP_DST), SctpDst.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.SCTP_SRC), SctpSrc.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.TCP_SRC), TcpSrc.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.TCP_DST), TcpDst.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.TUNNEL_ID), TunnelId.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.UDP_SRC), UdpSrc.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.UDP_DST), UdpDst.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.VLAN_PCP), VlanPcp.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.VLAN_VID), VlanVid.VALUE)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    OxmMatchConstants.EXPERIMENTER_CLASS,
                    OxmMatchConstants.NXM_NX_TCP_FLAG), TcpFlags.VALUE)
        .build();

    /**
     * Processes match entry header and returns if it have mask, or not.
     *
     * @param in input buffer
     * @return SetFieldMatchBuilder with hasMask properly set
     */
    protected static SetFieldMatchBuilder processHeader(final ByteBuf in) {
        in.skipBytes(Short.BYTES); // skip oxm_class
        boolean hasMask = (in.readUnsignedByte() & 1) != 0;
        in.skipBytes(Byte.BYTES); // skip match entry length

        return new SetFieldMatchBuilder()
                .setHasMask(hasMask);
    }

    /**
     * Deserialize match field if deserializer supports it, otherwise returns empty optional.
     *
     * @param message input buffer
     * @return set field match
     */
    // FIXME: consider a nullable return
    public Optional<SetFieldMatch> deserialize(final ByteBuf message) {
        final int oxmClass = message.getUnsignedShort(message.readerIndex());
        final int oxmField = message.getUnsignedByte(message.readerIndex() + Short.BYTES) >>> 1;

        final MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, oxmClass,
            oxmField);
        if (oxmClass == EncodeConstants.EXPERIMENTER_VALUE) {
            key.setExperimenterId(Uint32.valueOf(
                message.getUnsignedInt(message.readerIndex() + Short.BYTES + 2 * Byte.BYTES)));
        }

        final MatchField clazz = codeToFieldMap.get(key);
        return clazz == null ? Optional.empty() : Optional.of(processHeader(message).setMatchType(clazz).build());
    }
}
