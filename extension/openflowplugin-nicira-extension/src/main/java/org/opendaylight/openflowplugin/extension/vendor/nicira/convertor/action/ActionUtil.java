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
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegMoveGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifGroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcAddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcRemoveFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcRemoveFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcRemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcTransmitPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateFlowOriginalApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateFlowOriginalWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateFlowUpdatedApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateFlowUpdatedWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateGroupOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateGroupUpdated;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * @author msunal
 *
 */
public class ActionUtil {

    private final static Set<Class<? extends Augmentation<Extension>>> augmentationsOfExtension = new HashSet<>();
    public final static ExperimenterIdAction EXPERIMENTER_ID_ACTION;
    public final static GroupingResolver<NxActionRegLoadGrouping, Extension> regLoadResolver = new GroupingResolver<>(
            NxActionRegLoadGrouping.class);
    public final static GroupingResolver<NxActionRegMoveGrouping, Extension> regMoveResolver = new GroupingResolver<>(
            NxActionRegMoveGrouping.class);

    static {
        augmentationsOfExtension.add(NxAugActionRpcAddFlowWriteActionsCase.class);
        augmentationsOfExtension.add(NxAugActionRpcAddFlowApplyActionsCase.class);
        augmentationsOfExtension.add(NxAugActionRpcRemoveFlowWriteActionsCase.class);
        augmentationsOfExtension.add(NxAugActionRpcRemoveFlowApplyActionsCase.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateFlowOriginalWriteActionsCase.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateFlowOriginalApplyActionsCase.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateFlowUpdatedWriteActionsCase.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateFlowUpdatedApplyActionsCase.class);
        augmentationsOfExtension.add(NxAugActionRpcAddGroup.class);
        augmentationsOfExtension.add(NxAugActionRpcRemoveGroup.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateGroupOriginal.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateGroupUpdated.class);
        augmentationsOfExtension.add(NxAugActionRpcTransmitPacket.class);
        augmentationsOfExtension.add(NxAugActionNodesNodeTableFlowWriteActionsCase.class);
        augmentationsOfExtension.add(NxAugActionNodesNodeTableFlowApplyActionsCase.class);
        augmentationsOfExtension.add(NxAugActionNotifFlowsStatisticsUpdateWriteActionsCase.class);
        augmentationsOfExtension.add(NxAugActionNotifFlowsStatisticsUpdateApplyActionsCase.class);
        augmentationsOfExtension.add(NxAugActionNotifGroupDescStatsUpdated.class);
        regLoadResolver.setAugmentations(augmentationsOfExtension);
        regMoveResolver.setAugmentations(augmentationsOfExtension);
        ExperimenterIdActionBuilder experimenterIdActionBuilder = new ExperimenterIdActionBuilder();
        experimenterIdActionBuilder.setExperimenter(new ExperimenterId(NiciraConstants.NX_VENDOR_ID));
        EXPERIMENTER_ID_ACTION = experimenterIdActionBuilder.build();
    }

    public static Action createNiciraAction(OfjAugNxAction augNxAction) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setType(Experimenter.class);
        actionBuilder.addAugmentation(ExperimenterIdAction.class, EXPERIMENTER_ID_ACTION);
        actionBuilder.addAugmentation(OfjAugNxAction.class, augNxAction);
        return actionBuilder.build();
    }
}
