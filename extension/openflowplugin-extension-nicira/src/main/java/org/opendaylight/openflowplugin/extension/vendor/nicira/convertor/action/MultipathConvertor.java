/*
 * Copyright (c) 2014, 2015 SDN Hub, LLC. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionMultipathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.multipath.grouping.NxActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.multipath.grouping.NxActionMultipathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionMultipathGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionMultipathNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionMultipathNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionMultipathNotifDirectStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionMultipathNotifDirectStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionMultipathNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionMultipathNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionMultipathNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.multipath.grouping.NxMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.multipath.grouping.NxMultipathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.multipath.grouping.nx.multipath.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.multipath.grouping.nx.multipath.DstBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

public class MultipathConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
            Action>, ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public Action convert(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxActionArg) {
        Preconditions.checkArgument(nxActionArg instanceof NxActionMultipathGrouping);
        NxActionMultipathGrouping nxAction = (NxActionMultipathGrouping) nxActionArg;

        NxActionMultipathBuilder nxActionMultipathBuilder = new NxActionMultipathBuilder();
        nxActionMultipathBuilder.setFields(nxAction.getNxMultipath().getFields());
        nxActionMultipathBuilder.setBasis(nxAction.getNxMultipath().getBasis());
        nxActionMultipathBuilder.setAlgorithm(nxAction.getNxMultipath().getAlgorithm());
        nxActionMultipathBuilder.setMaxLink(nxAction.getNxMultipath().getMaxLink());
        nxActionMultipathBuilder.setArg(nxAction.getNxMultipath().getArg());
        Dst dst = nxAction.getNxMultipath().getDst();

        final int start = dst.getStart().toJava();
        nxActionMultipathBuilder.setOfsNbits(Uint16.valueOf(start << 6 | dst.getEnd().toJava() - start));
        // We resolve the destination as a uint32 header, multipath action
        // does not support 8-byte experimenter headers.
        nxActionMultipathBuilder.setDst(FieldChoiceResolver.resolveDstHeaderUint32(dst.getDstChoice()));
        ActionMultipathBuilder actionMultipathBuilder = new ActionMultipathBuilder();
        actionMultipathBuilder.setNxActionMultipath(nxActionMultipathBuilder.build());
        return ActionUtil.createAction(actionMultipathBuilder.build());
    }

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(
            final Action input, final ActionPath path) {
        NxActionMultipath action = ((ActionMultipath) input.getActionChoice()).getNxActionMultipath();
        DstBuilder dstBuilder = new DstBuilder();
        dstBuilder.setDstChoice(FieldChoiceResolver.resolveDstChoice(action.getDst()));
        dstBuilder.setStart(resolveStart(action.getOfsNbits()));
        dstBuilder.setEnd(resolveEnd(action.getOfsNbits()));
        NxMultipathBuilder builder = new NxMultipathBuilder();
        builder.setBasis(action.getBasis());
        builder.setAlgorithm(action.getAlgorithm());
        builder.setMaxLink(action.getMaxLink());
        builder.setArg(action.getArg());
        builder.setDst(dstBuilder.build());
        return resolveAction(builder.build(), path);
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

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            final NxMultipath value, final ActionPath path) {
        switch (path) {
            case INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS:
                return new NxActionMultipathNodesNodeTableFlowWriteActionsCaseBuilder().setNxMultipath(value).build();
            case FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS:
                return new NxActionMultipathNotifFlowsStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxMultipath(value).build();
            case FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS:
                return new NxActionMultipathNotifFlowsStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxMultipath(value).build();
            case GROUP_DESC_STATS_UPDATED_BUCKET_ACTION:
                return new NxActionMultipathNotifGroupDescStatsUpdatedCaseBuilder().setNxMultipath(value).build();
            case FLOWS_STATISTICS_RPC_WRITE_ACTIONS:
                return new NxActionMultipathNotifDirectStatisticsUpdateWriteActionsCaseBuilder()
                        .setNxMultipath(value).build();
            case FLOWS_STATISTICS_RPC_APPLY_ACTIONS:
                return new NxActionMultipathNotifDirectStatisticsUpdateApplyActionsCaseBuilder()
                        .setNxMultipath(value).build();
            case INVENTORY_FLOWNODE_TABLE_APPLY_ACTIONS:
                return new NxActionMultipathNodesNodeTableFlowApplyActionsCaseBuilder().setNxMultipath(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }
}
