/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load.grouping.NxActionRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionRegLoadNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.DstBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Convert to/from SAL flow model to openflowjava model for NxActionRegLoad action.
 *
 * @author msunal
 */
public class RegLoadConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
            Action>, ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public Action convert(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxActionArg) {
        checkArgument(nxActionArg instanceof NxActionRegLoadGrouping);
        final var nxAction = (NxActionRegLoadGrouping) nxActionArg;
        final var dst = nxAction.getNxRegLoad().getDst();
        final int start = dst.getStart().toJava();

        return ActionUtil.createAction(new ActionRegLoadBuilder()
            .setNxActionRegLoad(new NxActionRegLoadBuilder()
                // We resolve the destination as a uint32 header, reg load action
                // does not support 8-byte experimenter headers.
                .setDst(FieldChoiceResolver.resolveDstHeaderUint32(dst.getDstChoice()))
                .setOfsNbits(Uint16.valueOf(start << 6 | dst.getEnd().toJava() - start))
                .setValue(nxAction.getNxRegLoad().getValue())
                .build())
            .build());
    }

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(
            final Action input, final ActionPath path) {
        final var actionRegLoad = ((ActionRegLoad) input.getActionChoice()).getNxActionRegLoad();
        return resolveAction(new NxRegLoadBuilder()
            .setDst(new DstBuilder()
                .setDstChoice(FieldChoiceResolver.resolveDstChoice(actionRegLoad.getDst()))
                .setStart(resolveStart(actionRegLoad.getOfsNbits()))
                .setEnd(resolveEnd(actionRegLoad.getOfsNbits()))
                .build())
            .setValue(actionRegLoad.getValue())
            .build(), path);
    }

    private static Uint16 resolveStart(final Uint16 ofsNBints) {
        return Uint16.valueOf(extractSub(ofsNBints.toJava(), 10, 6));
    }

    private static Uint16 resolveEnd(final Uint16 ofsNBints) {
        final int bits = ofsNBints.toJava();
        int ofs = extractSub(bits, 10, 6);
        int numBits = extractSub(bits, 6, 0);
        return Uint16.valueOf(ofs + numBits);
    }

    private static int extractSub(final int value, final int nrBits, final int offset) {
        final int rightShifted = value >>> offset;
        final int mask = (1 << nrBits) - 1;
        return rightShifted & mask;
    }

    static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            final NxRegLoad value, final ActionPath path) {
        switch (path) {
            case INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS:
                return new NxActionRegLoadNodesNodeTableFlowWriteActionsCaseBuilder().setNxRegLoad(value).build();
            case FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS:
                return new NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxRegLoad(value).build();
            case FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS:
                return new NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxRegLoad(value).build();
            case GROUP_DESC_STATS_UPDATED_BUCKET_ACTION:
                return new NxActionRegLoadNotifGroupDescStatsUpdatedCaseBuilder().setNxRegLoad(value).build();
            case FLOWS_STATISTICS_RPC_WRITE_ACTIONS:
                return new NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxRegLoad(value).build();
            case FLOWS_STATISTICS_RPC_APPLY_ACTIONS:
                return new NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxRegLoad(value).build();
            case INVENTORY_FLOWNODE_TABLE_APPLY_ACTIONS:
                return new NxActionRegLoadNodesNodeTableFlowApplyActionsCaseBuilder().setNxRegLoad(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }
}
