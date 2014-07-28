/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterActionSerializerKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.ExtensionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.ExtensionNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.ExtensionNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.ExtensionNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.ExtensionNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public final class ActionExtensionHelper {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(ActionExtensionHelper.class);
    
    private ActionExtensionHelper() {
        throw new IllegalAccessError("singleton enforcement");
    }

    /**
     * @param action 
     * @param ofVersion
     * @param actionPath
     * @return augmentation wrapper containing augmentation depending on matchPath
     */
    public static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action
    processAlienAction(Action action, OpenflowVersion ofVersion, ActionPath actionPath) {
        
        /** TODO: EXTENSION PROPOSAL (action, OFJava to MD-SAL) */
        ExperimenterActionSerializerKey key = new ExperimenterActionSerializerKey(
                ofVersion.getVersion(), 
                action.getAugmentation(ExperimenterIdAction.class).getExperimenter().getValue(), 
                action.getAugmentation(ExperimenterIdAction.class).getSubType());
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action alienAction = null;
        ConvertorActionFromOFJava<Action, ActionPath> convertor = OFSessionUtil.getExtensionConvertorProvider().getActionConverter(key);
        if (convertor != null) {
            
            alienAction = convertor.convert(
                    action, actionPath);
        }
        return alienAction;
        
    }
}
