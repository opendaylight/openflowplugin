/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionOutputRegGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.src.choice.grouping.SrcChoice;

public class OutputRegConvertorProxy implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
                        Action> {

    private static final OutputRegConvertor OUTPUT_REG_CONVERTOR = new OutputRegConvertor();
    private static final OutputReg2Convertor OUTPUT_REG_2_CONVERTOR = new OutputReg2Convertor();

    @Override
    public Action convert(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase) {
        Preconditions.checkArgument(actionCase instanceof NxActionOutputRegGrouping);
        SrcChoice srcChoice = ((NxActionOutputRegGrouping) actionCase).getNxOutputReg().getSrc().getSrcChoice();
        if (FieldChoiceResolver.isExperimenter(srcChoice)) {
            return OUTPUT_REG_2_CONVERTOR.convert(actionCase);
        } else {
            return OUTPUT_REG_CONVERTOR.convert(actionCase);
        }
    }
}
