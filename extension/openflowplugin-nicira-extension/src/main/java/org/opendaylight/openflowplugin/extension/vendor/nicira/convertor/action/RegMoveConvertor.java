/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import org.opendaylight.openflowjava.nx.NiciraMatchCodecs;
import org.opendaylight.openflowjava.nx.codec.match.NxmHeader;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.action.rev140421.OfjAugNxActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.action.rev140421.ofj.nx.action.reg.move.grouping.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.action.rev140421.ofj.nx.action.reg.move.grouping.ActionRegMoveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.NxmNxReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.NxmNxReg0;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.NxmNxReg1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.NxmNxReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.NxmNxReg3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.NxmNxReg4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.NxmNxReg5;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.NxmNxReg6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.NxmNxReg7;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegMoveGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegMoveKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowWriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowWriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateWriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateWriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifGroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifGroupDescStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.DstChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxArpShaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxArpThaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIpv4DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIpv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpSpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpTpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfEthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfEthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.NxRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.NxRegMoveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.SrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.SrcChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxArpShaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxArpThaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxRegCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxRegCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIpv4DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIpv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpSpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpTpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthSrcCaseBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import com.google.common.base.Optional;

/**
 * @author msunal
 *
 */
public class RegMoveConvertor implements ConvertorToOFJava<Action>, ConvertorFromOFJava<Action, ActionPath> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava#convert
     * (org.opendaylight.yangtools.yang.binding.DataContainer,
     * org.opendaylight.openflowplugin.extension.api.path.AugmentationPath)
     */
    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(Action input, ActionPath path) {
        ActionRegMove actionRegMove = input.getAugmentation(OfjAugNxAction.class).getActionRegMove();
        DstBuilder dstBuilder = new DstBuilder();
        dstBuilder.setDstChoice(resolveDst(actionRegMove.getDst()));
        dstBuilder.setStart(actionRegMove.getDstOfs());
        dstBuilder.setEnd(actionRegMove.getDstOfs() + actionRegMove.getNBits());
        SrcBuilder srcBuilder = new SrcBuilder();
        srcBuilder.setSrcChoice(resolveSrc(actionRegMove.getSrc()));
        srcBuilder.setStart(actionRegMove.getSrcOfs());
        srcBuilder.setEnd(actionRegMove.getSrcOfs() + actionRegMove.getNBits());
        NxRegMoveBuilder nxRegMoveBuilder = new NxRegMoveBuilder();
        nxRegMoveBuilder.setDst(dstBuilder.build());
        nxRegMoveBuilder.setSrc(srcBuilder.build());
        return resolveAugmentation(nxRegMoveBuilder.build(), path, NxActionRegMoveKey.class);
    }

    public static DstChoice resolveDst(long dstValue) {
        Class<? extends NxmNxReg> potentialDst = resolveReg(dstValue);
        if (potentialDst != null) {
            return new DstNxRegCaseBuilder().setNxReg(potentialDst).build();
        }
        if (dstValue == NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstNxTunIdCaseBuilder().setNxTunId(true).build();
        }
        if (dstValue == NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstNxArpShaCaseBuilder().setNxArpSha(true).build();
        }
        if (dstValue == NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstNxArpThaCaseBuilder().setNxArpTha(true).build();
        }
        if (dstValue == NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstOfArpOpCaseBuilder().setOfArpOp(true).build();
        }
        if (dstValue == NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstOfArpSpaCaseBuilder().setOfArpSpa(true).build();
        }
        if (dstValue == NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstOfArpTpaCaseBuilder().setOfArpTpa(true).build();
        }
        if (dstValue == NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstNxTunIpv4DstCaseBuilder().setNxTunIpv4Dst(true).build();
        }
        if (dstValue == NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstNxTunIpv4SrcCaseBuilder().setNxTunIpv4Src(true).build();
        }
        if (dstValue == NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstOfEthDstCaseBuilder().setOfEthDst(true).build();
        }
        if (dstValue == NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstOfEthSrcCaseBuilder().setOfEthSrc(true).build();
        }
        throw new CodecPreconditionException("Missing codec for " + new NxmHeader(dstValue));
    }

    private static SrcChoice resolveSrc(long srcValue) {
        Class<? extends NxmNxReg> potentialSrc = resolveReg(srcValue);
        if (potentialSrc != null) {
            return new SrcNxRegCaseBuilder().setNxReg(potentialSrc).build();
        }
        if (srcValue == NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcNxTunIdCaseBuilder().setNxTunId(true).build();
        }
        if (srcValue == NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcNxArpShaCaseBuilder().setNxArpSha(true).build();
        }
        if (srcValue == NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcNxArpThaCaseBuilder().setNxArpTha(true).build();
        }
        if (srcValue == NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcOfArpOpCaseBuilder().setOfArpOp(true).build();
        }
        if (srcValue == NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcOfArpSpaCaseBuilder().setOfArpSpa(true).build();
        }
        if (srcValue == NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcOfArpTpaCaseBuilder().setOfArpTpa(true).build();
        }
        if (srcValue == NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcNxTunIpv4DstCaseBuilder().setNxTunIpv4Dst(true).build();
        }
        if (srcValue == NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcNxTunIpv4SrcCaseBuilder().setNxTunIpv4Src(true).build();
        }
        if (srcValue == NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcOfEthDstCaseBuilder().setOfEthDst(true).build();
        }
        if (srcValue == NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcOfEthSrcCaseBuilder().setOfEthSrc(true).build();
        }
        throw new CodecPreconditionException("Missing codec for " + new NxmHeader(srcValue));
    }

    private static Class<? extends NxmNxReg> resolveReg(long value) {
        if (value == NiciraMatchCodecs.REG0_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg0.class;
        }
        if (value == NiciraMatchCodecs.REG1_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg1.class;
        }
        if (value == NiciraMatchCodecs.REG2_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg2.class;
        }
        if (value == NiciraMatchCodecs.REG3_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg3.class;
        }
        if (value == NiciraMatchCodecs.REG4_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg4.class;
        }
        if (value == NiciraMatchCodecs.REG5_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg5.class;
        }
        if (value == NiciraMatchCodecs.REG6_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg6.class;
        }
        if (value == NiciraMatchCodecs.REG7_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg7.class;
        }
        return null;
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(NxRegMove value,
            ActionPath path, Class<? extends ExtensionKey> key) {
        switch (path) {
        case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
            return new ExtensionAugment<>(NxAugActionNodesNodeTableFlowWriteActions.class,
                    new NxAugActionNodesNodeTableFlowWriteActionsBuilder().setNxRegMove(value).build(), key);
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
            return new ExtensionAugment<>(NxAugActionNotifFlowsStatisticsUpdateWriteActions.class,
                    new NxAugActionNotifFlowsStatisticsUpdateWriteActionsBuilder().setNxRegMove(value).build(), key);
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
            return new ExtensionAugment<>(NxAugActionNotifFlowsStatisticsUpdateApplyActions.class,
                    new NxAugActionNotifFlowsStatisticsUpdateApplyActionsBuilder().setNxRegMove(value).build(), key);
        case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
            return new ExtensionAugment<>(NxAugActionNotifGroupDescStatsUpdated.class,
                    new NxAugActionNotifGroupDescStatsUpdatedBuilder().setNxRegMove(value).build(), key);
        default:
            throw new CodecPreconditionException(path);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava#convert
     * (org
     * .opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general
     * .rev140714.general.extension.grouping.Extension)
     */
    @Override
    public Action convert(Extension extension) {
        Optional<NxActionRegMoveGrouping> actionGrouping = ActionUtil.regMoveResolver.getExtension(extension);
        if (!actionGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        Dst dst = actionGrouping.get().getNxRegMove().getDst();
        Src src = actionGrouping.get().getNxRegMove().getSrc();
        ActionRegMoveBuilder actionRegMoveBuilder = new ActionRegMoveBuilder();
        actionRegMoveBuilder.setDst(resolveDst(dst.getDstChoice()));
        actionRegMoveBuilder.setDstOfs(dst.getStart());
        actionRegMoveBuilder.setSrc(resolveSrc(src.getSrcChoice()));
        actionRegMoveBuilder.setSrcOfs(src.getStart());
        actionRegMoveBuilder.setNBits(dst.getEnd() - dst.getStart());
        OfjAugNxActionBuilder augNxActionBuilder = new OfjAugNxActionBuilder();
        augNxActionBuilder.setActionRegMove(actionRegMoveBuilder.build());
        return ActionUtil.createNiciraAction(augNxActionBuilder.build());
    }

    public static long resolveDst(DstChoice dstChoice) {
        if (dstChoice instanceof DstNxRegCase) {
            return resolveReg(((DstNxRegCase) dstChoice).getNxReg());
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong();
        }
        throw new CodecPreconditionException("Missing implementation of a case in dst-choice? " + dstChoice.getClass());
    }

    private static long resolveSrc(SrcChoice dstChoice) {
        if (dstChoice instanceof SrcNxRegCase) {
            return resolveReg(((SrcNxRegCase) dstChoice).getNxReg());
        }
        if (dstChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong();
        }
        throw new CodecPreconditionException("Missing implementation of a case in dst-choice? " + dstChoice.getClass());
    }

    private static long resolveReg(Class<? extends NxmNxReg> reg) {
        if (reg.equals(NxmNxReg0.class)) {
            return NiciraMatchCodecs.REG0_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (reg.equals(NxmNxReg1.class)) {
            return NiciraMatchCodecs.REG1_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (reg.equals(NxmNxReg2.class)) {
            return NiciraMatchCodecs.REG2_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (reg.equals(NxmNxReg3.class)) {
            return NiciraMatchCodecs.REG3_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (reg.equals(NxmNxReg4.class)) {
            return NiciraMatchCodecs.REG4_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (reg.equals(NxmNxReg5.class)) {
            return NiciraMatchCodecs.REG5_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (reg.equals(NxmNxReg6.class)) {
            return NiciraMatchCodecs.REG6_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (reg.equals(NxmNxReg7.class)) {
            return NiciraMatchCodecs.REG7_CODEC.getHeaderWithoutHasMask().toLong();
        }
        throw new CodecPreconditionException("Missing codec for nxm_nx_reg?" + reg);
    }

}
