/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.decap.grouping.NxActionDecap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.decap.grouping.NxActionDecapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionDecapGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionDecapNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecapNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionDecapNotifDirectStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecapNotifDirectStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionDecapNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionDecapNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecapNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.decap.grouping.NxDecap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.decap.grouping.NxDecapBuilder;

public class DecapConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
                        Action>, ConvertorActionFromOFJava<Action, ActionPath> {
    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(
            Action input, ActionPath path) {
        ActionDecap actionDecap = (ActionDecap) input.getActionChoice();
        Long packetType = actionDecap.getNxActionDecap().getPacketType();
        NxDecap nxDecap = new NxDecapBuilder().setPacketType(packetType).build();
        return resolveAction(nxDecap, path);
    }

    @Override
    public Action convert(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase) {
        Preconditions.checkArgument(actionCase instanceof NxActionDecapGrouping);
        NxActionDecapGrouping nxActionDecapGrouping = (NxActionDecapGrouping) actionCase;
        Long packetType = nxActionDecapGrouping.getNxDecap().getPacketType();
        NxActionDecap nxActionDecap = new NxActionDecapBuilder().setPacketType(packetType).build();
        ActionDecap actionDecap = new ActionDecapBuilder().setNxActionDecap(nxActionDecap).build();
        return ActionUtil.createAction(actionDecap);
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            NxDecap value, ActionPath path) {
        switch (path) {
            case INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS:
                return new NxActionDecapNodesNodeTableFlowWriteActionsCaseBuilder().setNxDecap(value).build();
            case FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS:
                return new NxActionDecapNotifFlowsStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxDecap(value).build();
            case FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS:
                return new NxActionDecapNotifFlowsStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxDecap(value).build();
            case GROUP_DESC_STATS_UPDATED_BUCKET_ACTION:
                return new NxActionDecapNotifGroupDescStatsUpdatedCaseBuilder().setNxDecap(value).build();
            case FLOWS_STATISTICS_RPC_WRITE_ACTIONS:
                return new NxActionDecapNotifDirectStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxDecap(value).build();
            case FLOWS_STATISTICS_RPC_APPLY_ACTIONS:
                return new NxActionDecapNotifDirectStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxDecap(value).build();
            case INVENTORY_FLOWNODE_TABLE_APPLY_ACTIONS:
                return new NxActionDecapNodesNodeTableFlowApplyActionsCaseBuilder().setNxDecap(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }
}
