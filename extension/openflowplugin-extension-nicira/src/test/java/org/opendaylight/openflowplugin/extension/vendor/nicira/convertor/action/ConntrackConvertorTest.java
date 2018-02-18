/**
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.NxActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionConntrackNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionConntrackNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.conntrack.grouping.NxConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.conntrack.grouping.nx.conntrack.CtActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.conntrack.grouping.nx.conntrack.CtActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.NxActionNatCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.NxActionNatCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.nx.action.nat._case.NxActionNat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.nx.action.nat._case.NxActionNatBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link ConntrackConvertor}.
 */
public class ConntrackConvertorTest {

    private ConntrackConvertor conntrackConvertor;

    @Before
    public void setUp() throws Exception {
        conntrackConvertor = new ConntrackConvertor();
    }

    @Test
    public void testConvertToOfJava() throws Exception {

        final NxActionNatBuilder nxActionNatBuilder = new NxActionNatBuilder()
                .setFlags(1)
                .setRangePresent(2)
                .setIpAddressMin(new IpAddress("192.168.0.0".toCharArray()))
                .setIpAddressMin(new IpAddress("192.168.10.0".toCharArray()))
                .setPortMin(3000)
                .setPortMax(4000);
        final CtActionsBuilder ctActionsBuilder = new CtActionsBuilder().setOfpactActions(new NxActionNatCaseBuilder()
                .setNxActionNat(nxActionNatBuilder.build()).build());
        List<CtActions> ctAction = new ArrayList<>();
        ctAction.add(ctActionsBuilder.build());
        final NxConntrackBuilder nxConntrackBuilder = new NxConntrackBuilder()
                .setConntrackZone(1)
                .setFlags(1)
                .setRecircTable((short) 1)
                .setZoneSrc(1L)
                .setCtActions(ctAction);

        final NxActionConntrackNodesNodeTableFlowWriteActionsCaseBuilder nxActionConntrackBuilder =
                new NxActionConntrackNodesNodeTableFlowWriteActionsCaseBuilder()
                        .setNxConntrack(nxConntrackBuilder.build());

        final NxActionConntrackNodesNodeTableFlowWriteActionsCase actionsCase = nxActionConntrackBuilder.build();
        final ActionConntrack actionConntrack = (ActionConntrack) conntrackConvertor.convert(actionsCase)
                .getActionChoice();

        Assert.assertEquals(actionsCase.getNxConntrack().getConntrackZone(), actionConntrack.getNxActionConntrack()
                .getConntrackZone());
        Assert.assertEquals(actionsCase.getNxConntrack().getFlags(), actionConntrack.getNxActionConntrack().getFlags());
        Assert.assertEquals(actionsCase.getNxConntrack().getRecircTable(), actionConntrack.getNxActionConntrack()
                .getRecircTable());
        Assert.assertEquals(actionsCase.getNxConntrack().getZoneSrc(), actionConntrack.getNxActionConntrack()
                .getZoneSrc());

        NxActionNat natactionCase = ((NxActionNatCase) actionsCase.getNxConntrack().getCtActions().get(0)
                .getOfpactActions()).getNxActionNat();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions
            .nx.action.nat._case.NxActionNat nataction = ( (org.opendaylight
                    .yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions
                    .NxActionNatCase) actionConntrack.getNxActionConntrack().getCtActions().get(0).getOfpactActions())
            .getNxActionNat();
        Assert.assertEquals(natactionCase.getFlags(), nataction.getFlags());
        Assert.assertEquals(natactionCase.getRangePresent(), nataction.getRangePresent());
        Assert.assertEquals(natactionCase.getIpAddressMin().getIpv4Address().getValue(), nataction.getIpAddressMin()
            .getIpv4Address().getValue());
        Assert.assertEquals(natactionCase.getIpAddressMin().getIpv4Address().getValue(), nataction.getIpAddressMin()
            .getIpv4Address().getValue());
        Assert.assertEquals(natactionCase.getPortMin(), nataction.getPortMin());
        Assert.assertEquals(natactionCase.getPortMax(), nataction.getPortMax());

    }

    @Test
    public void testConvertFromOfJava() throws Exception {

        org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions
            .nx.action.nat._case.NxActionNatBuilder nxActionNatBuilder = new org.opendaylight.yang.gen.v1.urn
            .opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.nat._case
            .NxActionNatBuilder()
            .setFlags(1)
            .setRangePresent(2)
            .setIpAddressMin(new IpAddress("192.168.0.0".toCharArray()))
            .setIpAddressMax(new IpAddress("192.168.10.0".toCharArray()))
            .setPortMin(3000)
            .setPortMax(4000);
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping
            .nx.action.conntrack.CtActionsBuilder ctActionsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight
            .openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.nx.action.conntrack.CtActionsBuilder()
            .setOfpactActions(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact
                    .actions.ofpact.actions.NxActionNatCaseBuilder().setNxActionNat(nxActionNatBuilder.build())
                    .build());
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack
            .grouping.nx.action.conntrack.CtActions> ctActions = new ArrayList<>();
        ctActions.add(ctActionsBuilder.build());
        final NxActionConntrackBuilder nxActionConntrackBuilder = new NxActionConntrackBuilder()
                .setConntrackZone(1)
                .setFlags(1)
                .setRecircTable((short) 1)
                .setZoneSrc(1L)
                .setCtActions(ctActions);

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
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action4
                = conntrackConvertor.convert(groupingAction, ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action5
                = conntrackConvertor.convert(groupingAction, ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);

        Assert.assertEquals(1, ((NxActionConntrackNodesNodeTableFlowWriteActionsCase) action).getNxConntrack()
                .getConntrackZone().longValue());
        Assert.assertEquals(1L, ((NxActionConntrackNodesNodeTableFlowWriteActionsCase) action).getNxConntrack()
                .getZoneSrc().longValue());
        Assert.assertEquals(1, ((NxActionConntrackNodesNodeTableFlowWriteActionsCase) action).getNxConntrack()
                .getRecircTable().intValue());
        Assert.assertEquals(1, ((NxActionConntrackNodesNodeTableFlowWriteActionsCase) action).getNxConntrack()
                .getFlags().intValue());

        NxActionNat natActionCase = ((NxActionNatCase)((NxActionConntrackNodesNodeTableFlowWriteActionsCase)action)
               .getNxConntrack().getCtActions().get(0).getOfpactActions()).getNxActionNat();
        Assert.assertEquals(1, natActionCase.getFlags().shortValue());
        Assert.assertEquals(2, natActionCase.getRangePresent().intValue());
        Assert.assertEquals("192.168.0.0", natActionCase.getIpAddressMin().getIpv4Address().getValue());
        Assert.assertEquals("192.168.10.0", natActionCase.getIpAddressMax().getIpv4Address().getValue());
        Assert.assertEquals(3000, natActionCase.getPortMin().shortValue());
        Assert.assertEquals(4000, natActionCase.getPortMax().shortValue());

        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCase) action1)
                .getNxConntrack().getConntrackZone().longValue());
        Assert.assertEquals(1L, ((NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCase) action1)
                .getNxConntrack().getZoneSrc().longValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCase) action1)
                .getNxConntrack().getRecircTable().intValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCase) action1)
                .getNxConntrack().getFlags().intValue());

        NxActionNat natActionCase1 = ((NxActionNatCase)((NxActionConntrackNodesNodeTableFlowWriteActionsCase)action)
                .getNxConntrack().getCtActions().get(0).getOfpactActions()).getNxActionNat();
        Assert.assertEquals(1, natActionCase1.getFlags().shortValue());
        Assert.assertEquals(2, natActionCase1.getRangePresent().intValue());
        Assert.assertEquals("192.168.0.0", natActionCase1.getIpAddressMin().getIpv4Address().getValue());
        Assert.assertEquals("192.168.10.0", natActionCase1.getIpAddressMax().getIpv4Address().getValue());
        Assert.assertEquals(3000, natActionCase1.getPortMin().shortValue());
        Assert.assertEquals(4000, natActionCase1.getPortMax().shortValue());

        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCase) action2)
                .getNxConntrack().getConntrackZone().longValue());
        Assert.assertEquals(1L, ((NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCase) action2)
                .getNxConntrack().getZoneSrc().longValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCase) action2)
                .getNxConntrack().getRecircTable().intValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCase) action2)
                .getNxConntrack().getFlags().intValue());

        NxActionNat natActionCase2 = ((NxActionNatCase)((NxActionConntrackNodesNodeTableFlowWriteActionsCase)action)
                .getNxConntrack().getCtActions().get(0).getOfpactActions()).getNxActionNat();
        Assert.assertEquals(1, natActionCase2.getFlags().shortValue());
        Assert.assertEquals(2, natActionCase2.getRangePresent().intValue());
        Assert.assertEquals("192.168.0.0", natActionCase2.getIpAddressMin().getIpv4Address().getValue());
        Assert.assertEquals("192.168.10.0", natActionCase2.getIpAddressMax().getIpv4Address().getValue());
        Assert.assertEquals(3000, natActionCase2.getPortMin().shortValue());
        Assert.assertEquals(4000, natActionCase2.getPortMax().shortValue());

        NxActionNat natActionCase3 = ((NxActionNatCase)((NxActionConntrackNodesNodeTableFlowWriteActionsCase)action)
                .getNxConntrack().getCtActions().get(0).getOfpactActions()).getNxActionNat();
        Assert.assertEquals(1, natActionCase3.getFlags().shortValue());
        Assert.assertEquals(2, natActionCase3.getRangePresent().intValue());
        Assert.assertEquals("192.168.0.0", natActionCase3.getIpAddressMin().getIpv4Address().getValue());
        Assert.assertEquals("192.168.10.0", natActionCase3.getIpAddressMax().getIpv4Address().getValue());
        Assert.assertEquals(3000, natActionCase3.getPortMin().shortValue());
        Assert.assertEquals(4000, natActionCase3.getPortMax().shortValue());

        Assert.assertEquals(1, ((NxActionConntrackNotifGroupDescStatsUpdatedCase) action3).getNxConntrack()
                .getConntrackZone().longValue());
        Assert.assertEquals(1L, ((NxActionConntrackNotifGroupDescStatsUpdatedCase) action3).getNxConntrack()
                .getZoneSrc().longValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifGroupDescStatsUpdatedCase) action3).getNxConntrack()
                .getRecircTable().intValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifGroupDescStatsUpdatedCase) action3).getNxConntrack()
                .getFlags().intValue());

        Assert.assertEquals(1, ((NxActionConntrackNotifDirectStatisticsUpdateWriteActionsCase) action4)
                .getNxConntrack().getConntrackZone().longValue());
        Assert.assertEquals(1L, ((NxActionConntrackNotifDirectStatisticsUpdateWriteActionsCase) action4)
                .getNxConntrack().getZoneSrc().longValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifDirectStatisticsUpdateWriteActionsCase) action4)
                .getNxConntrack().getRecircTable().intValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifDirectStatisticsUpdateWriteActionsCase) action4)
                .getNxConntrack().getFlags().intValue());

        Assert.assertEquals(1, ((NxActionConntrackNotifDirectStatisticsUpdateApplyActionsCase) action5).getNxConntrack()
                .getConntrackZone().longValue());
        Assert.assertEquals(1L, ((NxActionConntrackNotifDirectStatisticsUpdateApplyActionsCase) action5)
                .getNxConntrack().getZoneSrc().longValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifDirectStatisticsUpdateApplyActionsCase) action5).getNxConntrack()
                .getRecircTable().intValue());
        Assert.assertEquals(1, ((NxActionConntrackNotifDirectStatisticsUpdateApplyActionsCase) action5).getNxConntrack()
                .getFlags().intValue());
    }
}