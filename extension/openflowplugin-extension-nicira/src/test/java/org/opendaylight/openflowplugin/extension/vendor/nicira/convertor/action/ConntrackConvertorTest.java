/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.NxActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionConntrackNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.conntrack.grouping.NxConntrackBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link ConntrackConvertor}.
 */
public class ConntrackConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConntrackConvertorTest.class);

    private ConntrackConvertor conntrackConvertor;

    @Before
    public void setUp() throws Exception {
        conntrackConvertor = new ConntrackConvertor();
    }

    @Test
    public void testConvertToOFJava() throws Exception {

        final NxConntrackBuilder nxConntrackBuilder = new NxConntrackBuilder()
                .setConntrackZone(1)
                .setFlags(1)
                .setRecircTable((short) 1)
                .setZoneSrc(1L);

        final NxActionConntrackNodesNodeTableFlowWriteActionsCaseBuilder nxActionConntrackBuilder =
                new NxActionConntrackNodesNodeTableFlowWriteActionsCaseBuilder()
                        .setNxConntrack(nxConntrackBuilder.build());

        final NxActionConntrackNodesNodeTableFlowWriteActionsCase actionsCase = nxActionConntrackBuilder.build();
        final ActionConntrack actionConntrack = (ActionConntrack) conntrackConvertor.convert(actionsCase).getActionChoice();

        Assert.assertEquals(actionsCase.getNxConntrack().getConntrackZone(), actionConntrack.getNxActionConntrack().getConntrackZone());
        Assert.assertEquals(actionsCase.getNxConntrack().getFlags(), actionConntrack.getNxActionConntrack().getFlags());
        Assert.assertEquals(actionsCase.getNxConntrack().getRecircTable(), actionConntrack.getNxActionConntrack().getRecircTable());
        Assert.assertEquals(actionsCase.getNxConntrack().getZoneSrc(), actionConntrack.getNxActionConntrack().getZoneSrc());
    }

    @Test
    public void testConvertFromOFJava() throws Exception {
        final NxActionConntrackBuilder nxActionConntrackBuilder = new NxActionConntrackBuilder()
                .setConntrackZone(1)
                .setFlags(1)
                .setRecircTable((short) 1)
                .setZoneSrc(1L);

        final ActionConntrackBuilder actionConntrackBuilder = new ActionConntrackBuilder()
                .setNxActionConntrack(nxActionConntrackBuilder.build());

        final ActionBuilder actionBuilder = new ActionBuilder()
                .setActionChoice(actionConntrackBuilder.build());

        final Action groupingAction = actionBuilder.build();

        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action
                = conntrackConvertor.convert(groupingAction, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action1
                = conntrackConvertor.convert(groupingAction, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action2
                = conntrackConvertor.convert(groupingAction, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action3
                = conntrackConvertor.convert(groupingAction, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);

        Assert.assertEquals(1, ((NxActionConntrackNodesNodeTableFlowWriteActionsCase) action).getNxConntrack().getConntrackZone().longValue());
        Assert.assertEquals(1L, ((NxActionConntrackNodesNodeTableFlowWriteActionsCase) action).getNxConntrack().getZoneSrc().longValue());
        Assert.assertEquals(1, ((NxActionConntrackNodesNodeTableFlowWriteActionsCase) action).getNxConntrack().getRecircTable().intValue());
        Assert.assertEquals(1, ((NxActionConntrackNodesNodeTableFlowWriteActionsCase) action).getNxConntrack().getFlags().intValue());

        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCase) action1).getNxConntrack().getConntrackZone().longValue());
        Assert.assertEquals(1L, ((NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCase) action1).getNxConntrack().getZoneSrc().longValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCase) action1).getNxConntrack().getRecircTable().intValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCase) action1).getNxConntrack().getFlags().intValue());

        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCase) action2).getNxConntrack().getConntrackZone().longValue());
        Assert.assertEquals(1L, ((NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCase) action2).getNxConntrack().getZoneSrc().longValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCase) action2).getNxConntrack().getRecircTable().intValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCase) action2).getNxConntrack().getFlags().intValue());

        Assert.assertEquals(1, ((NxActionConntrackNotifGroupDescStatsUpdatedCase) action3).getNxConntrack().getConntrackZone().longValue());
        Assert.assertEquals(1L, ((NxActionConntrackNotifGroupDescStatsUpdatedCase) action3).getNxConntrack().getZoneSrc().longValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifGroupDescStatsUpdatedCase) action3).getNxConntrack().getRecircTable().intValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifGroupDescStatsUpdatedCase) action3).getNxConntrack().getFlags().intValue());

    }

}