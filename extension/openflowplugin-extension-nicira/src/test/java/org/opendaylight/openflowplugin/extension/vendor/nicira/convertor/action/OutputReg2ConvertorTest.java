/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.output.reg2.grouping.NxActionOutputReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionOutputRegNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionOutputRegNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionOutputRegNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionOutputRegNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.output.reg.grouping.NxOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.output.reg.grouping.nx.output.reg.Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIdCaseBuilder;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;

@RunWith(MockitoJUnitRunner.class)
public class OutputReg2ConvertorTest {

    private static final Uint16 OFS_N_BITS = Uint16.ONE;
    private static final Uint16 MAX_LEN = Uint16.valueOf(2);
    private static final SrcNxTunIdCase SRC_NX_TUN_ID_CASE = new SrcNxTunIdCaseBuilder()
        .setNxTunId(Empty.getInstance()).build();

    @Mock
    private NxActionOutputRegNodesNodeGroupBucketsBucketActionsCase actionsCase;

    @Mock
    private Action action;


    private OutputReg2Convertor outputReg2Convertor;

    @Before
    public void setUp() {
        final NxOutputReg nxOutputReg = Mockito.mock(NxOutputReg.class);
        final Src src = Mockito.mock(Src.class);

        when(src.getOfsNbits()).thenReturn(OFS_N_BITS);
        when(nxOutputReg.getSrc()).thenReturn(src);
        when(nxOutputReg.getSrc().getSrcChoice()).thenReturn(SRC_NX_TUN_ID_CASE);
        when(nxOutputReg.getMaxLen()).thenReturn(MAX_LEN);
        when(actionsCase.getNxOutputReg()).thenReturn(nxOutputReg);

        final ActionOutputReg2 actionOutputReg2 = Mockito.mock(ActionOutputReg2.class);
        final NxActionOutputReg2 nxActionOutputReg2 = Mockito.mock(NxActionOutputReg2.class);
        when(nxActionOutputReg2.getSrc()).thenReturn(
                NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toUint64());
        when(nxActionOutputReg2.getMaxLen()).thenReturn(MAX_LEN);
        when(nxActionOutputReg2.getNBits()).thenReturn(OFS_N_BITS);
        when(actionOutputReg2.getNxActionOutputReg2()).thenReturn(nxActionOutputReg2);
        when(action.getActionChoice()).thenReturn(actionOutputReg2);

        outputReg2Convertor = new OutputReg2Convertor();
    }

    @Test
    public void testConvertSalToOf() {
        final ActionOutputReg2 actionOutputReg2 =
                (ActionOutputReg2) outputReg2Convertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(OFS_N_BITS, actionOutputReg2.getNxActionOutputReg2().getNBits());
        Assert.assertEquals(MAX_LEN, actionOutputReg2.getNxActionOutputReg2().getMaxLen());
        Assert.assertEquals(NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toUint64(),
                actionOutputReg2.getNxActionOutputReg2().getSrc());
    }

    @Test
    public void testConvertOfToSal() {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = outputReg2Convertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = outputReg2Convertor.convert(action, ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = outputReg2Convertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = outputReg2Convertor.convert(action, ActionPath.GROUP_DESC_STATS_UPDATED_BUCKET_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = outputReg2Convertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = outputReg2Convertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_WRITE_ACTIONS);

        Assert.assertEquals(MAX_LEN,
                ((NxActionOutputRegNotifFlowsStatisticsUpdateApplyActionsCase) actionResult)
                        .getNxOutputReg().getMaxLen());
        Assert.assertEquals(OFS_N_BITS,
                ((NxActionOutputRegNotifFlowsStatisticsUpdateApplyActionsCase) actionResult)
                        .getNxOutputReg().getSrc().getOfsNbits());
        Assert.assertEquals(SRC_NX_TUN_ID_CASE,
                ((NxActionOutputRegNotifFlowsStatisticsUpdateApplyActionsCase) actionResult)
                        .getNxOutputReg().getSrc().getSrcChoice());

        Assert.assertEquals(MAX_LEN,
                ((NxActionOutputRegNodesNodeTableFlowWriteActionsCase) actionResult1)
                        .getNxOutputReg().getMaxLen());
        Assert.assertEquals(OFS_N_BITS,
                ((NxActionOutputRegNodesNodeTableFlowWriteActionsCase) actionResult1)
                        .getNxOutputReg().getSrc().getOfsNbits());
        Assert.assertEquals(SRC_NX_TUN_ID_CASE,
                ((NxActionOutputRegNodesNodeTableFlowWriteActionsCase) actionResult1)
                        .getNxOutputReg().getSrc().getSrcChoice());

        Assert.assertEquals(MAX_LEN,
                ((NxActionOutputRegNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2)
                        .getNxOutputReg().getMaxLen());
        Assert.assertEquals(OFS_N_BITS,
                ((NxActionOutputRegNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2)
                        .getNxOutputReg().getSrc().getOfsNbits());
        Assert.assertEquals(SRC_NX_TUN_ID_CASE,
                ((NxActionOutputRegNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2)
                        .getNxOutputReg().getSrc().getSrcChoice());

        Assert.assertEquals(MAX_LEN,
                ((NxActionOutputRegNotifGroupDescStatsUpdatedCase) actionResult3)
                        .getNxOutputReg().getMaxLen());
        Assert.assertEquals(OFS_N_BITS,
                ((NxActionOutputRegNotifGroupDescStatsUpdatedCase) actionResult3)
                .getNxOutputReg().getSrc().getOfsNbits());
        Assert.assertEquals(SRC_NX_TUN_ID_CASE,
                ((NxActionOutputRegNotifGroupDescStatsUpdatedCase) actionResult3)
                        .getNxOutputReg().getSrc().getSrcChoice());

        Assert.assertEquals(MAX_LEN,
                ((NxActionOutputRegNotifDirectStatisticsUpdateApplyActionsCase) actionResult4)
                        .getNxOutputReg().getMaxLen());
        Assert.assertEquals(OFS_N_BITS,
                ((NxActionOutputRegNotifDirectStatisticsUpdateApplyActionsCase) actionResult4)
                        .getNxOutputReg().getSrc().getOfsNbits());
        Assert.assertEquals(SRC_NX_TUN_ID_CASE,
                ((NxActionOutputRegNotifDirectStatisticsUpdateApplyActionsCase) actionResult4)
                        .getNxOutputReg().getSrc().getSrcChoice());

        Assert.assertEquals(MAX_LEN,
                ((NxActionOutputRegNotifDirectStatisticsUpdateWriteActionsCase) actionResult5)
                        .getNxOutputReg().getMaxLen());
        Assert.assertEquals(OFS_N_BITS,
                ((NxActionOutputRegNotifDirectStatisticsUpdateWriteActionsCase) actionResult5)
                        .getNxOutputReg().getSrc().getOfsNbits());
        Assert.assertEquals(SRC_NX_TUN_ID_CASE,
                ((NxActionOutputRegNotifDirectStatisticsUpdateWriteActionsCase) actionResult5)
                        .getNxOutputReg().getSrc().getSrcChoice());
    }
}
