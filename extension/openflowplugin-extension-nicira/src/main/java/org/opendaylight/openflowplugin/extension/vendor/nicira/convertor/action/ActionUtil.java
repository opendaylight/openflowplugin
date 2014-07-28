/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.ExperimenterActionSubType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxAction;

/**
 * @author msunal
 *
 */
public class ActionUtil {

    private final static ExperimenterIdActionBuilder EXPERIMENTER_ID_ACTION_BUILDER;

    static {
        EXPERIMENTER_ID_ACTION_BUILDER = new ExperimenterIdActionBuilder();
        EXPERIMENTER_ID_ACTION_BUILDER.setExperimenter(new ExperimenterId(NiciraConstants.NX_VENDOR_ID));
    }

    /**
     * @param augNxAction
     * @param subType
     * @return OFJava action with augmentation containing action subtype and experimenter type
     */
    public static Action createNiciraAction(OfjAugNxAction augNxAction,
            Class<? extends ExperimenterActionSubType> subType) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setType(Experimenter.class);
        actionBuilder.addAugmentation(ExperimenterIdAction.class, EXPERIMENTER_ID_ACTION_BUILDER.setSubType(subType)
                .build());
        actionBuilder.addAugmentation(OfjAugNxAction.class, augNxAction);
        return actionBuilder.build();
    }
}
