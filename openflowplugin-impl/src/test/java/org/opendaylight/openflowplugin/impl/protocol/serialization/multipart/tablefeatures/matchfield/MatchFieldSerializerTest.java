/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.AbstractTablePropertySerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.match.MatchSetfieldBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;

public class MatchFieldSerializerTest extends AbstractTablePropertySerializerTest {
    @Test
    public void testSerialize() {
        final Match property = new MatchBuilder()
            .setMatchSetfield(new MatchSetfieldBuilder()
                .setSetFieldMatch(BindingMap.ordered(
                    new SetFieldMatchBuilder().setMatchType(ArpOp.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(ArpSha.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(ArpSpa.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(ArpTha.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(ArpTpa.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(EthDst.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(EthSrc.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(EthType.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Icmpv4Code.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Icmpv4Type.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Icmpv6Code.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Icmpv6Type.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(InPhyPort.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(InPort.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(IpDscp.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(IpEcn.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(IpProto.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Ipv4Dst.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Ipv4Src.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Ipv6Dst.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Ipv6Exthdr.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Ipv6Flabel.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Ipv6NdSll.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Ipv6NdTarget.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Ipv6NdTll.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Ipv6Src.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(Metadata.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(MplsBos.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(MplsLabel.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(MplsTc.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(PbbIsid.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(SctpDst.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(SctpSrc.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(TcpDst.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(TcpFlags.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(TcpSrc.VALUE).setHasMask(true).build(),
                    new SetFieldMatchBuilder().setMatchType(TunnelId.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(UdpDst.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(UdpSrc.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(VlanPcp.VALUE).setHasMask(false).build(),
                    new SetFieldMatchBuilder().setMatchType(VlanVid.VALUE).setHasMask(false).build()))
                .build())
            .build();

        assertProperty(property, out -> {
            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ARP_OP << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ARP_SHA << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ARP_SPA << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ARP_THA << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ARP_TPA << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ETH_DST << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ETH_SRC << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ETH_TYPE << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ICMPV4_CODE << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ICMPV4_TYPE << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ICMPV6_CODE << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ICMPV6_TYPE << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IN_PHY_PORT << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IN_PORT << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IP_DSCP << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IP_ECN << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IP_PROTO << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IPV4_DST << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IPV4_SRC << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IPV6_DST << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IPV6_EXTHDR << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IPV6_FLABEL << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IPV6_ND_SLL << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IPV6_ND_TARGET << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IPV6_ND_TLL << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IPV6_SRC << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.METADATA << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.MPLS_BOS << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.MPLS_LABEL << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.MPLS_TC << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.PBB_ISID << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.SCTP_DST << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.SCTP_SRC << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.TCP_DST << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.EXPERIMENTER_CLASS);
            assertEquals(out.readUnsignedByte(), EncodeConstants.ONFOXM_ET_TCP_FLAGS << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte() >>> 1, OxmMatchConstants.TCP_SRC);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.TUNNEL_ID << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.UDP_DST << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.UDP_SRC << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.VLAN_PCP << 1);
            out.skipBytes(Byte.BYTES);

            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.VLAN_VID << 1);
            out.skipBytes(Byte.BYTES);
        });
    }

    @Override
    protected Class<? extends TableFeaturePropType> getClazz() {
        return Match.class;
    }

    @Override
    protected int getType() {
        return TableFeaturesPropType.OFPTFPTMATCH.getIntValue();
    }
}
