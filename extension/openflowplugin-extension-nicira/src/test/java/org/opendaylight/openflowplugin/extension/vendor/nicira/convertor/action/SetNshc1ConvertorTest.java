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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNshc1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nshc._1.grouping.NxActionSetNshc1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionSetNshc1NotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNshc1NotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionSetNshc1NotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNshc1NodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.set.nshc._1.grouping.NxSetNshc1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SetNshc1Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SetNshc1ConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SetNshc1ConvertorTest.class);

    @Mock
    private NxActionSetNshc1NodesNodeTableFlowWriteActionsCase actionsCase;

    @Mock
    private Action action;

    private SetNshc1Convertor setNshc1Convertor;

    @Before
    public void setUp() throws Exception {
        final NxSetNshc1 nxSetNshc1 = Mockito.mock(NxSetNshc1.class);
        when(actionsCase.getNxSetNshc1()).thenReturn(nxSetNshc1);
        when(nxSetNshc1.getNshc()).thenReturn(1L);

        final ActionSetNshc1 actionSetNshc1 = Mockito.mock(ActionSetNshc1.class);
        final NxActionSetNshc1 nxActionSetNshc1 = Mockito.mock(NxActionSetNshc1.class);
        when(nxActionSetNshc1.getNshc()).thenReturn(3L);
        when(actionSetNshc1.getNxActionSetNshc1()).thenReturn(nxActionSetNshc1);
        when(action.getActionChoice()).thenReturn(actionSetNshc1);

        setNshc1Convertor = new SetNshc1Convertor();
    }

    @Test
    public void testConvert() throws Exception {
        final ActionSetNshc1 actionSetNshc1 = (ActionSetNshc1) setNshc1Convertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(Long.valueOf(1L), actionSetNshc1.getNxActionSetNshc1().getNshc());
    }

    @Test
    public void testConvert1() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = setNshc1Convertor.convert(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = setNshc1Convertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = setNshc1Convertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = setNshc1Convertor.convert(action, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);

        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc1NodesNodeTableFlowWriteActionsCase) actionResult).getNxSetNshc1().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc1NotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxSetNshc1().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc1NotifFlowsStatisticsUpdateApplyActionsCase) actionResult2).getNxSetNshc1().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc1NotifGroupDescStatsUpdatedCase) actionResult3).getNxSetNshc1().getNshc());
    }

}