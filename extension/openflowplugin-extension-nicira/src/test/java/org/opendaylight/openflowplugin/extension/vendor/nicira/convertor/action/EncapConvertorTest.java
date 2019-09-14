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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionEncap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.encap.grouping.NxActionEncap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionEncapNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionEncapNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionEncapNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionEncapNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionEncapNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionEncapNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.encap.grouping.NxEncap;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class EncapConvertorTest {

    @Mock
    private NxActionEncapNodesNodeTableFlowWriteActionsCase actionsCase;

    @Mock
    private Action action;

    private static final Uint32 PACKET_TYPE = Uint32.valueOf(0x1894FL);

    private EncapConvertor encapConvertor;

    @Before
    public void setUp() {
        NxEncap nxEncap = Mockito.mock(NxEncap.class);
        when(nxEncap.getPacketType()).thenReturn(PACKET_TYPE);
        when(actionsCase.getNxEncap()).thenReturn(nxEncap);

        NxActionEncap nxActionEncap = Mockito.mock(NxActionEncap.class);
        when(nxActionEncap.getPacketType()).thenReturn(PACKET_TYPE);
        ActionEncap actionEncap = Mockito.mock(ActionEncap.class);
        when(actionEncap.getNxActionEncap()).thenReturn(nxActionEncap);
        when(action.getActionChoice()).thenReturn(actionEncap);

        encapConvertor = new EncapConvertor();
    }

    @Test
    public void testConvertSalToOf() {
        ActionEncap actionEncap = (ActionEncap) encapConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(PACKET_TYPE, actionEncap.getNxActionEncap().getPacketType());
    }

    @Test
    public void testConvertOfToSal() {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = encapConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = encapConvertor.convert(action, ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = encapConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = encapConvertor.convert(action, ActionPath.GROUP_DESC_STATS_UPDATED_BUCKET_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = encapConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = encapConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_WRITE_ACTIONS);

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionEncapNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxEncap().getPacketType());

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionEncapNodesNodeTableFlowWriteActionsCase) actionResult1).getNxEncap().getPacketType());

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionEncapNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxEncap().getPacketType());

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionEncapNotifGroupDescStatsUpdatedCase) actionResult3).getNxEncap().getPacketType());

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionEncapNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxEncap()
                        .getPacketType());

        Assert.assertEquals(PACKET_TYPE,
                ((NxActionEncapNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxEncap()
                        .getPacketType());
    }
}