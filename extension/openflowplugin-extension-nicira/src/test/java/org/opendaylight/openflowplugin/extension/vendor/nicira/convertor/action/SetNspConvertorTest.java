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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nsp.grouping.NxActionSetNsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionSetNspNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNspNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionSetNspNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNspNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.set.nsp.grouping.NxSetNsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SetNspConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SetNspConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SetNspConvertorTest.class);

    @Mock
    private NxActionSetNspNodesNodeTableFlowWriteActionsCase actionsCase;
    @Mock
    private Action action;

    private SetNspConvertor setNspConvertor;

    @Before
    public void setUp() throws Exception {
        final NxSetNsp nxSetNsp = Mockito.mock(NxSetNsp.class);
        when(actionsCase.getNxSetNsp()).thenReturn(nxSetNsp);
        when(nxSetNsp.getNsp()).thenReturn(1L);

        final ActionSetNsp actionSetNsp = Mockito.mock(ActionSetNsp.class);
        final NxActionSetNsp nxActionSetNsp = Mockito.mock(NxActionSetNsp.class);
        when(nxActionSetNsp.getNsp()).thenReturn(3L);
        when(actionSetNsp.getNxActionSetNsp()).thenReturn(nxActionSetNsp);
        when(action.getActionChoice()).thenReturn(actionSetNsp);

        setNspConvertor = new SetNspConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final ActionSetNsp actionSetNsp = (ActionSetNsp) setNspConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(Long.valueOf(1L), actionSetNsp.getNxActionSetNsp().getNsp());
    }

    @Test
    public void testConvert1() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = setNspConvertor.convert(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = setNspConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = setNspConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = setNspConvertor.convert(action, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);

        Assert.assertEquals(Long.valueOf(3L), ((NxActionSetNspNodesNodeTableFlowWriteActionsCase) actionResult).getNxSetNsp().getNsp());
        Assert.assertEquals(Long.valueOf(3L), ((NxActionSetNspNotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxSetNsp().getNsp());
        Assert.assertEquals(Long.valueOf(3L), ((NxActionSetNspNotifFlowsStatisticsUpdateApplyActionsCase) actionResult2).getNxSetNsp().getNsp());
        Assert.assertEquals(Long.valueOf(3L), ((NxActionSetNspNotifGroupDescStatsUpdatedCase) actionResult3).getNxSetNsp().getNsp());
    }

}