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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNshc3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nshc._3.grouping.NxActionSetNshc3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionSetNshc3NotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNshc3NotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionSetNshc3NotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNshc3NodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.set.nshc._3.grouping.NxSetNshc3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SetNshc3Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SetNshc3ConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SetNshc3ConvertorTest.class);

    @Mock
    private NxActionSetNshc3NodesNodeTableFlowWriteActionsCase actionsCase;
    @Mock
    private Action action;
    private SetNshc3Convertor setNshc3Convertor;

    @Before
    public void setUp() throws Exception {
        final NxSetNshc3 nxSetNshc3 = Mockito.mock(NxSetNshc3.class);
        when(actionsCase.getNxSetNshc3()).thenReturn(nxSetNshc3);
        when(nxSetNshc3.getNshc()).thenReturn(1L);

        final ActionSetNshc3 actionSetNshc3 = Mockito.mock(ActionSetNshc3.class);
        final NxActionSetNshc3 nxActionSetNshc3 = Mockito.mock(NxActionSetNshc3.class);
        when(nxActionSetNshc3.getNshc()).thenReturn(3L);
        when(actionSetNshc3.getNxActionSetNshc3()).thenReturn(nxActionSetNshc3);
        when(action.getActionChoice()).thenReturn(actionSetNshc3);

        setNshc3Convertor = new SetNshc3Convertor();
    }

    @Test
    public void testConvert() throws Exception {
        final ActionSetNshc3 actionSetNshc3 = (ActionSetNshc3) setNshc3Convertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(Long.valueOf(1L), actionSetNshc3.getNxActionSetNshc3().getNshc());
    }

    @Test
    public void testConvert1() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = setNshc3Convertor.convert(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = setNshc3Convertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = setNshc3Convertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = setNshc3Convertor.convert(action, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);

        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc3NodesNodeTableFlowWriteActionsCase) actionResult).getNxSetNshc3().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc3NotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxSetNshc3().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc3NotifFlowsStatisticsUpdateApplyActionsCase) actionResult2).getNxSetNshc3().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc3NotifGroupDescStatsUpdatedCase) actionResult3).getNxSetNshc3().getNshc());
    }

}