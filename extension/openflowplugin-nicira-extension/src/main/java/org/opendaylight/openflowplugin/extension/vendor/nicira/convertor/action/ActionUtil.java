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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcAddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcRemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcTransmitPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateFlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateGroupOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxAugActionRpcUpdateGroupUpdated;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import com.google.common.base.Optional;

/**
 * @author msunal
 *
 */
public class ActionUtil {

    private final static Set<Class<? extends Augmentation<Extension>>> augmentationsOfExtension = new HashSet<>();
    private final static ExperimenterIdAction EXPERIMENTER_ID_ACTION;
    private final static GroupingResolver<NxActionRegLoadGrouping, Extension> regLoadResolver = new GroupingResolver<>(
            NxActionRegLoadGrouping.class);

    static {
        augmentationsOfExtension.add(NxAugActionRpcAddFlow.class);
        augmentationsOfExtension.add(NxAugActionRpcRemoveFlow.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateFlowOriginal.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateFlowUpdated.class);
        augmentationsOfExtension.add(NxAugActionRpcAddGroup.class);
        augmentationsOfExtension.add(NxAugActionRpcRemoveGroup.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateGroupOriginal.class);
        augmentationsOfExtension.add(NxAugActionRpcUpdateGroupUpdated.class);
        augmentationsOfExtension.add(NxAugActionRpcTransmitPacket.class);
        augmentationsOfExtension.add(NxAugActionNodesNodeTableFlow.class);
        augmentationsOfExtension.add(NxAugActionNotifUpdateFlowStats.class);
        regLoadResolver.setAugmentations(augmentationsOfExtension);
        ExperimenterIdActionBuilder experimenterIdActionBuilder = new ExperimenterIdActionBuilder();
        experimenterIdActionBuilder.setExperimenter(new ExperimenterId(NiciraConstants.NX_VENDOR_ID));
        EXPERIMENTER_ID_ACTION = experimenterIdActionBuilder.build();
    }

    public static Optional<NxActionRegLoadGrouping> getNxActionRegLoadGrouping(Extension data) {
        return regLoadResolver.getExtension(data);
    }

    public static ExperimenterIdAction getNiciraExperimenterIdAction() {
        return EXPERIMENTER_ID_ACTION;
    }

}
