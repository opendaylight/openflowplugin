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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNshc2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nshc._2.grouping.NxActionSetNshc2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionSetNshc2NotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNshc2NotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionSetNshc2NotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNshc2NodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.set.nshc._2.grouping.NxSetNshc2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SetNshc2Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SetNshc2ConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SetNshc2ConvertorTest.class);

    @Mock
    private NxActionSetNshc2NodesNodeTableFlowWriteActionsCase actionsCase;
    @Mock
    private Action action;
    private SetNshc2Convertor setNshc2Convertor;

    @Before
    public void setUp() throws Exception {
        final NxSetNshc2 nxSetNshc2 = Mockito.mock(NxSetNshc2.class);
        when(actionsCase.getNxSetNshc2()).thenReturn(nxSetNshc2);
        when(nxSetNshc2.getNshc()).thenReturn(1L);

        final ActionSetNshc2 actionSetNshc2 = Mockito.mock(ActionSetNshc2.class);
        final NxActionSetNshc2 nxActionSetNshc2 = Mockito.mock(NxActionSetNshc2.class);
        when(nxActionSetNshc2.getNshc()).thenReturn(3L);
        when(actionSetNshc2.getNxActionSetNshc2()).thenReturn(nxActionSetNshc2);
        when(action.getActionChoice()).thenReturn(actionSetNshc2);

        setNshc2Convertor = new SetNshc2Convertor();
    }

    @Test
    public void testConvert() throws Exception {
        final ActionSetNshc2 actionSetNshc2 = (ActionSetNshc2) setNshc2Convertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(Long.valueOf(1L), actionSetNshc2.getNxActionSetNshc2().getNshc());
    }

    @Test
    public void testConvert1() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = setNshc2Convertor.convert(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = setNshc2Convertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = setNshc2Convertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = setNshc2Convertor.convert(action, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);

        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc2NodesNodeTableFlowWriteActionsCase) actionResult).getNxSetNshc2().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc2NotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxSetNshc2().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc2NotifFlowsStatisticsUpdateApplyActionsCase) actionResult2).getNxSetNshc2().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc2NotifGroupDescStatsUpdatedCase) actionResult3).getNxSetNshc2().getNshc());
    }

}