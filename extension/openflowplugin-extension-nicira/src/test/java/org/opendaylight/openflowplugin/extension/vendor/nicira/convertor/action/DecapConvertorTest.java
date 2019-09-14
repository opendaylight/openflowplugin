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
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.decap.grouping.NxActionDecap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionDecapNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecapNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionDecapNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecapNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionDecapNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecapNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.decap.grouping.NxDecap;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class DecapConvertorTest {

    @Mock
    private NxActionDecapNodesNodeTableFlowWriteActionsCase actionsCase;

    @Mock
    private Action action;

    private static final Uint32 PACKET_TYPE = Uint32.valueOf(0xFFFEL);

    private DecapConvertor decapConvertor;

    @Before
    public void setUp() {
        NxDecap nxDecap = Mockito.mock(NxDecap.class);
        when(nxDecap.getPacketType()).thenReturn(PACKET_TYPE);
        when(actionsCase.getNxDecap()).thenReturn(nxDecap);

        NxActionDecap nxActionDecap = Mockito.mock(NxActionDecap.class);
        when(nxActionDecap.getPacketType()).thenReturn(PACKET_TYPE);
        ActionDecap actionDecap = Mockito.mock(ActionDecap.class);
        when(actionDecap.getNxActionDecap()).thenReturn(nxActionDecap);
        when(action.getActionChoice()).thenReturn(actionDecap);

        decapConvertor = new DecapConvertor();
    }

    @Test
    public void testConvertSalToOf() {
        ActionDecap actionDecap = (ActionDecap) decapConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(PACKET_TYPE, actionDecap.getNxActionDecap().getPacketType());
    }

    @Test
    public void testConvertOfToSal() {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = decapConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = decapConvertor.convert(action, ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = decapConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = decapConvertor.convert(action, ActionPath.GROUP_DESC_STATS_UPDATED_BUCKET_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = decapConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = decapConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_WRITE_ACTIONS);

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionDecapNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxDecap().getPacketType());

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionDecapNodesNodeTableFlowWriteActionsCase) actionResult1).getNxDecap().getPacketType());

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionDecapNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxDecap().getPacketType());

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionDecapNotifGroupDescStatsUpdatedCase) actionResult3).getNxDecap().getPacketType());

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionDecapNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxDecap()
                        .getPacketType());

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionDecapNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxDecap()
                        .getPacketType());
    }
}