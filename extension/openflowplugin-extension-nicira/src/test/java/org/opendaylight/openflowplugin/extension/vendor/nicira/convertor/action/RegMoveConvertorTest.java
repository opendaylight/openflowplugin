/*
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
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.nx.codec.match.NiciraMatchCodecs;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.move.grouping.NxActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionRegMoveNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.NxRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIdCaseBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
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
    public void setUp() {
        final Src src = Mockito.mock(Src.class);
        final Dst dst = Mockito.mock(Dst.class);

        when(actionsCase.getNxRegMove()).thenReturn(nxRegMove);
        when(nxRegMove.getSrc()).thenReturn(src);
        when(nxRegMove.getDst()).thenReturn(dst);
        when(nxRegMove.getSrc().getStart()).thenReturn(Uint16.valueOf(1));
        when(nxRegMove.getDst().getStart()).thenReturn(Uint16.valueOf(3));
        when(nxRegMove.getDst().getEnd()).thenReturn(Uint16.valueOf(4));
        when(nxRegMove.getDst().getDstChoice()).thenReturn(new DstNxTunIdCaseBuilder().build());
        when(nxRegMove.getSrc().getSrcChoice()).thenReturn(new SrcNxTunIdCaseBuilder().build());

        final ActionRegMove actionRegMove = Mockito.mock(ActionRegMove.class);
        final NxActionRegMove nxActionRegMove = Mockito.mock(NxActionRegMove.class);
        when(nxActionRegMove.getSrc()).thenReturn(
                NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toUint64());
        when(nxActionRegMove.getDst()).thenReturn(
                NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toUint64());

        when(nxActionRegMove.getDstOfs()).thenReturn(Uint16.ZERO);
        when(nxActionRegMove.getSrcOfs()).thenReturn(Uint16.ZERO);
        when(nxActionRegMove.getNBits()).thenReturn(Uint16.valueOf(7));

        when(actionRegMove.getNxActionRegMove()).thenReturn(nxActionRegMove);
        when(action.getActionChoice()).thenReturn(actionRegMove);

        regMoveConvertor = new RegMoveConvertor();

    }

    @Test
    public void testConvert() {
        final ActionRegMove actionRegMove = (ActionRegMove) regMoveConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(Uint16.valueOf(3), actionRegMove.getNxActionRegMove().getDstOfs());
        Assert.assertEquals(Uint16.valueOf(2), actionRegMove.getNxActionRegMove().getNBits());
        Assert.assertEquals(Uint16.valueOf(1), actionRegMove.getNxActionRegMove().getSrcOfs());
    }

    @Test
    public void testConvert1() {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = regMoveConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = regMoveConvertor.convert(action, ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = regMoveConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = regMoveConvertor.convert(action, ActionPath.GROUP_DESC_STATS_UPDATED_BUCKET_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = regMoveConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = regMoveConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_WRITE_ACTIONS);

        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxRegMove().getDst()
                        .getStart());
        Assert.assertEquals(Uint16.valueOf(6),
                ((NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxRegMove().getDst()
                        .getEnd());
        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegMoveNodesNodeTableFlowWriteActionsCase) actionResult1).getNxRegMove().getDst().getStart());
        Assert.assertEquals(Uint16.valueOf(6),
                ((NxActionRegMoveNodesNodeTableFlowWriteActionsCase) actionResult1).getNxRegMove().getDst().getEnd());
        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegMoveNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegMove().getDst()
                        .getStart());
        Assert.assertEquals(Uint16.valueOf(6),
                ((NxActionRegMoveNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegMove().getDst()
                        .getEnd());
        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegMoveNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegMove().getDst().getStart());
        Assert.assertEquals(Uint16.valueOf(6),
                ((NxActionRegMoveNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegMove().getDst().getEnd());
        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegMoveNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegMove().getDst()
                        .getStart());
        Assert.assertEquals(Uint16.valueOf(6),
                ((NxActionRegMoveNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegMove().getDst()
                        .getEnd());
        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegMoveNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegMove().getDst()
                        .getStart());
        Assert.assertEquals(Uint16.valueOf(6),
                ((NxActionRegMoveNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegMove().getDst()
                        .getEnd());
    }
}
