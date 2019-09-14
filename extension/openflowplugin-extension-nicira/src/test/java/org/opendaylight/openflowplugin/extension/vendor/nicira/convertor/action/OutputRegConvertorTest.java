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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.output.reg.grouping.NxActionOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionOutputRegNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionOutputRegNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionOutputRegNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionOutputRegNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.output.reg.grouping.NxOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.output.reg.grouping.nx.output.reg.Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIdCaseBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link OutputRegConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OutputRegConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(OutputRegConvertorTest.class);

    @Mock
    private NxActionOutputRegNodesNodeGroupBucketsBucketActionsCase actionsCase;

    @Mock
    private Action action;

    private OutputRegConvertor outputRegConvertor;

    @Before
    public void setUp() {
        final NxOutputReg nxOutputReg = Mockito.mock(NxOutputReg.class);
        final Src src = Mockito.mock(Src.class);

        when(src.getOfsNbits()).thenReturn(Uint16.valueOf(1));
        when(nxOutputReg.getSrc()).thenReturn(src);
        when(nxOutputReg.getSrc().getSrcChoice()).thenReturn(new SrcNxTunIdCaseBuilder().build());
        when(nxOutputReg.getMaxLen()).thenReturn(Uint16.valueOf(2));
        when(actionsCase.getNxOutputReg()).thenReturn(nxOutputReg);

        final ActionOutputReg actionOutputReg = Mockito.mock(ActionOutputReg.class);
        final NxActionOutputReg nxActionOutputReg = Mockito.mock(NxActionOutputReg.class);
        when(nxActionOutputReg.getSrc()).thenReturn(
            Uint32.valueOf(NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong()));
        when(nxActionOutputReg.getMaxLen()).thenReturn(Uint16.valueOf(3));
        when(nxActionOutputReg.getNBits()).thenReturn(Uint16.valueOf(4));
        when(actionOutputReg.getNxActionOutputReg()).thenReturn(nxActionOutputReg);
        when(action.getActionChoice()).thenReturn(actionOutputReg);

        outputRegConvertor = new OutputRegConvertor();
    }

    @Test
    public void testConvert() {
        final ActionOutputReg actionOutputReg =
                (ActionOutputReg) outputRegConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(Uint16.valueOf(1), actionOutputReg.getNxActionOutputReg().getNBits());
        Assert.assertEquals(Uint16.valueOf(2), actionOutputReg.getNxActionOutputReg().getMaxLen());
    }

    @Test
    public void testConvert1() {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = outputRegConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = outputRegConvertor.convert(action, ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = outputRegConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = outputRegConvertor.convert(action, ActionPath.GROUP_DESC_STATS_UPDATED_BUCKET_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = outputRegConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = outputRegConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_WRITE_ACTIONS);

        Assert.assertEquals(Uint16.valueOf(3),
                ((NxActionOutputRegNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxOutputReg()
                        .getMaxLen());
        Assert.assertEquals(Uint16.valueOf(4),
                ((NxActionOutputRegNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxOutputReg().getSrc()
                        .getOfsNbits());

        Assert.assertEquals(Uint16.valueOf(3),
                ((NxActionOutputRegNodesNodeTableFlowWriteActionsCase) actionResult1).getNxOutputReg().getMaxLen());
        Assert.assertEquals(Uint16.valueOf(4), ((NxActionOutputRegNodesNodeTableFlowWriteActionsCase) actionResult1)
                .getNxOutputReg().getSrc().getOfsNbits());

        Assert.assertEquals(Uint16.valueOf(3),
                ((NxActionOutputRegNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxOutputReg()
                        .getMaxLen());
        Assert.assertEquals(Uint16.valueOf(4),
                ((NxActionOutputRegNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxOutputReg().getSrc()
                        .getOfsNbits());

        Assert.assertEquals(Uint16.valueOf(3),
                ((NxActionOutputRegNotifGroupDescStatsUpdatedCase) actionResult3).getNxOutputReg().getMaxLen());
        Assert.assertEquals(Uint16.valueOf(4), ((NxActionOutputRegNotifGroupDescStatsUpdatedCase) actionResult3)
                .getNxOutputReg().getSrc().getOfsNbits());

        Assert.assertEquals(Uint16.valueOf(3),
                ((NxActionOutputRegNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxOutputReg()
                        .getMaxLen());
        Assert.assertEquals(Uint16.valueOf(4),
                ((NxActionOutputRegNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxOutputReg().getSrc()
                        .getOfsNbits());

        Assert.assertEquals(Uint16.valueOf(3),
                ((NxActionOutputRegNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxOutputReg()
                        .getMaxLen());
        Assert.assertEquals(Uint16.valueOf(4),
                ((NxActionOutputRegNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxOutputReg().getSrc()
                        .getOfsNbits());
    }
}
