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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionEncap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionEncapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.encap.grouping.NxActionEncap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.encap.grouping.NxActionEncapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionEncapGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionEncapNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionEncapNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionEncapNotifDirectStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionEncapNotifDirectStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionEncapNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionEncapNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionEncapNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.encap.grouping.NxEncap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.encap.grouping.NxEncapBuilder;

public class EncapConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
                        Action>, ConvertorActionFromOFJava<Action, ActionPath> {
    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(
            Action input, ActionPath path) {
        ActionEncap actionEncap = (ActionEncap) input.getActionChoice();
        Long packetType = actionEncap.getNxActionEncap().getPacketType();
        NxEncap nxEncap = new NxEncapBuilder().setPacketType(packetType).build();
        return resolveAction(nxEncap, path);
    }

    @Override
    public Action convert(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase) {
        Preconditions.checkArgument(actionCase instanceof NxActionEncapGrouping);
        NxActionEncapGrouping nxActionEncapGrouping = (NxActionEncapGrouping) actionCase;
        Long packetType = nxActionEncapGrouping.getNxEncap().getPacketType();
        NxActionEncap nxActionEncap = new NxActionEncapBuilder().setPacketType(packetType).build();
        ActionEncap actionEncap = new ActionEncapBuilder().setNxActionEncap(nxActionEncap).build();
        return ActionUtil.createAction(actionEncap);
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            NxEncap value, ActionPath path) {
        switch (path) {
            case INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS:
                return new NxActionEncapNodesNodeTableFlowWriteActionsCaseBuilder().setNxEncap(value).build();
            case FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS:
                return new NxActionEncapNotifFlowsStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxEncap(value).build();
            case FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS:
                return new NxActionEncapNotifFlowsStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxEncap(value).build();
            case GROUP_DESC_STATS_UPDATED_BUCKET_ACTION:
                return new NxActionEncapNotifGroupDescStatsUpdatedCaseBuilder().setNxEncap(value).build();
            case FLOWS_STATISTICS_RPC_WRITE_ACTIONS:
                return new NxActionEncapNotifDirectStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxEncap(value).build();
            case FLOWS_STATISTICS_RPC_APPLY_ACTIONS:
                return new NxActionEncapNotifDirectStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxEncap(value).build();
            case INVENTORY_FLOWNODE_TABLE_APPLY_ACTIONS:
                return new NxActionEncapNodesNodeTableFlowApplyActionsCaseBuilder().setNxEncap(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }
}
