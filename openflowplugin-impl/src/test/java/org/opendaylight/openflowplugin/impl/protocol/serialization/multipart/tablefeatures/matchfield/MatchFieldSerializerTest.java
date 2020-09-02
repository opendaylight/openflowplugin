/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.match.MatchSetfieldBuilder;

public class MatchFieldSerializerTest extends AbstractTablePropertySerializerTest {
    @Test
    public void testSerialize() {
        final Match property = new MatchBuilder()
                .setMatchSetfield(new MatchSetfieldBuilder()
                        .setSetFieldMatch(ImmutableList
                                .<SetFieldMatch>builder()
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(ArpOp.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(ArpSha.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(ArpSpa.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(ArpTha.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(ArpTpa.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(EthDst.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(EthSrc.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(EthType.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Icmpv4Code.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Icmpv4Type.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Icmpv6Code.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Icmpv6Type.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(InPhyPort.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(InPort.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(IpDscp.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(IpEcn.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(IpProto.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Ipv4Dst.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Ipv4Src.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Ipv6Dst.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Ipv6Exthdr.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Ipv6Flabel.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Ipv6NdSll.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Ipv6NdTarget.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Ipv6NdTll.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Ipv6Src.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(Metadata.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(MplsBos.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(MplsLabel.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(MplsTc.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(PbbIsid.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(SctpDst.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(SctpSrc.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(TcpDst.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(TcpFlags.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(TcpSrc.class)
                                        .setHasMask(true)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(TunnelId.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(UdpDst.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(UdpSrc.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(VlanPcp.class)
                                        .setHasMask(false)
                                        .build())
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(VlanVid.class)
                                        .setHasMask(false)
                                        .build())
                                .build())
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
