/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.nx.NiciraMatchCodecs;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.move.grouping.NxActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxArpShaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxArpThaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc1Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc2Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc3Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc4Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNsiCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNspCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIpv4DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIpv4SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpOpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpSpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpTpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfEthDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfEthSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfIcmpTypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfIpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfIpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionRegMoveNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.NxRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxArpShaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxArpThaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshc1Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshc2Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshc3Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNshc4Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNsiCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxNspCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIpv4DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIpv4SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpOpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpSpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpTpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthTypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfIpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfIpSrcCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link RegMoveConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RegMoveConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegMoveConvertorTest.class);

    @Mock
    private NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCase actionsCase;
    @Mock
    private Action action;
    @Mock
    private NxRegMove nxRegMove;

    private RegMoveConvertor regMoveConvertor;


    @Before
    public void setUp() throws Exception {
        final Src src = Mockito.mock(Src.class);
        final Dst dst = Mockito.mock(Dst.class);

        when(actionsCase.getNxRegMove()).thenReturn(nxRegMove);
        when(nxRegMove.getSrc()).thenReturn(src);
        when(nxRegMove.getDst()).thenReturn(dst);
        when(nxRegMove.getSrc().getStart()).thenReturn(1);
        when(nxRegMove.getSrc().getEnd()).thenReturn(2);
        when(nxRegMove.getDst().getStart()).thenReturn(3);
        when(nxRegMove.getDst().getEnd()).thenReturn(4);
        when(nxRegMove.getDst().getDstChoice()).thenReturn(Mockito.mock(DstNxTunIdCase.class));
        when(nxRegMove.getSrc().getSrcChoice()).thenReturn(Mockito.mock(SrcNxTunIdCase.class));

        final ActionRegMove actionRegMove = Mockito.mock(ActionRegMove.class);
        final NxActionRegMove nxActionRegMove = Mockito.mock(NxActionRegMove.class);
        when(nxActionRegMove.getSrc()).thenReturn(NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong());
        when(nxActionRegMove.getDst()).thenReturn(NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong());

        when(nxActionRegMove.getNBits()).thenReturn(7);

        when(actionRegMove.getNxActionRegMove()).thenReturn(nxActionRegMove);
        when(action.getActionChoice()).thenReturn(actionRegMove);

        regMoveConvertor = new RegMoveConvertor();

    }

    @Test
    public void testConvert() throws Exception {
        final ActionRegMove actionRegMove = (ActionRegMove) regMoveConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(Integer.valueOf(3) ,actionRegMove.getNxActionRegMove().getDstOfs());
        Assert.assertEquals(Integer.valueOf(2) ,actionRegMove.getNxActionRegMove().getNBits());
        Assert.assertEquals(Integer.valueOf(1) ,actionRegMove.getNxActionRegMove().getSrcOfs());
    }

    @Test
    public void testConvert1() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = regMoveConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = regMoveConvertor.convert(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = regMoveConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = regMoveConvertor.convert(action, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = regMoveConvertor.convert(action, ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = regMoveConvertor.convert(action, ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);

        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxRegMove().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(6), ((NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxRegMove().getDst().getEnd());
        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegMoveNodesNodeTableFlowWriteActionsCase) actionResult1).getNxRegMove().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(6), ((NxActionRegMoveNodesNodeTableFlowWriteActionsCase) actionResult1).getNxRegMove().getDst().getEnd());
        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegMoveNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegMove().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(6), ((NxActionRegMoveNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegMove().getDst().getEnd());
        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegMoveNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegMove().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(6), ((NxActionRegMoveNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegMove().getDst().getEnd());
        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegMoveNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegMove().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(6), ((NxActionRegMoveNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegMove().getDst().getEnd());
        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegMoveNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegMove().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(6), ((NxActionRegMoveNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegMove().getDst().getEnd());

    }

    @Test
    public void testResolveDst() throws Exception {
        Assert.assertEquals(NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxTunIdCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxArpShaCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxArpThaCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstOfArpOpCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstOfArpSpaCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstOfArpTpaCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxTunIpv4DstCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxTunIpv4SrcCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstOfEthDstCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstOfEthSrcCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSP_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxNspCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSI_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxNsiCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSC1_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxNshc1Case.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSC2_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxNshc2Case.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSC3_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxNshc3Case.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSC4_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstNxNshc4Case.class)));
        Assert.assertEquals(NiciraMatchCodecs.IP_SRC_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstOfIpSrcCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.IP_DST_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstOfIpDstCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ICMP_TYPE_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveDst(Mockito.mock(DstOfIcmpTypeCase.class)));


    }

    @Test
    public void testResolveSrc() throws Exception {
        Assert.assertEquals(NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxTunIdCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxArpShaCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxArpThaCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcOfArpOpCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcOfArpSpaCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcOfArpTpaCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcOfEthDstCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcOfEthSrcCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxTunIpv4DstCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxTunIpv4SrcCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSP_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxNspCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSI_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxNsiCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSC1_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxNshc1Case.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSC2_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxNshc2Case.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSC3_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxNshc3Case.class)));
        Assert.assertEquals(NiciraMatchCodecs.NSC4_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxNshc4Case.class)));
        Assert.assertEquals(NiciraMatchCodecs.IP_SRC_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcOfIpSrcCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.IP_DST_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcOfIpDstCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxTunIpv4SrcCase.class)));
        Assert.assertEquals(NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong(),
                RegMoveConvertor.resolveSrc(Mockito.mock(SrcNxTunIpv4DstCase.class)));
    }

    @Test
    public void testResolveSrc1() throws Exception {
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxTunIdCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxArpShaCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxArpThaCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcOfArpOpCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcOfArpSpaCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcOfArpTpaCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcOfEthDstCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcOfEthSrcCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.ETH_TYPE_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcOfEthTypeCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxTunIpv4DstCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxTunIpv4SrcCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.NSP_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxNspCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.NSI_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxNsiCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.NSC1_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxNshc1Case);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.NSC2_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxNshc2Case);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.NSC3_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxNshc3Case);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.NSC4_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcNxNshc4Case);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.IP_DST_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcOfIpDstCase);
        Assert.assertTrue(RegMoveConvertor.resolveSrc(NiciraMatchCodecs.IP_SRC_CODEC.getHeaderWithoutHasMask().toLong()) instanceof SrcOfIpSrcCase);
    }

    @Test
    public void testResolveDst1() throws Exception {
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxTunIdCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxArpShaCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxArpThaCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstOfArpOpCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstOfArpSpaCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstOfArpTpaCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstOfEthDstCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstOfEthSrcCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxTunIpv4DstCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxTunIpv4SrcCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.NSP_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxNspCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.NSI_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxNsiCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.NSC1_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxNshc1Case);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.NSC2_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxNshc2Case);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.NSC3_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxNshc3Case);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.NSC4_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstNxNshc4Case);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.IP_DST_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstOfIpDstCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.IP_SRC_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstOfIpSrcCase);
        Assert.assertTrue(RegMoveConvertor.resolveDst(NiciraMatchCodecs.ICMP_TYPE_CODEC.getHeaderWithoutHasMask().toLong()) instanceof DstOfIcmpTypeCase);
    }

}