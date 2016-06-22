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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNsi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nsi.grouping.NxActionSetNsi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionSetNsiNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNsiNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionSetNsiNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNsiNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.set.nsi.grouping.NxSetNsi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SetNsiConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SetNsiConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SetNsiConvertorTest.class);

    @Mock
    private NxActionSetNsiNodesNodeTableFlowWriteActionsCase actionsCase;
    @Mock
    private Action action;
    private SetNsiConvertor setNsiConvertor;

    @Before
    public void setUp() throws Exception {
        final NxSetNsi nxSetNsi = Mockito.mock(NxSetNsi.class);
        when(actionsCase.getNxSetNsi()).thenReturn(nxSetNsi);
        when(nxSetNsi.getNsi()).thenReturn((short) 1);

        final ActionSetNsi actionSetNsi = Mockito.mock(ActionSetNsi.class);
        final NxActionSetNsi nxActionSetNsi = Mockito.mock(NxActionSetNsi.class);
        when(nxActionSetNsi.getNsi()).thenReturn((short) 3);
        when(actionSetNsi.getNxActionSetNsi()).thenReturn(nxActionSetNsi);
        when(action.getActionChoice()).thenReturn(actionSetNsi);

        setNsiConvertor = new SetNsiConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final ActionSetNsi actionSetNsi = (ActionSetNsi) setNsiConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(1, actionSetNsi.getNxActionSetNsi().getNsi().intValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = setNsiConvertor.convert(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = setNsiConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = setNsiConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = setNsiConvertor.convert(action, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);

        Assert.assertEquals(3, ((NxActionSetNsiNodesNodeTableFlowWriteActionsCase) actionResult).getNxSetNsi().getNsi().intValue());
        Assert.assertEquals(3, ((NxActionSetNsiNotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxSetNsi().getNsi().intValue());
        Assert.assertEquals(3, ((NxActionSetNsiNotifFlowsStatisticsUpdateApplyActionsCase) actionResult2).getNxSetNsi().getNsi().intValue());
        Assert.assertEquals(3, ((NxActionSetNsiNotifGroupDescStatsUpdatedCase) actionResult3).getNxSetNsi().getNsi().intValue());
    }

}