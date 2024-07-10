/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.openflowjava.nx.codec.match.NiciraMatchCodecs;
import org.opendaylight.openflowjava.nx.codec.match.NxmHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg0;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg5;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg7;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.DstChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxArpShaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxArpThaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxIpv6DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxIpv6SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshFlagsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc1CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc2CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc3CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc4CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNsiCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNspCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxOfInPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIpv4DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIpv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpSpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpTpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfEthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfEthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfIcmpTypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfIpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfIpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfMplsLabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.SrcChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxArpShaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxArpThaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxIpv6DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxIpv6SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshFlagsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshMdtypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshNpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshc1CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshc2CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshc3CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshc4CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNsiCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNspCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxOfInPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxOfMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxOfMplsLabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxRegCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxRegCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIpv4DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIpv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpSpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpTpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthTypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfIpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfIpSrcCaseBuilder;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * A helper class that maps various NXM/OXM header representations to
 * source/destination choice fields used in some of the openflow actions.
 */
public final class FieldChoiceResolver {

    private FieldChoiceResolver() {
        // utility class
    }

    private static final Map<Number, SrcChoice> NXMHEADER_TO_SRC_CHOICE;
    private static final Map<Number, DstChoice> NXMHEADER_TO_DST_CHOICE;
    private static final Map<Class<? extends DataContainer>, NxmHeader> DST_CHOICE_TYPE_TO_NXMHEADER;
    private static final Map<Class<? extends DataContainer>, NxmHeader> SRC_CHOICE_TYPE_TO_NXMHEADER;
    private static final Map<DstChoice, NxmHeader> REG_DST_CHOICE_TO_NXMHEADER;
    private static final Map<SrcChoice, NxmHeader> REG_SRC_CHOICE_TO_NXMHEADER;

    static {
        final ImmutableMap<SrcChoice, NxmHeader> srcChoiceToNxmheader =
                new ImmutableMap.Builder<SrcChoice, NxmHeader>()
                        .put(new SrcNxTunIdCaseBuilder().setNxTunId(Empty.value()).build(),
                                NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxArpShaCaseBuilder().setNxArpSha(Empty.value()).build(),
                                NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxArpThaCaseBuilder().setNxArpTha(Empty.value()).build(),
                                NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcOfArpOpCaseBuilder().setOfArpOp(Empty.value()).build(),
                                NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcOfArpSpaCaseBuilder().setOfArpSpa(Empty.value()).build(),
                                NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcOfArpTpaCaseBuilder().setOfArpTpa(Empty.value()).build(),
                                NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxOfInPortCaseBuilder().setOfInPort(Empty.value()).build(),
                                NiciraMatchCodecs.NXM_OF_IN_PORT_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxOfMetadataCaseBuilder().setOfMetadata(Empty.value()).build(),
                                NiciraMatchCodecs.OXM_OF_METADATA_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcOfEthDstCaseBuilder().setOfEthDst(Empty.value()).build(),
                                NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcOfEthSrcCaseBuilder().setOfEthSrc(Empty.value()).build(),
                                NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcOfEthTypeCaseBuilder().setOfEthType(Empty.value()).build(),
                                NiciraMatchCodecs.ETH_TYPE_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxTunIpv4DstCaseBuilder().setNxTunIpv4Dst(Empty.value()).build(),
                                NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxTunIpv4SrcCaseBuilder().setNxTunIpv4Src(Empty.value()).build(),
                                NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxNspCaseBuilder().setNxNspDst(Empty.value()).build(),
                                NiciraMatchCodecs.NSP_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxNsiCaseBuilder().setNxNsiDst(Empty.value()).build(),
                                NiciraMatchCodecs.NSI_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxNshc1CaseBuilder().setNxNshc1Dst(Empty.value()).build(),
                                NiciraMatchCodecs.NSC1_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxNshc2CaseBuilder().setNxNshc2Dst(Empty.value()).build(),
                                NiciraMatchCodecs.NSC2_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxNshc3CaseBuilder().setNxNshc3Dst(Empty.value()).build(),
                                NiciraMatchCodecs.NSC3_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxNshc4CaseBuilder().setNxNshc4Dst(Empty.value()).build(),
                                NiciraMatchCodecs.NSC4_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxNshFlagsCaseBuilder().setNxNshFlags(Empty.value()).build(),
                                NiciraMatchCodecs.NSH_FLAGS_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxNshTtlCaseBuilder().setNxNshTtl(Empty.value()).build(),
                                NiciraMatchCodecs.NSH_TTL_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxNshMdtypeCaseBuilder().setNxNshMdtype(Empty.value()).build(),
                                NiciraMatchCodecs.NSH_MDTYPE_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxNshNpCaseBuilder().setNxNshNp(Empty.value()).build(),
                                NiciraMatchCodecs.NSH_NP_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcOfIpDstCaseBuilder().setOfIpDst(Empty.value()).build(),
                                NiciraMatchCodecs.IP_DST_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcOfIpSrcCaseBuilder().setOfIpSrc(Empty.value()).build(),
                                NiciraMatchCodecs.IP_SRC_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxIpv6SrcCaseBuilder().setNxIpv6Src(Empty.value()).build(),
                                NiciraMatchCodecs.IPV6_SRC_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxIpv6DstCaseBuilder().setNxIpv6Dst(Empty.value()).build(),
                                NiciraMatchCodecs.IPV6_DST_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxOfMplsLabelCaseBuilder().setOfMplsLabel(Empty.value()).build(),
                                NiciraMatchCodecs.OXM_OF_MPLS_LABEL.getHeaderWithoutHasMask())
                        .put(new SrcNxRegCaseBuilder().setNxReg(NxmNxReg0.VALUE).build(),
                                NiciraMatchCodecs.REG0_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxRegCaseBuilder().setNxReg(NxmNxReg1.VALUE).build(),
                                NiciraMatchCodecs.REG1_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxRegCaseBuilder().setNxReg(NxmNxReg2.VALUE).build(),
                                NiciraMatchCodecs.REG2_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxRegCaseBuilder().setNxReg(NxmNxReg3.VALUE).build(),
                                NiciraMatchCodecs.REG3_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxRegCaseBuilder().setNxReg(NxmNxReg4.VALUE).build(),
                                NiciraMatchCodecs.REG4_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxRegCaseBuilder().setNxReg(NxmNxReg5.VALUE).build(),
                                NiciraMatchCodecs.REG5_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxRegCaseBuilder().setNxReg(NxmNxReg6.VALUE).build(),
                                NiciraMatchCodecs.REG6_CODEC.getHeaderWithoutHasMask())
                        .put(new SrcNxRegCaseBuilder().setNxReg(NxmNxReg7.VALUE).build(),
                                NiciraMatchCodecs.REG7_CODEC.getHeaderWithoutHasMask())
                        .build();

        final ImmutableMap<DstChoice, NxmHeader> dstChoiceToNxmheader =
                new ImmutableMap.Builder<DstChoice, NxmHeader>()
                        .put(new DstNxTunIdCaseBuilder().setNxTunId(Empty.value()).build(),
                                NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxArpShaCaseBuilder().setNxArpSha(Empty.value()).build(),
                                NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxArpThaCaseBuilder().setNxArpTha(Empty.value()).build(),
                                NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask())
                        .put(new DstOfArpOpCaseBuilder().setOfArpOp(Empty.value()).build(),
                                NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask())
                        .put(new DstOfArpSpaCaseBuilder().setOfArpSpa(Empty.value()).build(),
                                NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask())
                        .put(new DstOfArpTpaCaseBuilder().setOfArpTpa(Empty.value()).build(),
                                NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxOfInPortCaseBuilder().setOfInPort(Empty.value()).build(),
                                NiciraMatchCodecs.NXM_OF_IN_PORT_CODEC.getHeaderWithoutHasMask())
                        .put(new DstOfMetadataCaseBuilder().setOfMetadata(Empty.value()).build(),
                                NiciraMatchCodecs.OXM_OF_METADATA_CODEC.getHeaderWithoutHasMask())
                        .put(new DstOfEthSrcCaseBuilder().setOfEthSrc(Empty.value()).build(),
                                NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask())
                        .put(new DstOfEthDstCaseBuilder().setOfEthDst(Empty.value()).build(),
                                NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxTunIpv4DstCaseBuilder().setNxTunIpv4Dst(Empty.value()).build(),
                                NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxTunIpv4SrcCaseBuilder().setNxTunIpv4Src(Empty.value()).build(),
                                NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxNspCaseBuilder().setNxNspDst(Empty.value()).build(),
                                NiciraMatchCodecs.NSP_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxNsiCaseBuilder().setNxNsiDst(Empty.value()).build(),
                                NiciraMatchCodecs.NSI_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxNshc1CaseBuilder().setNxNshc1Dst(Empty.value()).build(),
                                NiciraMatchCodecs.NSC1_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxNshc2CaseBuilder().setNxNshc2Dst(Empty.value()).build(),
                                NiciraMatchCodecs.NSC2_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxNshc3CaseBuilder().setNxNshc3Dst(Empty.value()).build(),
                                NiciraMatchCodecs.NSC3_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxNshc4CaseBuilder().setNxNshc4Dst(Empty.value()).build(),
                                NiciraMatchCodecs.NSC4_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxNshFlagsCaseBuilder().setNxNshFlags(Empty.value()).build(),
                                NiciraMatchCodecs.NSH_FLAGS_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxNshTtlCaseBuilder().setNxNshTtl(Empty.value()).build(),
                                NiciraMatchCodecs.NSH_TTL_CODEC.getHeaderWithoutHasMask())
                        .put(new DstOfIpDstCaseBuilder().setOfIpDst(Empty.value()).build(),
                                NiciraMatchCodecs.IP_DST_CODEC.getHeaderWithoutHasMask())
                        .put(new DstOfIpSrcCaseBuilder().setOfIpSrc(Empty.value()).build(),
                                NiciraMatchCodecs.IP_SRC_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxIpv6SrcCaseBuilder().setNxIpv6Src(Empty.value()).build(),
                                NiciraMatchCodecs.IPV6_SRC_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxIpv6DstCaseBuilder().setNxIpv6Dst(Empty.value()).build(),
                                NiciraMatchCodecs.IPV6_DST_CODEC.getHeaderWithoutHasMask())
                        .put(new DstOfMplsLabelCaseBuilder().setOfMplsLabel(Empty.value()).build(),
                                NiciraMatchCodecs.OXM_OF_MPLS_LABEL.getHeaderWithoutHasMask())
                        .put(new DstOfIcmpTypeCaseBuilder().setOfIcmpType(Empty.value()).build(),
                                NiciraMatchCodecs.ICMP_TYPE_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxRegCaseBuilder().setNxReg(NxmNxReg0.VALUE).build(),
                                NiciraMatchCodecs.REG0_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxRegCaseBuilder().setNxReg(NxmNxReg1.VALUE).build(),
                                NiciraMatchCodecs.REG1_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxRegCaseBuilder().setNxReg(NxmNxReg2.VALUE).build(),
                                NiciraMatchCodecs.REG2_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxRegCaseBuilder().setNxReg(NxmNxReg3.VALUE).build(),
                                NiciraMatchCodecs.REG3_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxRegCaseBuilder().setNxReg(NxmNxReg4.VALUE).build(),
                                NiciraMatchCodecs.REG4_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxRegCaseBuilder().setNxReg(NxmNxReg5.VALUE).build(),
                                NiciraMatchCodecs.REG5_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxRegCaseBuilder().setNxReg(NxmNxReg6.VALUE).build(),
                                NiciraMatchCodecs.REG6_CODEC.getHeaderWithoutHasMask())
                        .put(new DstNxRegCaseBuilder().setNxReg(NxmNxReg7.VALUE).build(),
                                NiciraMatchCodecs.REG7_CODEC.getHeaderWithoutHasMask())
                        .build();

        ImmutableMap.Builder<Number, SrcChoice> headerToSrcChoiceBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<Class<? extends DataContainer>, NxmHeader> srcTypeToHeaderBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<SrcChoice, NxmHeader> regSrcChoiceToHeaderBuilder = ImmutableMap.builder();
        srcChoiceToNxmheader.forEach((srcChoice, header) -> {
            headerToSrcChoiceBuilder.put(header.toLong(), srcChoice);
            headerToSrcChoiceBuilder.put(header.toUint64(), srcChoice);
            if (srcChoice instanceof SrcNxRegCase) {
                regSrcChoiceToHeaderBuilder.put(srcChoice, header);
            } else {
                srcTypeToHeaderBuilder.put(srcChoice.implementedInterface(), header);
            }
        });
        NXMHEADER_TO_SRC_CHOICE = headerToSrcChoiceBuilder.build();
        SRC_CHOICE_TYPE_TO_NXMHEADER = srcTypeToHeaderBuilder.build();
        REG_SRC_CHOICE_TO_NXMHEADER = regSrcChoiceToHeaderBuilder.build();

        ImmutableMap.Builder<Number, DstChoice> dstToHeaderBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<Class<? extends DataContainer>, NxmHeader> dstTypeToHeaderBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DstChoice, NxmHeader> regDstChoiceToHeaderBuilder = ImmutableMap.builder();
        dstChoiceToNxmheader.forEach((dstChoice, header) -> {
            dstToHeaderBuilder.put(header.toLong(), dstChoice);
            dstToHeaderBuilder.put(header.toUint64(), dstChoice);
            if (dstChoice instanceof DstNxRegCase) {
                regDstChoiceToHeaderBuilder.put(dstChoice, header);
            } else {
                dstTypeToHeaderBuilder.put(dstChoice.implementedInterface(), header);
            }
        });
        NXMHEADER_TO_DST_CHOICE = dstToHeaderBuilder.build();
        DST_CHOICE_TYPE_TO_NXMHEADER = dstTypeToHeaderBuilder.build();
        REG_DST_CHOICE_TO_NXMHEADER = regDstChoiceToHeaderBuilder.build();
    }

    /**
     * Resolves a destination choice field from a {@code Long} representation
     * of a NXM/OXM header.
     *
     * @param header the OXM/NXM header.
     * @return the destination choice.
     */
    static DstChoice resolveDstChoice(final Uint32 header) {
        return NXMHEADER_TO_DST_CHOICE.get(header.toJava());
    }

    static DstChoice resolveDstChoice(final Long header) {
        return NXMHEADER_TO_DST_CHOICE.get(header);
    }

    /**
     * Resolves a destination choice field from a {@code BigInteger} representation
     * of a NXM/OXM header.
     *
     * @param header the OXM/NXM header.
     * @return the destination choice.
     */
    static DstChoice resolveDstChoice(final Uint64 header) {
        return NXMHEADER_TO_DST_CHOICE.get(header);
    }

    /**
     * Resolves a source choice field from a {@code Long} representation
     * of a NXM/OXM header.
     *
     * @param header the OXM/NXM header.
     * @return the source choice.
     */
    static SrcChoice resolveSrcChoice(final Long header) {
        return NXMHEADER_TO_SRC_CHOICE.get(header);
    }

    static SrcChoice resolveSrcChoice(final Uint32 header) {
        return NXMHEADER_TO_SRC_CHOICE.get(header.toJava());
    }

    /**
     * Resolves a source choice field from a {@code BigInteger} representation
     * of a NXM/OXM header.
     *
     * @param header the OXM/NXM header.
     * @return the destination choice.
     */
    static SrcChoice resolveSrcChoice(final Uint64 header) {
        return NXMHEADER_TO_SRC_CHOICE.get(header);
    }

    /**
     * Resolves a uint32 representation of a non experimenter 4 byte
     * OXM/NXM header for the field given as {@code dstChoice}.
     *
     * @param dstChoice the destination choice field.
     * @return the OXM/NXM header as uint32 {@code Long}.
     * @throws IllegalArgumentException if the field is experimenter.
     */
    static Uint32 resolveDstHeaderUint32(final DstChoice dstChoice) {
        NxmHeader nxmHeader = dstChoice instanceof DstNxRegCase
                ? REG_DST_CHOICE_TO_NXMHEADER.get(dstChoice)
                : DST_CHOICE_TYPE_TO_NXMHEADER.get(dstChoice.implementedInterface());
        if (nxmHeader.isExperimenter()) {
            throw new IllegalArgumentException("Cannot fit experimenter destination choice on a uint32 header");
        }
        return Uint32.valueOf(nxmHeader.toLong());
    }

    /**
     * Resolves a uint64 representation of a possibly experimenter
     * OXM/NXM header for the field given as {@code dstChoice}.
     *
     * @param dstChoice the destination choice field.
     * @return the OXM/NXM header as uint64 {@code BigInteger}.
     */
    static Uint64 resolveDstHeaderUint64(final DstChoice dstChoice) {
        return dstChoice instanceof DstNxRegCase
                ? REG_DST_CHOICE_TO_NXMHEADER.get(dstChoice).toUint64()
                : DST_CHOICE_TYPE_TO_NXMHEADER.get(dstChoice.implementedInterface()).toUint64();
    }

    /**
     * Resolves a uint32 representation of a non experimenter 4 byte
     * OXM/NXM header for the field given as {@code srcChoice}.
     *
     * @param srcChoice the source choice field.
     * @return the OXM/NXM header as uint32 {@code Long}.
     * @throws IllegalArgumentException if the field is experimenter.
     */
    static Uint32 resolveSrcHeaderUint32(final SrcChoice srcChoice) {
        NxmHeader nxmHeader = srcChoice instanceof SrcNxRegCase
                ? REG_SRC_CHOICE_TO_NXMHEADER.get(srcChoice)
                : SRC_CHOICE_TYPE_TO_NXMHEADER.get(srcChoice.implementedInterface());
        if (nxmHeader.isExperimenter()) {
            throw new IllegalArgumentException("Cannot fit experimenter source choice on a uint32 header");
        }
        return Uint32.valueOf(nxmHeader.toLong());
    }

    /**
     * Resolves a uint64 representation of a possibly experimenter
     * OXM/NXM header for the field given as {@code srcChoice}.
     *
     * @param srcChoice the destination choice field.
     * @return the OXM/NXM header as uint64 {@code BigInteger}.
     */
    static Uint64 resolveSrcHeaderUint64(final SrcChoice srcChoice) {
        return srcChoice instanceof SrcNxRegCase
                ? REG_SRC_CHOICE_TO_NXMHEADER.get(srcChoice).toUint64()
                : SRC_CHOICE_TYPE_TO_NXMHEADER.get(srcChoice.implementedInterface()).toUint64();
    }

    /**
     * Check if the source choice field is experimenter.
     *
     * @param srcChoice the source choice field.
     * @return true if experimenter.
     */
    static boolean isExperimenter(final SrcChoice srcChoice) {
        return srcChoice instanceof SrcNxRegCase
                ? REG_SRC_CHOICE_TO_NXMHEADER.get(srcChoice).isExperimenter()
                : SRC_CHOICE_TYPE_TO_NXMHEADER.get(srcChoice.implementedInterface()).isExperimenter();
    }

    /**
     * Check if the destination choice field is experimenter.
     *
     * @param dstChoice the destination choice field.
     * @return true if experimenter.
     */
    static boolean isExperimenter(final DstChoice dstChoice) {
        return dstChoice instanceof DstNxRegCase
                ? REG_DST_CHOICE_TO_NXMHEADER.get(dstChoice).isExperimenter()
                : DST_CHOICE_TYPE_TO_NXMHEADER.get(dstChoice.implementedInterface()).isExperimenter();
    }
}
