/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
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

@RunWith(Parameterized.class)
public class FieldChoiceResolverTest {

    @Parameter(0)
    public NxmHeader header;

    @Parameter(1)
    public DstChoice dstChoiceIn;

    @Parameter(2)
    public DstChoice dstChoiceOut;

    @Parameter(3)
    public SrcChoice srcChoiceIn;

    @Parameter(4)
    public SrcChoice srcChoiceOut;

    @Test
    public void testResolveChoiceToHeader() {
        if (srcChoiceOut != null) {
            if (header.isExperimenter()) {
                Assert.assertEquals(header.toBigInteger(), FieldChoiceResolver.resolveSrcHeaderUint64(srcChoiceIn));
            } else {
                Assert.assertEquals(header.toLong(),
                        FieldChoiceResolver.resolveSrcHeaderUint32(srcChoiceIn).longValue());
                Assert.assertEquals(header.toBigInteger(), FieldChoiceResolver.resolveSrcHeaderUint64(srcChoiceIn));
            }
        }
        if (dstChoiceOut != null) {
            if (header.isExperimenter()) {
                Assert.assertEquals(header.toBigInteger(), FieldChoiceResolver.resolveDstHeaderUint64(dstChoiceIn));
            } else {
                Assert.assertEquals(header.toLong(),
                        FieldChoiceResolver.resolveDstHeaderUint32(dstChoiceIn).longValue());
                Assert.assertEquals(header.toBigInteger(), FieldChoiceResolver.resolveDstHeaderUint64(dstChoiceIn));
            }
        }
    }

    @Test
    public void testResolveHeaderToChoice() {
        if (srcChoiceOut != null) {
            Assert.assertEquals(srcChoiceOut, FieldChoiceResolver.resolveSrcChoice(header.toLong()));
            Assert.assertEquals(srcChoiceOut, FieldChoiceResolver.resolveSrcChoice(header.toBigInteger()));
        }
        if (dstChoiceOut != null) {
            Assert.assertEquals(dstChoiceOut, FieldChoiceResolver.resolveDstChoice(header.toLong()));
            Assert.assertEquals(dstChoiceOut, FieldChoiceResolver.resolveDstChoice(header.toBigInteger()));
        }
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {
                NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask(),
                new DstNxTunIdCaseBuilder().build(),
                new DstNxTunIdCaseBuilder().setNxTunId(true).build(),
                new SrcNxTunIdCaseBuilder().build(),
                new SrcNxTunIdCaseBuilder().setNxTunId(true).build()
            },
            {
                NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask(),
                new DstNxArpShaCaseBuilder().build(),
                new DstNxArpShaCaseBuilder().setNxArpSha(true).build(),
                new SrcNxArpShaCaseBuilder().build(),
                new SrcNxArpShaCaseBuilder().setNxArpSha(true).build(),
            },
            {
                NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask(),
                new DstNxArpShaCaseBuilder().build(),
                new DstNxArpShaCaseBuilder().setNxArpSha(true).build(),
                new SrcNxArpShaCaseBuilder().build(),
                new SrcNxArpShaCaseBuilder().setNxArpSha(true).build(),
            },
            {
                NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask(),
                new DstNxArpThaCaseBuilder().build(),
                new DstNxArpThaCaseBuilder().setNxArpTha(true).build(),
                new SrcNxArpThaCaseBuilder().build(),
                new SrcNxArpThaCaseBuilder().setNxArpTha(true).build(),
            },
            {
                NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask(),
                new DstOfArpOpCaseBuilder().build(),
                new DstOfArpOpCaseBuilder().setOfArpOp(true).build(),
                new SrcOfArpOpCaseBuilder().build(),
                new SrcOfArpOpCaseBuilder().setOfArpOp(true).build(),
            },
            {
                NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask(),
                new DstOfArpSpaCaseBuilder().build(),
                new DstOfArpSpaCaseBuilder().setOfArpSpa(true).build(),
                new SrcOfArpSpaCaseBuilder().build(),
                new SrcOfArpSpaCaseBuilder().setOfArpSpa(true).build(),
            },
            {
                NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask(),
                new DstOfArpTpaCaseBuilder().build(),
                new DstOfArpTpaCaseBuilder().setOfArpTpa(true).build(),
                new SrcOfArpTpaCaseBuilder().build(),
                new SrcOfArpTpaCaseBuilder().setOfArpTpa(true).build(),
            },
            {
                NiciraMatchCodecs.NXM_OF_IN_PORT_CODEC.getHeaderWithoutHasMask(),
                new DstNxOfInPortCaseBuilder().build(),
                new DstNxOfInPortCaseBuilder().setOfInPort(true).build(),
                new SrcNxOfInPortCaseBuilder().build(),
                new SrcNxOfInPortCaseBuilder().setOfInPort(true).build(),
            },
            {
                NiciraMatchCodecs.OXM_OF_METADATA_CODEC.getHeaderWithoutHasMask(),
                new DstOfMetadataCaseBuilder().build(),
                new DstOfMetadataCaseBuilder().setOfMetadata(true).build(),
                new SrcNxOfMetadataCaseBuilder().build(),
                new SrcNxOfMetadataCaseBuilder().setOfMetadata(true).build(),
            },
            {
                NiciraMatchCodecs.OXM_OF_MPLS_LABEL.getHeaderWithoutHasMask(),
                new DstOfMplsLabelCaseBuilder().build(),
                new DstOfMplsLabelCaseBuilder().setOfMplsLabel(true).build(),
                new SrcNxOfMplsLabelCaseBuilder().build(),
                new SrcNxOfMplsLabelCaseBuilder().setOfMplsLabel(true).build(),
            },
            {
                NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask(),
                new DstOfEthDstCaseBuilder().build(),
                new DstOfEthDstCaseBuilder().setOfEthDst(true).build(),
                new SrcOfEthDstCaseBuilder().build(),
                new SrcOfEthDstCaseBuilder().setOfEthDst(true).build(),
            },
            {
                NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask(),
                new DstOfEthSrcCaseBuilder().build(),
                new DstOfEthSrcCaseBuilder().setOfEthSrc(true).build(),
                new SrcOfEthSrcCaseBuilder().build(),
                new SrcOfEthSrcCaseBuilder().setOfEthSrc(true).build(),
            },
            {
                NiciraMatchCodecs.ETH_TYPE_CODEC.getHeaderWithoutHasMask(),
                null,
                null,
                new SrcOfEthTypeCaseBuilder().build(),
                new SrcOfEthTypeCaseBuilder().setOfEthType(true).build()
            },
            {
                NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask(),
                new DstNxTunIpv4DstCaseBuilder().build(),
                new DstNxTunIpv4DstCaseBuilder().setNxTunIpv4Dst(true).build(),
                new SrcNxTunIpv4DstCaseBuilder().build(),
                new SrcNxTunIpv4DstCaseBuilder().setNxTunIpv4Dst(true).build(),
            },
            {
                NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask(),
                new DstNxTunIpv4SrcCaseBuilder().build(),
                new DstNxTunIpv4SrcCaseBuilder().setNxTunIpv4Src(true).build(),
                new SrcNxTunIpv4SrcCaseBuilder().build(),
                new SrcNxTunIpv4SrcCaseBuilder().setNxTunIpv4Src(true).build(),
            },
            {
                NiciraMatchCodecs.NSP_CODEC.getHeaderWithoutHasMask(),
                new DstNxNspCaseBuilder().build(),
                new DstNxNspCaseBuilder().setNxNspDst(true).build(),
                new SrcNxNspCaseBuilder().build(),
                new SrcNxNspCaseBuilder().setNxNspDst(true).build(),
            },
            {
                NiciraMatchCodecs.NSI_CODEC.getHeaderWithoutHasMask(),
                new DstNxNsiCaseBuilder().build(),
                new DstNxNsiCaseBuilder().setNxNsiDst(true).build(),
                new SrcNxNsiCaseBuilder().build(),
                new SrcNxNsiCaseBuilder().setNxNsiDst(true).build(),
            },
            {
                NiciraMatchCodecs.NSC1_CODEC.getHeaderWithoutHasMask(),
                new DstNxNshc1CaseBuilder().build(),
                new DstNxNshc1CaseBuilder().setNxNshc1Dst(true).build(),
                new SrcNxNshc1CaseBuilder().build(),
                new SrcNxNshc1CaseBuilder().setNxNshc1Dst(true).build(),
            },
            {
                NiciraMatchCodecs.NSC2_CODEC.getHeaderWithoutHasMask(),
                new DstNxNshc2CaseBuilder().build(),
                new DstNxNshc2CaseBuilder().setNxNshc2Dst(true).build(),
                new SrcNxNshc2CaseBuilder().build(),
                new SrcNxNshc2CaseBuilder().setNxNshc2Dst(true).build(),
            },
            {
                NiciraMatchCodecs.NSC3_CODEC.getHeaderWithoutHasMask(),
                new DstNxNshc3CaseBuilder().build(),
                new DstNxNshc3CaseBuilder().setNxNshc3Dst(true).build(),
                new SrcNxNshc3CaseBuilder().build(),
                new SrcNxNshc3CaseBuilder().setNxNshc3Dst(true).build(),
            },
            {
                NiciraMatchCodecs.NSC4_CODEC.getHeaderWithoutHasMask(),
                new DstNxNshc4CaseBuilder().build(),
                new DstNxNshc4CaseBuilder().setNxNshc4Dst(true).build(),
                new SrcNxNshc4CaseBuilder().build(),
                new SrcNxNshc4CaseBuilder().setNxNshc4Dst(true).build(),
            },
            {
                NiciraMatchCodecs.NSH_FLAGS_CODEC.getHeaderWithoutHasMask(),
                new DstNxNshFlagsCaseBuilder().build(),
                new DstNxNshFlagsCaseBuilder().setNxNshFlags(true).build(),
                new SrcNxNshFlagsCaseBuilder().build(),
                new SrcNxNshFlagsCaseBuilder().setNxNshFlags(true).build(),
            },
            {
                NiciraMatchCodecs.NSH_TTL_CODEC.getHeaderWithoutHasMask(),
                new DstNxNshTtlCaseBuilder().build(),
                new DstNxNshTtlCaseBuilder().setNxNshTtl(true).build(),
                new SrcNxNshTtlCaseBuilder().build(),
                new SrcNxNshTtlCaseBuilder().setNxNshTtl(true).build(),
            },
            {
                NiciraMatchCodecs.NSH_MDTYPE_CODEC.getHeaderWithoutHasMask(),
                null,
                null,
                new SrcNxNshMdtypeCaseBuilder().build(),
                new SrcNxNshMdtypeCaseBuilder().setNxNshMdtype(true).build(),
            },
            {
                NiciraMatchCodecs.NSH_NP_CODEC.getHeaderWithoutHasMask(),
                null,
                null,
                new SrcNxNshNpCaseBuilder().build(),
                new SrcNxNshNpCaseBuilder().setNxNshNp(true).build(),
            },
            {
                NiciraMatchCodecs.IP_SRC_CODEC.getHeaderWithoutHasMask(),
                new DstOfIpSrcCaseBuilder().build(),
                new DstOfIpSrcCaseBuilder().setOfIpSrc(true).build(),
                new SrcOfIpSrcCaseBuilder().build(),
                new SrcOfIpSrcCaseBuilder().setOfIpSrc(true).build(),
            },
            {
                NiciraMatchCodecs.IP_DST_CODEC.getHeaderWithoutHasMask(),
                new DstOfIpDstCaseBuilder().build(),
                new DstOfIpDstCaseBuilder().setOfIpDst(true).build(),
                new SrcOfIpDstCaseBuilder().build(),
                new SrcOfIpDstCaseBuilder().setOfIpDst(true).build(),
            },
            {
                NiciraMatchCodecs.IPV6_SRC_CODEC.getHeaderWithoutHasMask(),
                new DstNxIpv6SrcCaseBuilder().build(),
                new DstNxIpv6SrcCaseBuilder().setNxIpv6Src(true).build(),
                new SrcNxIpv6SrcCaseBuilder().build(),
                new SrcNxIpv6SrcCaseBuilder().setNxIpv6Src(true).build(),
            },
            {
                NiciraMatchCodecs.IPV6_DST_CODEC.getHeaderWithoutHasMask(),
                new DstNxIpv6DstCaseBuilder().build(),
                new DstNxIpv6DstCaseBuilder().setNxIpv6Dst(true).build(),
                new SrcNxIpv6DstCaseBuilder().build(),
                new SrcNxIpv6DstCaseBuilder().setNxIpv6Dst(true).build(),
            },
            {
                NiciraMatchCodecs.ICMP_TYPE_CODEC.getHeaderWithoutHasMask(),
                new DstOfIcmpTypeCaseBuilder().build(),
                new DstOfIcmpTypeCaseBuilder().setOfIcmpType(true).build(),
                null,
                null
            },
            {
                NiciraMatchCodecs.REG0_CODEC.getHeaderWithoutHasMask(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg0.class).build(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg0.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg0.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg0.class).build()
            },
            {
                NiciraMatchCodecs.REG1_CODEC.getHeaderWithoutHasMask(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg1.class).build(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg1.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg1.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg1.class).build()
            },
            {
                NiciraMatchCodecs.REG2_CODEC.getHeaderWithoutHasMask(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg2.class).build(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg2.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg2.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg2.class).build()
            },
            {
                NiciraMatchCodecs.REG3_CODEC.getHeaderWithoutHasMask(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg3.class).build(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg3.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg3.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg3.class).build()
            },
            {
                NiciraMatchCodecs.REG4_CODEC.getHeaderWithoutHasMask(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg4.class).build(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg4.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg4.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg4.class).build()
            },
            {
                NiciraMatchCodecs.REG5_CODEC.getHeaderWithoutHasMask(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg5.class).build(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg5.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg5.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg5.class).build()
            },
            {
                NiciraMatchCodecs.REG6_CODEC.getHeaderWithoutHasMask(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg6.class).build(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg6.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg6.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg6.class).build()
            },
            {
                NiciraMatchCodecs.REG7_CODEC.getHeaderWithoutHasMask(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg7.class).build(),
                new DstNxRegCaseBuilder().setNxReg(NxmNxReg7.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg7.class).build(),
                new SrcNxRegCaseBuilder().setNxReg(NxmNxReg7.class).build()
            }
        });
    }

}