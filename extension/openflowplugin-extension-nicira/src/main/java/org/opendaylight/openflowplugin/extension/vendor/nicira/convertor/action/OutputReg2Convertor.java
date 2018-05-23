/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.output.reg2.grouping.NxActionOutputReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.output.reg2.grouping.NxActionOutputReg2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionOutputRegGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.output.reg.grouping.NxOutputRegBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.output.reg.grouping.nx.output.reg.Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.output.reg.grouping.nx.output.reg.SrcBuilder;

public class OutputReg2Convertor  implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
                        Action>, ConvertorActionFromOFJava<Action, ActionPath> {
    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(
            Action input, ActionPath path) {
        NxActionOutputReg2 action = ((ActionOutputReg2) input.getActionChoice()).getNxActionOutputReg2();
        SrcBuilder srcBuilder = new SrcBuilder();
        srcBuilder.setSrcChoice(FieldChoiceResolver.resolveSrcChoice(action.getSrc()));
        srcBuilder.setOfsNbits(action.getNBits());
        NxOutputRegBuilder builder = new NxOutputRegBuilder();
        builder.setSrc(srcBuilder.build());
        builder.setMaxLen(action.getMaxLen());
        return OutputRegConvertor.resolveAction(builder.build(), path);
    }

    @Override
    public Action convert(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase) {
        Preconditions.checkArgument(actionCase instanceof NxActionOutputRegGrouping);
        NxActionOutputRegGrouping nxAction = (NxActionOutputRegGrouping) actionCase;
        Src src = nxAction.getNxOutputReg().getSrc();
        final ActionOutputReg2Builder builder = new ActionOutputReg2Builder();
        NxActionOutputReg2Builder nxActionOutputReg2Builder = new NxActionOutputReg2Builder();
        nxActionOutputReg2Builder.setSrc(FieldChoiceResolver.resolveSrcHeaderUint64(src.getSrcChoice()));
        nxActionOutputReg2Builder.setNBits(src.getOfsNbits());
        nxActionOutputReg2Builder.setMaxLen(nxAction.getNxOutputReg().getMaxLen());
        builder.setNxActionOutputReg2(nxActionOutputReg2Builder.build());
        return ActionUtil.createAction(builder.build());
    }
}
