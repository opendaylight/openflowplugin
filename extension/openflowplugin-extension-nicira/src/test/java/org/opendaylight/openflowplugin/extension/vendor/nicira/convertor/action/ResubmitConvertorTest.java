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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.resubmit.grouping.NxActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionResubmitNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionResubmitNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionResubmitNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionResubmitNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.resubmit.grouping.NxResubmit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link ResubmitConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResubmitConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(ResubmitConvertorTest.class);

    @Mock
    private NxActionResubmitNodesNodeTableFlowWriteActionsCase actionsCase;

    @Mock
    private Action action;

    private ResubmitConvertor resubmitConvertor;

    @Before
    public void setUp() throws Exception {
        final NxResubmit nxResubmit = Mockito.mock(NxResubmit.class);
        when(actionsCase.getNxResubmit()).thenReturn(nxResubmit);
        when(nxResubmit.getInPort()).thenReturn(1);
        when(nxResubmit.getTable()).thenReturn((short) 2);

        final ActionResubmit actionResubmit = Mockito.mock(ActionResubmit.class);
        final NxActionResubmit nxActionResubmit = Mockito.mock(NxActionResubmit.class);
        when(nxActionResubmit.getInPort()).thenReturn(3);
        when(nxActionResubmit.getTable()).thenReturn((short) 4);
        when(actionResubmit.getNxActionResubmit()).thenReturn(nxActionResubmit);
        when(action.getActionChoice()).thenReturn(actionResubmit);

        resubmitConvertor = new ResubmitConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final ActionResubmit actionResubmit = (ActionResubmit) resubmitConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(1, actionResubmit.getNxActionResubmit().getInPort().intValue());
        Assert.assertEquals(2, actionResubmit.getNxActionResubmit().getTable().intValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = resubmitConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = resubmitConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = resubmitConvertor.convert(action, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = resubmitConvertor.convert(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = resubmitConvertor.convert(action, ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = resubmitConvertor.convert(action, ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);

        Assert.assertEquals(3, ((NxActionResubmitNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxResubmit().getInPort().intValue());
        Assert.assertEquals(4, ((NxActionResubmitNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxResubmit().getTable().intValue());

        Assert.assertEquals(3, ((NxActionResubmitNotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxResubmit().getInPort().intValue());
        Assert.assertEquals(4, ((NxActionResubmitNotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxResubmit().getTable().intValue());

        Assert.assertEquals(3, ((NxActionResubmitNotifGroupDescStatsUpdatedCase) actionResult2).getNxResubmit().getInPort().intValue());
        Assert.assertEquals(4, ((NxActionResubmitNotifGroupDescStatsUpdatedCase) actionResult2).getNxResubmit().getTable().intValue());

        Assert.assertEquals(3, ((NxActionResubmitNodesNodeTableFlowWriteActionsCase) actionResult3).getNxResubmit().getInPort().intValue());
        Assert.assertEquals(4, ((NxActionResubmitNodesNodeTableFlowWriteActionsCase) actionResult3).getNxResubmit().getTable().intValue());

        Assert.assertEquals(3, ((NxActionResubmitNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxResubmit().getInPort().intValue());
        Assert.assertEquals(4, ((NxActionResubmitNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxResubmit().getTable().intValue());

        Assert.assertEquals(3, ((NxActionResubmitNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxResubmit().getInPort().intValue());
        Assert.assertEquals(4, ((NxActionResubmitNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxResubmit().getTable().intValue());
    }
}