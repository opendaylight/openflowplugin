/*
 * Copyright (c) 2014, 2015 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionResubmitBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.resubmit.grouping.NxActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.resubmit.grouping.NxActionResubmitBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionResubmitGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionResubmitNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitNotifDirectStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionResubmitNotifDirectStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionResubmitNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionResubmitNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.resubmit.grouping.NxResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.resubmit.grouping.NxResubmitBuilder;

/**
 * Convert to/from SAL flow model to openflowjava model for Resubmit action.
 */
public class ResubmitConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
            Action>, ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public Action convert(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxActionArg) {
        Preconditions.checkArgument(nxActionArg instanceof NxActionResubmitGrouping);
        NxActionResubmitGrouping nxAction = (NxActionResubmitGrouping) nxActionArg;
        ActionResubmitBuilder builder = new ActionResubmitBuilder();
        NxActionResubmitBuilder nxActionResubmitBuilder = new NxActionResubmitBuilder();
        nxActionResubmitBuilder.setInPort(nxAction.getNxResubmit().getInPort());
        nxActionResubmitBuilder.setTable(nxAction.getNxResubmit().getTable());
        builder.setNxActionResubmit(nxActionResubmitBuilder.build());
        return ActionUtil.createAction(builder.build());
    }

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(
            final Action input, final ActionPath path) {
        NxActionResubmit action = ((ActionResubmit) input.getActionChoice()).getNxActionResubmit();
        NxResubmitBuilder builder = new NxResubmitBuilder();
        builder.setInPort(action.getInPort());
        builder.setTable(action.getTable());
        return resolveAction(builder.build(), path);
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            final NxResubmit value, final ActionPath path) {
        switch (path) {
            case INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS:
                return new NxActionResubmitNodesNodeTableFlowWriteActionsCaseBuilder().setNxResubmit(value).build();
            case FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS:
                return new NxActionResubmitNotifFlowsStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxResubmit(value).build();
            case FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS:
                return new NxActionResubmitNotifFlowsStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxResubmit(value).build();
            case GROUP_DESC_STATS_UPDATED_BUCKET_ACTION:
                return new NxActionResubmitNotifGroupDescStatsUpdatedCaseBuilder().setNxResubmit(value).build();
            case FLOWS_STATISTICS_RPC_WRITE_ACTIONS:
                return new NxActionResubmitNotifDirectStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxResubmit(value).build();
            case FLOWS_STATISTICS_RPC_APPLY_ACTIONS:
                return new NxActionResubmitNotifDirectStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxResubmit(value).build();
            case INVENTORY_FLOWNODE_TABLE_APPLY_ACTIONS:
                return new NxActionResubmitNodesNodeTableFlowApplyActionsCaseBuilder().setNxResubmit(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }
}
