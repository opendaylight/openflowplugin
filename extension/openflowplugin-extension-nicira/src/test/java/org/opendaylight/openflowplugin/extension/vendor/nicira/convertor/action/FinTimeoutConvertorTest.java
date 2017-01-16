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
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionFinTimeout;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.fin.timeout.grouping.NxActionFinTimeout;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionFinTimeoutNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionFinTimeoutNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionFinTimeoutNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionFinTimeoutNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionFinTimeoutNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionFinTimeoutNodesNodeTableFlowWriteActionsCase;

/**
 * Test for {@link FinTimeoutConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FinTimeoutConvertorTest {

    @Mock
    private NxActionFinTimeoutNodesNodeTableFlowWriteActionsCase actionsCase;

    @Mock
    private Action action;

    private FinTimeoutConvertor finTimeoutConvertor;

    @Before
    public void setUp() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.fin.timeout.grouping.NxActionFinTimeout nxFinTimeout =
                Mockito.mock(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.fin.timeout.grouping.NxActionFinTimeout.class);
        when(actionsCase.getNxActionFinTimeout()).thenReturn(nxFinTimeout);
        when(nxFinTimeout.getFinIdleTimeout()).thenReturn(1);
        when(nxFinTimeout.getFinHardTimeout()).thenReturn(2);

        final ActionFinTimeout actionFinTimeout = Mockito.mock(ActionFinTimeout.class);
        final NxActionFinTimeout nxActionFinTimeout = Mockito.mock(NxActionFinTimeout.class);
        when(nxActionFinTimeout.getFinIdleTimeout()).thenReturn(3);
        when(nxActionFinTimeout.getFinHardTimeout()).thenReturn(4);
        when(actionFinTimeout.getNxActionFinTimeout()).thenReturn(nxActionFinTimeout);
        when(action.getActionChoice()).thenReturn(actionFinTimeout);

        finTimeoutConvertor = new FinTimeoutConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final ActionFinTimeout actionFinTimeout =
                (ActionFinTimeout) finTimeoutConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(1, actionFinTimeout.getNxActionFinTimeout().getFinIdleTimeout().intValue());
        Assert.assertEquals(2, actionFinTimeout.getNxActionFinTimeout().getFinHardTimeout().intValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult =
                finTimeoutConvertor.convert(action,
                        ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1 =
                finTimeoutConvertor.convert(action,
                        ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2 =
                finTimeoutConvertor.convert(action,
                        ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3 =
                finTimeoutConvertor.convert(action,
                        ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4 =
                finTimeoutConvertor.convert(action,
                        ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5 =
                finTimeoutConvertor.convert(action,
                        ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);

        Assert.assertEquals(3, ((NxActionFinTimeoutNotifFlowsStatisticsUpdateApplyActionsCase) actionResult)
                .getNxActionFinTimeout().getFinIdleTimeout().intValue());
        Assert.assertEquals(4, ((NxActionFinTimeoutNotifFlowsStatisticsUpdateApplyActionsCase) actionResult)
                .getNxActionFinTimeout().getFinHardTimeout().intValue());

        Assert.assertEquals(3, ((NxActionFinTimeoutNotifFlowsStatisticsUpdateWriteActionsCase) actionResult1)
                .getNxActionFinTimeout().getFinIdleTimeout().intValue());
        Assert.assertEquals(4, ((NxActionFinTimeoutNotifFlowsStatisticsUpdateWriteActionsCase) actionResult1)
                .getNxActionFinTimeout().getFinHardTimeout().intValue());

        Assert.assertEquals(3, ((NxActionFinTimeoutNotifGroupDescStatsUpdatedCase) actionResult2)
                .getNxActionFinTimeout().getFinIdleTimeout().intValue());
        Assert.assertEquals(4, ((NxActionFinTimeoutNotifGroupDescStatsUpdatedCase) actionResult2)
                .getNxActionFinTimeout().getFinHardTimeout().intValue());

        Assert.assertEquals(3, ((NxActionFinTimeoutNodesNodeTableFlowWriteActionsCase) actionResult3)
                .getNxActionFinTimeout().getFinIdleTimeout().intValue());
        Assert.assertEquals(4, ((NxActionFinTimeoutNodesNodeTableFlowWriteActionsCase) actionResult3)
                .getNxActionFinTimeout().getFinHardTimeout().intValue());

        Assert.assertEquals(3, ((NxActionFinTimeoutNotifDirectStatisticsUpdateApplyActionsCase) actionResult4)
                .getNxActionFinTimeout().getFinIdleTimeout().intValue());
        Assert.assertEquals(4, ((NxActionFinTimeoutNotifDirectStatisticsUpdateApplyActionsCase) actionResult4)
                .getNxActionFinTimeout().getFinHardTimeout().intValue());

        Assert.assertEquals(3, ((NxActionFinTimeoutNotifDirectStatisticsUpdateWriteActionsCase) actionResult5)
                .getNxActionFinTimeout().getFinIdleTimeout().intValue());
        Assert.assertEquals(4, ((NxActionFinTimeoutNotifDirectStatisticsUpdateWriteActionsCase) actionResult5)
                .getNxActionFinTimeout().getFinHardTimeout().intValue());
    }
}