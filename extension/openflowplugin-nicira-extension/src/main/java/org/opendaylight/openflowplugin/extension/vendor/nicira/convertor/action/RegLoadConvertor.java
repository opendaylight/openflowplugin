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
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.action.rev140421.NxmNxRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.action.rev140421.OfjAugNxActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.action.rev140421.ofj.nx.action.reg.load.grouping.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.action.rev140421.ofj.nx.action.reg.load.grouping.ActionRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowWriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowWriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateWriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateWriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifGroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifGroupDescStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadRpcAddFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.DstBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import com.google.common.base.Optional;

/**
 * @author msunal
 *
 */
public class RegLoadConvertor implements ConvertorToOFJava<Action>, ConvertorFromOFJava<Action, ActionPath>, 
ConvertorActionToOFJava<NxActionRegLoadNodesNodeTableFlowApplyActionsCase, Action>,
ConvertorActionFromOFJava<Action, ActionPath>{

    @Override
    @Deprecated
    public ExtensionAugment<? extends Augmentation<Extension>> convert(Action input, ActionPath path) {
        ActionRegLoad actionRegLoad = input.getAugmentation(OfjAugNxAction.class).getActionRegLoad();
        DstBuilder dstBuilder = new DstBuilder();
        dstBuilder.setDstChoice(RegMoveConvertor.resolveDst(actionRegLoad.getDst()));
        dstBuilder.setStart(resolveStart(actionRegLoad.getOfsNbits()));
        dstBuilder.setEnd(resolveEnd(actionRegLoad.getOfsNbits()));
        NxRegLoadBuilder nxRegLoadBuilder = new NxRegLoadBuilder();
        nxRegLoadBuilder.setDst(dstBuilder.build());
        nxRegLoadBuilder.setValue(actionRegLoad.getValue());
        return resolveAugmentation(nxRegLoadBuilder.build(), path, NxActionRegLoadKey.class);
    }
    
    @Override
    public <TO extends org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> TO convert2(Action input, ActionPath path) {
        ActionRegLoad actionRegLoad = input.getAugmentation(OfjAugNxAction.class).getActionRegLoad();
        DstBuilder dstBuilder = new DstBuilder();
        dstBuilder.setDstChoice(RegMoveConvertor.resolveDst(actionRegLoad.getDst()));
        dstBuilder.setStart(resolveStart(actionRegLoad.getOfsNbits()));
        dstBuilder.setEnd(resolveEnd(actionRegLoad.getOfsNbits()));
        NxRegLoadBuilder nxRegLoadBuilder = new NxRegLoadBuilder();
        nxRegLoadBuilder.setDst(dstBuilder.build());
        nxRegLoadBuilder.setValue(actionRegLoad.getValue());
        return (TO) resolveAction(nxRegLoadBuilder.build(), path);
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

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(NxRegLoad value,
            ActionPath path, Class<? extends ExtensionKey> key) {
        switch (path) {
        case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
            return new ExtensionAugment<>(NxAugActionNodesNodeTableFlowWriteActions.class,
                    new NxAugActionNodesNodeTableFlowWriteActionsBuilder().setNxRegLoad(value).build(), key);
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
            return new ExtensionAugment<>(NxAugActionNotifFlowsStatisticsUpdateWriteActions.class,
                    new NxAugActionNotifFlowsStatisticsUpdateWriteActionsBuilder().setNxRegLoad(value).build(), key);
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
            // TODO: !HACK! remove after actions are in stats
//            return new ExtensionAugment<>(NxAugActionNotifFlowsStatisticsUpdateApplyActions.class,
                    //new NxAugActionNotifFlowsStatisticsUpdateApplyActionsBuilder().setNxRegLoad(value).build(), key);
            return new ExtensionAugment<>(NxAugActionNotifFlowsStatisticsUpdateWriteActions.class,
                    new NxAugActionNotifFlowsStatisticsUpdateWriteActionsBuilder().setNxRegLoad(value).build(), key);
        case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
            return new ExtensionAugment<>(NxAugActionNotifGroupDescStatsUpdated.class,
                    new NxAugActionNotifGroupDescStatsUpdatedBuilder().setNxRegLoad(value).build(), key);
        default:
            throw new CodecPreconditionException(path);
        }
    }
    
    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(NxRegLoad value, ActionPath path) {
        switch (path) {
        case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
            return new NxActionRegLoadNodesNodeTableFlowApplyActionsCaseBuilder().setNxRegLoad(value).build();
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
            return new NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setNxRegLoad(value).build();
        default:
            throw new CodecPreconditionException(path);
        }
    }

    @Override
    @Deprecated
    public Action convert(Extension extension) {
        Optional<NxActionRegLoadGrouping> actionGrouping = ActionUtil.regLoadResolver.getExtension(extension);
        if (!actionGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        Dst dst = actionGrouping.get().getNxRegLoad().getDst();
        ActionRegLoadBuilder actionRegLoadBuilder = new ActionRegLoadBuilder();
        actionRegLoadBuilder.setDst(RegMoveConvertor.resolveDst(dst.getDstChoice()));
        actionRegLoadBuilder.setOfsNbits((dst.getStart() << 6) | (dst.getEnd() - dst.getStart()));
        actionRegLoadBuilder.setValue(actionGrouping.get().getNxRegLoad().getValue());
        OfjAugNxActionBuilder augNxActionBuilder = new OfjAugNxActionBuilder();
        augNxActionBuilder.setActionRegLoad(actionRegLoadBuilder.build());
        return ActionUtil.createNiciraAction(augNxActionBuilder.build(), NxmNxRegLoad.class);
    }
    
    @Override
    public Action convert(NxActionRegLoadNodesNodeTableFlowApplyActionsCase nxAction) {
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
