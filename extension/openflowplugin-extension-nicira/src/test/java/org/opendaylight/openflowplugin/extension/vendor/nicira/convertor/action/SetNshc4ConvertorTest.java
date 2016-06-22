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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNshc4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nshc._4.grouping.NxActionSetNshc4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionSetNshc4NotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNshc4NotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionSetNshc4NotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNshc4NodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.set.nshc._4.grouping.NxSetNshc4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SetNshc4Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SetNshc4ConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SetNshc4ConvertorTest.class);

    @Mock
    private NxActionSetNshc4NodesNodeTableFlowWriteActionsCase actionsCase;
    @Mock
    private Action action;
    private SetNshc4Convertor setNshc4Convertor;

    @Before
    public void setUp() throws Exception {
        final NxSetNshc4 nxSetNshc4 = Mockito.mock(NxSetNshc4.class);
        when(actionsCase.getNxSetNshc4()).thenReturn(nxSetNshc4);
        when(nxSetNshc4.getNshc()).thenReturn(1L);

        final ActionSetNshc4 actionSetNshc4 = Mockito.mock(ActionSetNshc4.class);
        final NxActionSetNshc4 nxActionSetNshc4 = Mockito.mock(NxActionSetNshc4.class);
        when(nxActionSetNshc4.getNshc()).thenReturn(3L);
        when(actionSetNshc4.getNxActionSetNshc4()).thenReturn(nxActionSetNshc4);
        when(action.getActionChoice()).thenReturn(actionSetNshc4);

        setNshc4Convertor = new SetNshc4Convertor();
    }

    @Test
    public void testConvert() throws Exception {
        final ActionSetNshc4 actionSetNshc4 = (ActionSetNshc4) setNshc4Convertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(Long.valueOf(1L), actionSetNshc4.getNxActionSetNshc4().getNshc());
    }

    @Test
    public void testConvert1() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = setNshc4Convertor.convert(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = setNshc4Convertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = setNshc4Convertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = setNshc4Convertor.convert(action, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);

        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc4NodesNodeTableFlowWriteActionsCase) actionResult).getNxSetNshc4().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc4NotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxSetNshc4().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc4NotifFlowsStatisticsUpdateApplyActionsCase) actionResult2).getNxSetNshc4().getNshc());
        Assert.assertEquals(Long.valueOf(3), ((NxActionSetNshc4NotifGroupDescStatsUpdatedCase) actionResult3).getNxSetNshc4().getNshc());
    }
}