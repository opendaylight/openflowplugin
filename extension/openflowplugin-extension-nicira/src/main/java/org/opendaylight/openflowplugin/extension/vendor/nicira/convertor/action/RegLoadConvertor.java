/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load.grouping.NxActionRegLoad;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.DstBuilder;

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
        Preconditions.checkArgument(nxActionArg instanceof NxActionRegLoadGrouping);

        NxActionRegLoadGrouping nxAction = (NxActionRegLoadGrouping) nxActionArg;
        Dst dst = nxAction.getNxRegLoad().getDst();


        final ActionRegLoadBuilder actionRegLoadBuilder = new ActionRegLoadBuilder();
        NxActionRegLoadBuilder nxActionRegLoadBuilder = new NxActionRegLoadBuilder();
        // We resolve the destination as a uint32 header, reg load action
        // does not support 8-byte experimenter headers.
        nxActionRegLoadBuilder.setDst(FieldChoiceResolver.resolveDstHeaderUint32(dst.getDstChoice()));
        nxActionRegLoadBuilder.setOfsNbits(dst.getStart() << 6 | dst.getEnd() - dst.getStart());
        nxActionRegLoadBuilder.setValue(nxAction.getNxRegLoad().getValue());
        actionRegLoadBuilder.setNxActionRegLoad(nxActionRegLoadBuilder.build());
        return ActionUtil.createAction(actionRegLoadBuilder.build());
    }

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(
            final Action input, final ActionPath path) {
        NxActionRegLoad actionRegLoad = ((ActionRegLoad) input.getActionChoice()).getNxActionRegLoad();
        DstBuilder dstBuilder = new DstBuilder();
        dstBuilder.setDstChoice(FieldChoiceResolver.resolveDstChoice(actionRegLoad.getDst()));
        dstBuilder.setStart(resolveStart(actionRegLoad.getOfsNbits()));
        dstBuilder.setEnd(resolveEnd(actionRegLoad.getOfsNbits()));
        NxRegLoadBuilder nxRegLoadBuilder = new NxRegLoadBuilder();
        nxRegLoadBuilder.setDst(dstBuilder.build());
        nxRegLoadBuilder.setValue(actionRegLoad.getValue());
        return resolveAction(nxRegLoadBuilder.build(), path);
    }

    private static int resolveStart(final int ofsNBints) {
        return extractSub(ofsNBints, 10, 6);
    }

    private static int resolveEnd(final int ofsNBints) {
        int ofs = extractSub(ofsNBints, 10, 6);
        int numBits = extractSub(ofsNBints, 6, 0);
        return ofs + numBits;
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
