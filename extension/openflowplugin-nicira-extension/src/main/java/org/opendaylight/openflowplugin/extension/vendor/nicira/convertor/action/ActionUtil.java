/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.openflowjava.nx.NiciraConstants;
import org.opendaylight.openflowplugin.extension.api.GroupingResolver;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.ExperimenterActionSubType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegMoveGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowWriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateWriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifGroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcAddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcRemoveFlowApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcRemoveFlowWriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcRemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcTransmitPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateFlowOriginalApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateFlowOriginalWriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateFlowUpdatedApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateFlowUpdatedWriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateGroupOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateGroupUpdated;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * @author msunal
 *
 */
public class ActionUtil {

    private final static Set<Class<? extends Augmentation<Extension>>> augmentationsOfExtension = new HashSet<>();
    private final static ExperimenterIdActionBuilder EXPERIMENTER_ID_ACTION_BUILDER;
    public final static GroupingResolver<NxActionRegLoadGrouping, Extension> regLoadResolver = new GroupingResolver<>(
            NxActionRegLoadGrouping.class);
    public final static GroupingResolver<NxActionRegMoveGrouping, Extension> regMoveResolver = new GroupingResolver<>(
            NxActionRegMoveGrouping.class);

    static {
        augmentationsOfExtension.add(NxAugActionRpcRemoveFlowWriteActions.class);
        augmentationsOfExtension.add(NxAugActionRpcRemoveFlowApplyActions.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateFlowOriginalWriteActions.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateFlowOriginalApplyActions.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateFlowUpdatedWriteActions.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateFlowUpdatedApplyActions.class);
        augmentationsOfExtension.add(NxAugActionRpcAddGroup.class);
        augmentationsOfExtension.add(NxAugActionRpcRemoveGroup.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateGroupOriginal.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateGroupUpdated.class);
        augmentationsOfExtension.add(NxAugActionRpcTransmitPacket.class);
        augmentationsOfExtension.add(NxAugActionNodesNodeTableFlowWriteActions.class);
        augmentationsOfExtension.add(NxAugActionNotifFlowsStatisticsUpdateWriteActions.class);
        augmentationsOfExtension.add(NxAugActionNotifGroupDescStatsUpdated.class);
        regLoadResolver.setAugmentations(augmentationsOfExtension);
        regMoveResolver.setAugmentations(augmentationsOfExtension);
        EXPERIMENTER_ID_ACTION_BUILDER = new ExperimenterIdActionBuilder();
        EXPERIMENTER_ID_ACTION_BUILDER.setExperimenter(new ExperimenterId(NiciraConstants.NX_VENDOR_ID));
    }

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
