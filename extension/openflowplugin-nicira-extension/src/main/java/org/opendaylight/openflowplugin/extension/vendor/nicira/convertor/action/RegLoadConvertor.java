/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import org.opendaylight.openflowjava.nx.NiciraMatchCodecs;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.action.rev140421.OfjAugNxActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.dst.DstChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.dst.dst.choice.RegCase;
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
        // TODO Auto-generated method stub
        return null;
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
        if (dstChoice instanceof RegCase) {
            RegCase dstReg = (RegCase) dstChoice;
            return resolveReg(dstReg.getNxmNxReg());
        }
        throw new CodecPreconditionException("Missing implementation of a case in dst-choice? " + dstChoice.getClass());
    }

    private static long resolveReg(Class<? extends NxmNxReg> reg) {
        if (reg.equals(NxmNxReg0.class)) {
            return NiciraMatchCodecs.REG0_CODEC.serializeHeaderToLong(false);
        }
        if (reg.equals(NxmNxReg1.class)) {
            return NiciraMatchCodecs.REG1_CODEC.serializeHeaderToLong(false);
        }
        if (reg.equals(NxmNxReg2.class)) {
            return NiciraMatchCodecs.REG2_CODEC.serializeHeaderToLong(false);
        }
        if (reg.equals(NxmNxReg3.class)) {
            return NiciraMatchCodecs.REG3_CODEC.serializeHeaderToLong(false);
        }
        if (reg.equals(NxmNxReg4.class)) {
            return NiciraMatchCodecs.REG4_CODEC.serializeHeaderToLong(false);
        }
        if (reg.equals(NxmNxReg5.class)) {
            return NiciraMatchCodecs.REG5_CODEC.serializeHeaderToLong(false);
        }
        if (reg.equals(NxmNxReg6.class)) {
            return NiciraMatchCodecs.REG6_CODEC.serializeHeaderToLong(false);
        }
        if (reg.equals(NxmNxReg7.class)) {
            return NiciraMatchCodecs.REG7_CODEC.serializeHeaderToLong(false);
        }
        throw new CodecPreconditionException("Missing codec for nxm_nx_reg?");
    }

}
