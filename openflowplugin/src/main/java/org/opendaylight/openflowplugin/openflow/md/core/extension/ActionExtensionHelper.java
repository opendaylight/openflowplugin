/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterActionSerializerKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.ExtensionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.ExtensionCaseBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
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
    processAllExtensions(Action action, OpenflowVersion ofVersion, ActionPath actionPath) {
        Extension extension = processExtension(action, ofVersion, actionPath);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase = null;
        
        switch (actionPath) {
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
            ExtensionCaseBuilder generalExtActionCaseBld1 = new ExtensionCaseBuilder();
            generalExtActionCaseBld1.setExtension(extension);
            actionCase = generalExtActionCaseBld1.build();
            break;
        case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.ExtensionCaseBuilder generalExtActionCaseBld2 = 
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.ExtensionCaseBuilder();
            generalExtActionCaseBld2.setExtension(extension);
            actionCase = generalExtActionCaseBld2.build();
            break;
        case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.ExtensionCaseBuilder generalExtActionCaseBld3 = 
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.ExtensionCaseBuilder();
            generalExtActionCaseBld3.setExtension(extension);
            actionCase = generalExtActionCaseBld3.build();
            break;
        default:
            LOG.warn("actionPath not supported: {}", actionPath);
        }
        
        return actionCase;
    }

    /**
     * @param ofVersion 
     * @param actionPath
     * @param matchBuilder
     * @param match
     * @return 
     */
    private static Extension processExtension(Action action, OpenflowVersion ofVersion, ActionPath actionPath) {
        /** TODO: EXTENSION PROPOSAL (action, OFJava to MD-SAL) */
        ExperimenterActionSerializerKey key = new ExperimenterActionSerializerKey(
                ofVersion.getVersion(), action.getAugmentation(ExperimenterIdAction.class).getExperimenter().getValue());
                
        ConvertorFromOFJava<Action, ActionPath> convertor = OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
        ExtensionAugment<? extends Augmentation<Extension>> extensionAction = convertor.convert(
                action, actionPath);
        ExtensionBuilder extBld = new ExtensionBuilder();
        extBld.addAugmentation(extensionAction.getAugmentationClass(), extensionAction.getAugmentationObject());
        ExtensionCaseBuilder extCaseBld = new ExtensionCaseBuilder();
        extCaseBld.setExtension(extBld.build());    
        return extBld.build();
    }

}
