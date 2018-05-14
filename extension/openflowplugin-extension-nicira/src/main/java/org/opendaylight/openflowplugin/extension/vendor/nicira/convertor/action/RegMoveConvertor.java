/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMoveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.move.grouping.NxActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.move.grouping.NxActionRegMoveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegMoveGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNotifDirectStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNotifDirectStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionRegMoveNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.NxRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.NxRegMoveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.SrcBuilder;

/**
 * Convert to/from SAL flow model to openflowjava model for NxActionRegMove action.
 *
 * @author msunal
 * @author Josh Hershberg (jhershbe@redhat.com)
 */
public class RegMoveConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
            Action>, ConvertorActionFromOFJava<Action, ActionPath> {



    @Override
    public Action convert(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxActionArg) {
        Preconditions.checkArgument(nxActionArg instanceof NxActionRegMoveGrouping);
        NxActionRegMoveGrouping nxAction = (NxActionRegMoveGrouping) nxActionArg;

        Dst dst = nxAction.getNxRegMove().getDst();
        Src src = nxAction.getNxRegMove().getSrc();
        final ActionRegMoveBuilder actionRegMoveBuilder = new ActionRegMoveBuilder();
        NxActionRegMoveBuilder nxActionRegMove = new NxActionRegMoveBuilder();

        nxActionRegMove.setDst(FieldChoiceResolver.resolveDstHeaderUint64(dst.getDstChoice()));
        nxActionRegMove.setDstOfs(dst.getStart());
        nxActionRegMove.setSrc(FieldChoiceResolver.resolveSrcHeaderUint64(src.getSrcChoice()));
        nxActionRegMove.setSrcOfs(src.getStart());
        nxActionRegMove.setNBits(dst.getEnd() - dst.getStart() + 1);
        actionRegMoveBuilder.setNxActionRegMove(nxActionRegMove.build());
        return ActionUtil.createAction(actionRegMoveBuilder.build());
    }

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(
            Action input, ActionPath path) {
        NxActionRegMove actionRegMove = ((ActionRegMove) input.getActionChoice()).getNxActionRegMove();
        DstBuilder dstBuilder = new DstBuilder();
        dstBuilder.setDstChoice(FieldChoiceResolver.resolveDstChoice(actionRegMove.getDst()));
        dstBuilder.setStart(actionRegMove.getDstOfs());
        dstBuilder.setEnd(actionRegMove.getDstOfs() + actionRegMove.getNBits() - 1);
        SrcBuilder srcBuilder = new SrcBuilder();
        srcBuilder.setSrcChoice(FieldChoiceResolver.resolveSrcChoice(actionRegMove.getSrc()));
        srcBuilder.setStart(actionRegMove.getSrcOfs());
        srcBuilder.setEnd(actionRegMove.getSrcOfs() + actionRegMove.getNBits() - 1);
        NxRegMoveBuilder nxRegMoveBuilder = new NxRegMoveBuilder();
        nxRegMoveBuilder.setDst(dstBuilder.build());
        nxRegMoveBuilder.setSrc(srcBuilder.build());
        return resolveAction(nxRegMoveBuilder.build(), path);
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            NxRegMove value, ActionPath path) {
        switch (path) {
            case INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS:
                return new NxActionRegMoveNodesNodeTableFlowWriteActionsCaseBuilder().setNxRegMove(value).build();
            case FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS:
                return new NxActionRegMoveNotifFlowsStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxRegMove(value).build();
            case FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS:
                return new NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxRegMove(value).build();
            case GROUP_DESC_STATS_UPDATED_BUCKET_ACTION:
                return new NxActionRegMoveNotifGroupDescStatsUpdatedCaseBuilder().setNxRegMove(value).build();
            case FLOWS_STATISTICS_RPC_WRITE_ACTIONS:
                return new NxActionRegMoveNotifDirectStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxRegMove(value).build();
            case FLOWS_STATISTICS_RPC_APPLY_ACTIONS:
                return new NxActionRegMoveNotifDirectStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxRegMove(value).build();
            case INVENTORY_FLOWNODE_TABLE_APPLY_ACTIONS:
                return new NxActionRegMoveNodesNodeTableFlowApplyActionsCaseBuilder().setNxRegMove(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }

}
