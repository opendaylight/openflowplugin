/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import com.google.common.base.Preconditions;

import org.opendaylight.openflowjava.nx.NiciraMatchCodecs;
import org.opendaylight.openflowjava.nx.codec.match.NxmHeader;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMoveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.move.grouping.NxActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.move.grouping.NxActionRegMoveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg0;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg5;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg7;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegMoveGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.DstChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxArpShaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxArpShaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxArpThaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxArpThaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIpv4SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpOpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpSpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpSpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpTpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfArpTpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIpv4DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfEthDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfEthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfEthSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstOfEthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionRegMoveNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.NxRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.NxRegMoveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.move.grouping.nx.reg.move.SrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.SrcChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxArpShaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxArpShaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxArpThaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxArpThaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxRegCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxRegCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIpv4DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpOpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpSpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpSpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpTpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfArpTpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcNxTunIpv4SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthTypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.src.choice.SrcOfEthTypeCaseBuilder;

/**
 * @author msunal
 */
public class RegMoveConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(Action input, ActionPath path) {
        NxActionRegMove actionRegMove = ((ActionRegMove) input.getActionChoice()).getNxActionRegMove();
        DstBuilder dstBuilder = new DstBuilder();
        dstBuilder.setDstChoice(resolveDst(actionRegMove.getDst()));
        dstBuilder.setStart(actionRegMove.getDstOfs());
        dstBuilder.setEnd(actionRegMove.getDstOfs() + actionRegMove.getNBits() - 1);
        SrcBuilder srcBuilder = new SrcBuilder();
        srcBuilder.setSrcChoice(resolveSrc(actionRegMove.getSrc()));
        srcBuilder.setStart(actionRegMove.getSrcOfs());
        srcBuilder.setEnd(actionRegMove.getSrcOfs() + actionRegMove.getNBits() - 1);
        NxRegMoveBuilder nxRegMoveBuilder = new NxRegMoveBuilder();
        nxRegMoveBuilder.setDst(dstBuilder.build());
        nxRegMoveBuilder.setSrc(srcBuilder.build());
        return resolveAction(nxRegMoveBuilder.build(), path);
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
        if (dstValue == NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstOfEthDstCaseBuilder().setOfEthDst(true).build();
        }
        if (dstValue == NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new DstOfEthSrcCaseBuilder().setOfEthSrc(true).build();
        }
        throw new CodecPreconditionException("Missing codec for " + new NxmHeader(dstValue));
    }

    static SrcChoice resolveSrc(long srcValue) {
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
        if (srcValue == NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcOfEthDstCaseBuilder().setOfEthDst(true).build();
        }
        if (srcValue == NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcOfEthSrcCaseBuilder().setOfEthSrc(true).build();
        }
        if (srcValue == NiciraMatchCodecs.ETH_TYPE_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new SrcOfEthTypeCaseBuilder().setOfEthType(true).build();
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

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            NxRegMove value, ActionPath path) {
        switch (path) {
            case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
                return new NxActionRegMoveNodesNodeTableFlowWriteActionsCaseBuilder().setNxRegMove(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
                return new NxActionRegMoveNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setNxRegMove(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
                return new NxActionRegMoveNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setNxRegMove(value).build();
            case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
                return new NxActionRegMoveNotifGroupDescStatsUpdatedCaseBuilder().setNxRegMove(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }

    @Override
    public Action convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxActionArg) {
        Preconditions.checkArgument(nxActionArg instanceof NxActionRegMoveGrouping);
        NxActionRegMoveGrouping nxAction = (NxActionRegMoveGrouping) nxActionArg;

        Dst dst = nxAction.getNxRegMove().getDst();
        Src src = nxAction.getNxRegMove().getSrc();
        ActionRegMoveBuilder actionRegMoveBuilder = new ActionRegMoveBuilder();
        NxActionRegMoveBuilder nxActionRegMove = new NxActionRegMoveBuilder();

        nxActionRegMove.setDst(resolveDst(dst.getDstChoice()));
        nxActionRegMove.setDstOfs(dst.getStart());
        nxActionRegMove.setSrc(resolveSrc(src.getSrcChoice()));
        nxActionRegMove.setSrcOfs(src.getStart());
        nxActionRegMove.setNBits(dst.getEnd() - dst.getStart() + 1);
        actionRegMoveBuilder.setNxActionRegMove(nxActionRegMove.build());
        return ActionUtil.createAction(actionRegMoveBuilder.build());
    }

    public static long resolveDst(DstChoice dstChoice) {
        if (dstChoice instanceof DstNxRegCase) {
            return resolveReg(((DstNxRegCase) dstChoice).getNxReg());
        }
        if (dstChoice instanceof DstNxTunIdCase) {
            return NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxArpShaCase) {
            return NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxArpThaCase) {
            return NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstOfArpOpCase) {
            return NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstOfArpSpaCase) {
            return NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstOfArpTpaCase) {
            return NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIpv4DstCase) {
            return NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstNxTunIpv4SrcCase) {
            return NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstOfEthDstCase) {
            return NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (dstChoice instanceof DstOfEthSrcCase) {
            return NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong();
        }
        throw new CodecPreconditionException("Missing implementation of a case in dst-choice? " + dstChoice.getClass());
    }

    static long resolveSrc(SrcChoice srcChoice) {
        if (srcChoice instanceof SrcNxRegCase) {
            return resolveReg(((SrcNxRegCase) srcChoice).getNxReg());
        }
        if (srcChoice instanceof SrcNxTunIdCase) {
            return NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (srcChoice instanceof SrcNxArpShaCase) {
            return NiciraMatchCodecs.ARP_SHA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (srcChoice instanceof SrcNxArpThaCase) {
            return NiciraMatchCodecs.ARP_THA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (srcChoice instanceof SrcOfArpOpCase) {
            return NiciraMatchCodecs.ARP_OP_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (srcChoice instanceof SrcOfArpSpaCase) {
            return NiciraMatchCodecs.ARP_SPA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (srcChoice instanceof SrcOfArpTpaCase) {
            return NiciraMatchCodecs.ARP_TPA_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (srcChoice instanceof SrcNxTunIpv4DstCase) {
            return NiciraMatchCodecs.TUN_IPV4_DST_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (srcChoice instanceof SrcNxTunIpv4SrcCase) {
            return NiciraMatchCodecs.TUN_IPV4_SRC_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (srcChoice instanceof SrcOfEthDstCase) {
            return NiciraMatchCodecs.ETH_DST_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (srcChoice instanceof SrcOfEthSrcCase) {
            return NiciraMatchCodecs.ETH_SRC_CODEC.getHeaderWithoutHasMask().toLong();
        }
        if (srcChoice instanceof SrcOfEthTypeCase) {
            return NiciraMatchCodecs.ETH_TYPE_CODEC.getHeaderWithoutHasMask().toLong();
        }
        throw new CodecPreconditionException("Missing implementation of a case in dst-choice? " + srcChoice.getClass());
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
