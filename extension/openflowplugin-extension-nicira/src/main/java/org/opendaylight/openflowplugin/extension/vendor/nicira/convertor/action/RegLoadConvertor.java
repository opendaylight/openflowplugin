/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.NxmNxRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load.grouping.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load.grouping.ActionRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionRegLoadNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.DstBuilder;

import com.google.common.base.Preconditions;

/**
 * @author msunal
 *
 */
public class RegLoadConvertor implements 
ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(Action input, ActionPath path) {
        ActionRegLoad actionRegLoad = input.getAugmentation(OfjAugNxAction.class).getActionRegLoad();
        DstBuilder dstBuilder = new DstBuilder();
        dstBuilder.setDstChoice(RegMoveConvertor.resolveDst(actionRegLoad.getDst()));
        dstBuilder.setStart(resolveStart(actionRegLoad.getOfsNbits()));
        dstBuilder.setEnd(resolveEnd(actionRegLoad.getOfsNbits()));
        NxRegLoadBuilder nxRegLoadBuilder = new NxRegLoadBuilder();
        nxRegLoadBuilder.setDst(dstBuilder.build());
        nxRegLoadBuilder.setValue(actionRegLoad.getValue());
        return resolveAction(nxRegLoadBuilder.build(), path);
    }

    private static int resolveStart(int ofsNBints) {
        return extractSub(ofsNBints, 10, 6);
    }

    private static int resolveEnd(int ofsNBints) {
        int ofs = extractSub(ofsNBints, 10, 6);
        int nBits = extractSub(ofsNBints, 6, 0);
        return ofs + nBits;
    }

    private static int extractSub(final int l, final int nrBits, final int offset) {
        final int rightShifted = l >>> offset;
        final int mask = (1 << nrBits) - 1;
        return rightShifted & mask;
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(NxRegLoad value,
            ActionPath path) {
        switch (path) {
        case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
            return new NxActionRegLoadNodesNodeTableFlowWriteActionsCaseBuilder().setNxRegLoad(value).build();
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
            return new NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setNxRegLoad(value).build();
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
            return new NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setNxRegLoad(value).build();
        case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
            return new NxActionRegLoadNotifGroupDescStatsUpdatedCaseBuilder().setNxRegLoad(value).build();
        default:
            throw new CodecPreconditionException(path);
        }
    }
    
    @Override
    public Action convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxActionArg) {
        Preconditions.checkArgument(nxActionArg instanceof NxActionRegLoadGrouping);
        
        NxActionRegLoadGrouping nxAction = (NxActionRegLoadGrouping) nxActionArg;
        Dst dst = nxAction.getNxRegLoad().getDst();
        ActionRegLoadBuilder actionRegLoadBuilder = new ActionRegLoadBuilder();
        actionRegLoadBuilder.setDst(RegMoveConvertor.resolveDst(dst.getDstChoice()));
        actionRegLoadBuilder.setOfsNbits((dst.getStart() << 6) | (dst.getEnd() - dst.getStart()));
        actionRegLoadBuilder.setValue(nxAction.getNxRegLoad().getValue());
        OfjAugNxActionBuilder augNxActionBuilder = new OfjAugNxActionBuilder();
        augNxActionBuilder.setActionRegLoad(actionRegLoadBuilder.build());
        return ActionUtil.createNiciraAction(augNxActionBuilder.build(), NxmNxRegLoad.class);
    }

   
}
