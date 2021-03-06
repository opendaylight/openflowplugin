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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionFinTimeout;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionFinTimeoutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.fin.timeout.grouping.NxActionFinTimeout;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.fin.timeout.grouping.NxActionFinTimeoutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionFinTimeoutGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionFinTimeoutNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionFinTimeoutNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionFinTimeoutNotifDirectStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionFinTimeoutNotifDirectStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionFinTimeoutNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionFinTimeoutNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionFinTimeoutNodesNodeTableFlowWriteActionsCaseBuilder;

/**
 * Convert to/from SAL flow model to openflowjava model for FinTimeout action.
 */
public class FinTimeoutConvertor
        implements ConvertorActionToOFJava<
                org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(
            final Action input, final ActionPath path) {
        NxActionFinTimeout action = ((ActionFinTimeout) input.getActionChoice()).getNxActionFinTimeout();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.fin
            .timeout.grouping.NxActionFinTimeoutBuilder builder = new org.opendaylight.yang.gen.v1.urn.opendaylight
                .openflowplugin.extension.nicira.action.rev140714.nx.action.fin.timeout.grouping
                    .NxActionFinTimeoutBuilder();
        builder.setFinIdleTimeout(action.getFinIdleTimeout());
        builder.setFinHardTimeout(action.getFinHardTimeout());
        return resolveAction(builder.build(), path);
    }

    @Override
    public Action convert(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxActionArg) {
        Preconditions.checkArgument(nxActionArg instanceof NxActionFinTimeoutGrouping);
        NxActionFinTimeoutGrouping nxAction = (NxActionFinTimeoutGrouping) nxActionArg;
        ActionFinTimeoutBuilder builder = new ActionFinTimeoutBuilder();
        NxActionFinTimeoutBuilder nxActionFinTimeoutBuilder = new NxActionFinTimeoutBuilder();
        nxActionFinTimeoutBuilder.setFinIdleTimeout(nxAction.getNxActionFinTimeout().getFinIdleTimeout());
        nxActionFinTimeoutBuilder.setFinHardTimeout(nxAction.getNxActionFinTimeout().getFinHardTimeout());
        builder.setNxActionFinTimeout(nxActionFinTimeoutBuilder.build());
        return ActionUtil.createAction(builder.build());
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx
                .action.fin.timeout.grouping.NxActionFinTimeout nxActionFinTimeout,
            final ActionPath path) {
        switch (path) {
            case INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS:
                return new NxActionFinTimeoutNodesNodeTableFlowWriteActionsCaseBuilder()
                        .setNxActionFinTimeout(nxActionFinTimeout).build();
            case FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS:
                return new NxActionFinTimeoutNotifFlowsStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxActionFinTimeout(nxActionFinTimeout).build();
            case FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS:
                return new NxActionFinTimeoutNotifFlowsStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxActionFinTimeout(nxActionFinTimeout).build();
            case GROUP_DESC_STATS_UPDATED_BUCKET_ACTION:
                return new NxActionFinTimeoutNotifGroupDescStatsUpdatedCaseBuilder()
                        .setNxActionFinTimeout(nxActionFinTimeout).build();
            case FLOWS_STATISTICS_RPC_WRITE_ACTIONS:
                return new NxActionFinTimeoutNotifDirectStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxActionFinTimeout(nxActionFinTimeout).build();
            case FLOWS_STATISTICS_RPC_APPLY_ACTIONS:
                return new NxActionFinTimeoutNotifDirectStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxActionFinTimeout(nxActionFinTimeout).build();
            case INVENTORY_FLOWNODE_TABLE_APPLY_ACTIONS:
                return new NxActionFinTimeoutNodesNodeTableFlowApplyActionsCaseBuilder()
                        .setNxActionFinTimeout(nxActionFinTimeout).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }
}
