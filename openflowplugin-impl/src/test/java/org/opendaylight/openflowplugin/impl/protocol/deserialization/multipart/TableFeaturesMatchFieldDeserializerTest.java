/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.Optional;
import org.junit.Test;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;

public class TableFeaturesMatchFieldDeserializerTest {
    private TableFeaturesMatchFieldDeserializer deserializer = new TableFeaturesMatchFieldDeserializer();

    @Test
    public void deserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ARP_OP);
        Optional<SetFieldMatch> fieldMatch = deserializer.deserialize(buffer);
        assertEquals(ArpOp.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ARP_SHA);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(ArpSha.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ARP_SPA);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(ArpSpa.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ARP_THA);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(ArpTha.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ARP_TPA);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(ArpTpa.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ETH_DST);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(EthDst.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ETH_SRC);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(EthSrc.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ICMPV4_CODE);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Icmpv4Code.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ICMPV4_TYPE);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Icmpv4Type.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ICMPV6_CODE);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Icmpv6Code.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ICMPV6_TYPE);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Icmpv6Type.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IN_PHY_PORT);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(InPhyPort.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IN_PORT);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(InPort.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IP_DSCP);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(IpDscp.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IP_ECN);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(IpEcn.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IP_PROTO);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(IpProto.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV4_SRC);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Ipv4Src.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV4_DST);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Ipv4Dst.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_SRC);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Ipv6Src.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_DST);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Ipv6Dst.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_EXTHDR);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Ipv6Exthdr.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_FLABEL);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Ipv6Flabel.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_ND_SLL);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Ipv6NdSll.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_ND_TLL);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Ipv6NdTll.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_ND_TARGET);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Ipv6NdTarget.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.METADATA);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(Metadata.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.MPLS_BOS);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(MplsBos.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.MPLS_LABEL);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(MplsLabel.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.MPLS_TC);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(MplsTc.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.PBB_ISID);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(PbbIsid.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.SCTP_SRC);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(SctpSrc.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.SCTP_DST);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(SctpDst.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.TCP_SRC);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(TcpSrc.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.TCP_DST);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(TcpDst.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.TUNNEL_ID);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(TunnelId.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.UDP_SRC);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(UdpSrc.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.UDP_DST);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(UdpDst.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.VLAN_PCP);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(VlanPcp.class, fieldMatch.get().getKey().getMatchType());

        addValues(buffer, OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.VLAN_VID);
        fieldMatch = deserializer.deserialize(buffer);
        assertEquals(VlanVid.class, fieldMatch.get().getKey().getMatchType());

        assertEquals(0, buffer.readableBytes());
    }

    private void addValues(ByteBuf buffer, int oxmClass, int oxmField) {
        buffer.clear();
        buffer.writeShort(oxmClass);
        buffer.writeByte(oxmField << 1);
        buffer.writeByte(EncodeConstants.EMPTY_LENGTH);
    }
}
