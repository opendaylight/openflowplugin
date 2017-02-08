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
import java.util.Map;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchKey;

public class TableFeaturesMatchFieldDeserializer {

    /**
     * Mapping of match entry code to match set field class
     */
    private final Map<MatchEntryDeserializerKey, Class<? extends MatchField>> CODE_TO_FIELD = ImmutableMap
        .<MatchEntryDeserializerKey, Class<? extends MatchField>>builder()
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ARP_OP), ArpOp.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ARP_SHA), ArpSha.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ARP_SPA), ArpSpa.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ARP_THA), ArpTha.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ARP_TPA), ArpTpa.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ETH_DST), EthDst.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ETH_SRC), EthSrc.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ICMPV4_CODE), Icmpv4Code.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ICMPV4_TYPE), Icmpv4Type.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ICMPV6_CODE), Icmpv6Code.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.ICMPV6_TYPE), Icmpv6Type.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IN_PHY_PORT), InPhyPort.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IN_PORT), InPort.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IP_DSCP), IpDscp.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IP_ECN), IpEcn.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IP_PROTO), IpProto.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV4_DST), Ipv4Dst.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV4_SRC), Ipv4Src.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_DST), Ipv6Dst.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_SRC), Ipv6Src.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_EXTHDR), Ipv6Exthdr.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_FLABEL), Ipv6Flabel.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_ND_SLL), Ipv6NdSll.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_ND_TLL), Ipv6NdTll.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.IPV6_ND_TARGET), Ipv6NdTarget.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.METADATA), Metadata.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.MPLS_BOS), MplsBos.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.MPLS_LABEL), MplsLabel.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.MPLS_TC), MplsTc.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.PBB_ISID), PbbIsid.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.SCTP_DST), SctpDst.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.SCTP_SRC), SctpSrc.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.TCP_SRC), TcpSrc.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.TCP_DST), TcpDst.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.TUNNEL_ID), TunnelId.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.UDP_SRC), UdpSrc.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.UDP_DST), UdpDst.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.VLAN_PCP), VlanPcp.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.OPENFLOW_BASIC_CLASS,
                    OxmMatchConstants.VLAN_VID), VlanVid.class)
        .put(new MatchEntryDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    OxmMatchConstants.EXPERIMENTER_CLASS,
                    OxmMatchConstants.NXM_NX_TCP_FLAG), TcpFlags.class)
        .build();

/**
     * Processes match entry header and returns if it have mask, or not
     * @param in input buffer
     * @return SetFieldMatchBuilder with hasMask properly set
     */
    protected static SetFieldMatchBuilder processHeader(ByteBuf in) {
        in.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES); // skip oxm_class
        boolean hasMask = (in.readUnsignedByte() & 1) != 0;
        in.skipBytes(EncodeConstants.SIZE_OF_BYTE_IN_BYTES); // skip match entry length

        return new SetFieldMatchBuilder()
            .setHasMask(hasMask);
    }

    /**
     * Deserialize match field if deserializer supports it, otherwise returns empty optional
     * @param message input buffer
     * @return set field match
     */
    public Optional<SetFieldMatch> deserialize(ByteBuf message) {
        int oxmClass = message.getUnsignedShort(message.readerIndex());
        int oxmField = message.getUnsignedByte(message.readerIndex()
                + EncodeConstants.SIZE_OF_SHORT_IN_BYTES) >>> 1;
        Long expId = null;

        if (oxmClass == EncodeConstants.EXPERIMENTER_VALUE) {
            expId = message.getUnsignedInt(message.readerIndex() + EncodeConstants.SIZE_OF_SHORT_IN_BYTES
                    + 2 * EncodeConstants.SIZE_OF_BYTE_IN_BYTES);
        }

        final MatchEntryDeserializerKey key =
            new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, oxmClass, oxmField);

        key.setExperimenterId(expId);

        return Optional
            .ofNullable(CODE_TO_FIELD.get(key))
            .map(clazz -> processHeader(message)
                    .setKey(new SetFieldMatchKey(clazz))
                    .setMatchType(clazz)
                    .build());
    }}
