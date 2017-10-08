/*
 * Copyright (c) 2015, 2017 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.NxActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.NxActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionConntrackGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionConntrackNotifDirectStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNotifDirectStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionConntrackNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.conntrack.grouping.NxConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.conntrack.grouping.NxConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.conntrack.grouping.nx.conntrack.CtActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.conntrack.grouping.nx.conntrack.CtActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.OfpactActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.NxActionCtMarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.NxActionNatCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.NxActionCtMarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.NxActionNatCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.nx.action.ct.mark._case.NxActionCtMark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.nx.action.nat._case.NxActionNat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.nx.action.ct.mark._case.NxActionCtMarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.ofpact.actions.ofpact.actions.nx.action.nat._case.NxActionNatBuilder;

/**
 * @author Aswin Suryanarayanan.
 */

public class ConntrackConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
        .Action, Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
                          .Action convert(final Action input, final ActionPath path) {
        NxActionConntrack action = ((ActionConntrack) input.getActionChoice()).getNxActionConntrack();
        NxConntrackBuilder builder = new NxConntrackBuilder();
        builder.setFlags(action.getFlags());
        builder.setZoneSrc(action.getZoneSrc());
        builder.setRecircTable(action.getRecircTable());
        builder.setConntrackZone(action.getConntrackZone());
        builder.setCtActions(getCtAction(action));
        return resolveAction(builder.build(), path);
    }

    private List<CtActions> getCtAction(final NxActionConntrack action) {
        if (action.getCtActions() == null) {
            return null;
        }
        List<CtActions> ctActions = new ArrayList<>();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack
                .grouping.nx.action.conntrack.CtActions ctAction : action.getCtActions()) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions
                .OfpactActions ofpactAction = ctAction.getOfpactActions();
            if (ofpactAction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421
                    .ofpact.actions.ofpact.actions.NxActionNatCase) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact
                    .actions.nx.action.nat._case.NxActionNat nxActionNat = ((org.opendaylight.yang.gen.v1.urn
                            .opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions
                            .NxActionNatCase) ofpactAction).getNxActionNat();
                NxActionNatBuilder nxActionNatBuilder = new NxActionNatBuilder();
                nxActionNatBuilder.setFlags(nxActionNat.getFlags());
                nxActionNatBuilder.setRangePresent(nxActionNat.getRangePresent());
                nxActionNatBuilder.setIpAddressMin(nxActionNat.getIpAddressMin());
                nxActionNatBuilder.setIpAddressMax(nxActionNat.getIpAddressMax());
                nxActionNatBuilder.setPortMin(nxActionNat.getPortMin());
                nxActionNatBuilder.setPortMax(nxActionNat.getPortMax());
                NxActionNatCaseBuilder caseBuilder = new NxActionNatCaseBuilder();
                caseBuilder.setNxActionNat(nxActionNatBuilder.build());
                CtActionsBuilder ctActionsBuilder = new CtActionsBuilder();
                ctActionsBuilder.setOfpactActions(caseBuilder.build());
                ctActions.add(ctActionsBuilder.build());
            } else if (ofpactAction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421
                    .ofpact.actions.ofpact.actions.NxActionCtMarkCase) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact
                    .actions.nx.action.ct.mark._case.NxActionCtMark nxActionCtMark = ((org.opendaylight.yang.gen.v1.urn
                            .opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions
                            .NxActionCtMarkCase) ofpactAction).getNxActionCtMark();
                NxActionCtMarkBuilder nxActionCtMarkBuilder = new NxActionCtMarkBuilder();
                nxActionCtMarkBuilder.setCtMark(nxActionCtMark.getCtMark());
                // TODO: ct_mark mask is not supported yet
                NxActionCtMarkCaseBuilder caseBuilder = new NxActionCtMarkCaseBuilder();
                caseBuilder.setNxActionCtMark(nxActionCtMarkBuilder.build());
                CtActionsBuilder ctActionsBuilder = new CtActionsBuilder();
                ctActionsBuilder.setOfpactActions(caseBuilder.build());
                ctActions.add(ctActionsBuilder.build());
            }
        }
        return ctActions;
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
                        .Action resolveAction(final NxConntrack value, final ActionPath path) {
        switch (path) {
            case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
                return new NxActionConntrackNodesNodeTableFlowWriteActionsCaseBuilder().setNxConntrack(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
                return new NxActionConntrackNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setNxConntrack(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
                return new NxActionConntrackNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setNxConntrack(value).build();
            case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
                return new NxActionConntrackNotifGroupDescStatsUpdatedCaseBuilder().setNxConntrack(value).build();
            case RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
                return new NxActionConntrackNotifDirectStatisticsUpdateWriteActionsCaseBuilder().setNxConntrack(value).build();
            case RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
                return new NxActionConntrackNotifDirectStatisticsUpdateApplyActionsCaseBuilder().setNxConntrack(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }

    @Override
    public Action convert(final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
            .Action nxActionArg) {
        Preconditions.checkArgument(nxActionArg instanceof NxActionConntrackGrouping);
        NxActionConntrackGrouping nxAction = (NxActionConntrackGrouping) nxActionArg;

        NxActionConntrackBuilder nxActionConntrackBuilder = new NxActionConntrackBuilder();
        nxActionConntrackBuilder.setFlags(nxAction.getNxConntrack().getFlags());
        nxActionConntrackBuilder.setZoneSrc(nxAction.getNxConntrack().getZoneSrc());
        nxActionConntrackBuilder.setRecircTable(nxAction.getNxConntrack().getRecircTable());
        nxActionConntrackBuilder.setConntrackZone(nxAction.getNxConntrack().getConntrackZone());
        nxActionConntrackBuilder.setCtActions(getCtAction(nxAction.getNxConntrack()));
        ActionConntrackBuilder actionConntrackBuilder = new ActionConntrackBuilder();
        actionConntrackBuilder.setNxActionConntrack(nxActionConntrackBuilder.build());
        return ActionUtil.createAction(actionConntrackBuilder.build());
    }

    private List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack
                                .grouping.nx.action.conntrack.CtActions> getCtAction(final NxConntrack action) {
        if (action.getCtActions() == null) {
            return null;
        }
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack
            .grouping.nx.action.conntrack.CtActions> ctActions = new ArrayList<>();
        for (CtActions ctAction : action.getCtActions()) {
            OfpactActions ofpactAction = ctAction.getOfpactActions();
            if (ofpactAction instanceof NxActionNatCase) {
                NxActionNat nxActionNat = ((NxActionNatCase) ofpactAction).getNxActionNat();
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact
                    .actions.nx.action.nat._case.NxActionNatBuilder nxActionNatBuilder = new org.opendaylight.yang.gen
                    .v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.nat
                    ._case.NxActionNatBuilder();
                nxActionNatBuilder.setFlags(nxActionNat.getFlags());
                nxActionNatBuilder.setRangePresent(nxActionNat.getRangePresent());
                nxActionNatBuilder.setIpAddressMin(nxActionNat.getIpAddressMin());
                nxActionNatBuilder.setIpAddressMax(nxActionNat.getIpAddressMax());
                nxActionNatBuilder.setPortMin(nxActionNat.getPortMin());
                nxActionNatBuilder.setPortMax(nxActionNat.getPortMax());
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact
                    .actions.NxActionNatCaseBuilder caseBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight
                    .openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionNatCaseBuilder();
                caseBuilder.setNxActionNat(nxActionNatBuilder.build());
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack
                    .grouping.nx.action.conntrack.CtActionsBuilder ctActionsBuilder = new org.opendaylight.yang.gen.v1
                    .urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.nx.action
                    .conntrack.CtActionsBuilder();
                ctActionsBuilder.setOfpactActions(caseBuilder.build());
                ctActions.add(ctActionsBuilder.build());
            } else if (ofpactAction instanceof NxActionCtMarkCase) {
                NxActionCtMark nxActionCtMark = ((NxActionCtMarkCase) ofpactAction).getNxActionCtMark();
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact
                    .actions.nx.action.ct.mark._case.NxActionCtMarkBuilder nxActionCtMarkBuilder = new org.opendaylight.yang.gen
                    .v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.ct.mark
                    ._case.NxActionCtMarkBuilder();
                nxActionCtMarkBuilder.setCtMark(nxActionCtMark.getCtMark());
                // TODO: ct_mark mask is not supported yet
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact
                    .actions.NxActionCtMarkCaseBuilder caseBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight
                    .openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionCtMarkCaseBuilder();
                caseBuilder.setNxActionCtMark(nxActionCtMarkBuilder.build());
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack
                    .grouping.nx.action.conntrack.CtActionsBuilder ctActionsBuilder = new org.opendaylight.yang.gen.v1
                    .urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.nx.action
                    .conntrack.CtActionsBuilder();
                ctActionsBuilder.setOfpactActions(caseBuilder.build());
                ctActions.add(ctActionsBuilder.build());
            }
        }
        return ctActions;
    }
}
