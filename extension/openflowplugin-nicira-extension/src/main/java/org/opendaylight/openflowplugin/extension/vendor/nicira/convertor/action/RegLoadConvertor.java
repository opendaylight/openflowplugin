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
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.action.rev140421.OfjAugNxActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.action.rev140421.ofj.nx.action.reg.load.grouping.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.action.rev140421.ofj.nx.action.reg.load.grouping.ActionRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg0;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg5;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg7;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifGroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifGroupDescStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.DstChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.NxRegCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.NxRegCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.NxTunIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.NxTunIdCaseBuilder;
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
public class RegLoadConvertor implements ConvertorToOFJava<Action>, ConvertorFromOFJava<Action, ActionPath> {

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
        ActionRegLoad actionRegLoad = input.getAugmentation(OfjAugNxAction.class).getActionRegLoad();
        DstBuilder dstBuilder = new DstBuilder();
        dstBuilder.setDstChoice(resolveDst(actionRegLoad.getDst()));
        dstBuilder.setFrom(resolveFrom(actionRegLoad.getOfsNbits()));
        dstBuilder.setTo(resolveTo(actionRegLoad.getOfsNbits()));
        NxRegLoadBuilder nxRegLoadBuilder = new NxRegLoadBuilder();
        nxRegLoadBuilder.setDst(dstBuilder.build());
        nxRegLoadBuilder.setValue(actionRegLoad.getValue());
        return resolveAugmentation(nxRegLoadBuilder.build(), path, NxActionRegLoadKey.class);
    }

    private static DstChoice resolveDst(long dstValue) {
        Class<? extends NxmNxReg> potentialDst = resolveReg(dstValue);
        if (potentialDst != null) {
            return new NxRegCaseBuilder().setNxmNxReg(potentialDst).build();
        }
        if (dstValue == NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong()) {
            return new NxTunIdCaseBuilder().setTunId(true).build();
        }
        throw new CodecPreconditionException("Missing codec for " + new NxmHeader(dstValue));
    }

    private static Class<? extends NxmNxReg> resolveReg(long dstValue) {
        if (dstValue == NiciraMatchCodecs.REG0_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg0.class;
        }
        if (dstValue == NiciraMatchCodecs.REG1_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg1.class;
        }
        if (dstValue == NiciraMatchCodecs.REG2_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg2.class;
        }
        if (dstValue == NiciraMatchCodecs.REG3_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg3.class;
        }
        if (dstValue == NiciraMatchCodecs.REG4_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg4.class;
        }
        if (dstValue == NiciraMatchCodecs.REG5_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg5.class;
        }
        if (dstValue == NiciraMatchCodecs.REG6_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg6.class;
        }
        if (dstValue == NiciraMatchCodecs.REG7_CODEC.getHeaderWithoutHasMask().toLong()) {
            return NxmNxReg7.class;
        }
        return null;
    }

    private static int resolveFrom(int ofsNBints) {
        return extractSub(ofsNBints, 10, 6);
    }

    private static int resolveTo(int ofsNBints) {
        int ofs = extractSub(ofsNBints, 10, 6);
        int nBits = extractSub(ofsNBints, 6, 0);
        return ofs + nBits - 1;
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
            return new ExtensionAugment<>(NxAugActionNodesNodeTableFlowWriteActionsCase.class,
                    new NxAugActionNodesNodeTableFlowWriteActionsCaseBuilder().setNxRegLoad(value).build(), key);
        case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
            return new ExtensionAugment<>(NxAugActionNodesNodeTableFlowApplyActionsCase.class,
                    new NxAugActionNodesNodeTableFlowApplyActionsCaseBuilder().setNxRegLoad(value).build(), key);
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
            return new ExtensionAugment<>(NxAugActionNotifFlowsStatisticsUpdateWriteActionsCase.class,
                    new NxAugActionNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setNxRegLoad(value).build(), key);
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
            return new ExtensionAugment<>(NxAugActionNotifFlowsStatisticsUpdateApplyActionsCase.class,
                    new NxAugActionNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setNxRegLoad(value).build(), key);
        case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
            return new ExtensionAugment<>(NxAugActionNotifGroupDescStatsUpdated.class,
                    new NxAugActionNotifGroupDescStatsUpdatedBuilder().setNxRegLoad(value).build(), key);
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
        Optional<NxActionRegLoadGrouping> actionGrouping = ActionUtil.regLoadResolver.getExtension(extension);
        if (!actionGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        Dst dst = actionGrouping.get().getNxRegLoad().getDst();
        ActionRegLoadBuilder actionRegLoadBuilder = new ActionRegLoadBuilder();
        actionRegLoadBuilder.setDst(resolveDst(dst.getDstChoice()));
        actionRegLoadBuilder.setOfsNbits((dst.getFrom() << 6) | (dst.getTo() - dst.getFrom()));
        actionRegLoadBuilder.setValue(actionGrouping.get().getNxRegLoad().getValue());
        OfjAugNxActionBuilder augNxActionBuilder = new OfjAugNxActionBuilder();
        augNxActionBuilder.setActionRegLoad(actionRegLoadBuilder.build());
        return ActionUtil.createNiciraAction(augNxActionBuilder.build());
    }

    private static long resolveDst(DstChoice dstChoice) {
        if (dstChoice instanceof NxRegCase) {
            return resolveReg(((NxRegCase) dstChoice).getNxmNxReg());
        }
        if (dstChoice instanceof NxTunIdCase) {
            return NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong();
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
