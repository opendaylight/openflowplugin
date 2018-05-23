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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.DstChoice;

/**
 * RegLoad SAL actions are converted to RegLoad2 action for experimenter
 * fields, or to RegLoad action otherwise.
 */
public class RegLoadConvertorProxy implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
                Action> {

    private static final RegLoadConvertor REG_LOAD_CONVERTOR = new RegLoadConvertor();
    private static final RegLoad2Convertor REG_LOAD2_CONVERTOR = new RegLoad2Convertor();

    @Override
    public Action convert(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase) {
        Preconditions.checkArgument(actionCase instanceof NxActionRegLoadGrouping);
        DstChoice dstChoice = ((NxActionRegLoadGrouping) actionCase).getNxRegLoad().getDst().getDstChoice();
        if (FieldChoiceResolver.isExperimenter(dstChoice)) {
            return REG_LOAD2_CONVERTOR.convert(actionCase);
        } else {
            return REG_LOAD_CONVERTOR.convert(actionCase);
        }
    }
}
