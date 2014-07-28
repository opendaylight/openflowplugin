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
        ExtensionBuilder extensionBld = processExtension(action, ofVersion, actionPath);
        if (extensionBld == null) {
            return null;
        }
        
        Extension extension = extensionBld.build();
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase = null;
        
        switch (actionPath) {
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
            //TODO: improve type safe
            ExperimenterActionSerializerKey key = new ExperimenterActionSerializerKey(
                    ofVersion.getVersion(), action.getAugmentation(ExperimenterIdAction.class).getExperimenter().getValue(), action.getAugmentation(ExperimenterIdAction.class).getSubType());
            
            ConvertorActionFromOFJava<Action, ActionPath> convertor = (ConvertorActionFromOFJava<Action, ActionPath>) OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
            if (convertor != null) {
                //actionCase = convertor.convert2(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
                actionCase = convertor.convert2(action, actionPath);
            }
            break;
        case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
            ExtensionNodesNodeTableFlowWriteActionsCaseBuilder generalExtActionCaseBld2 = new ExtensionNodesNodeTableFlowWriteActionsCaseBuilder();
            generalExtActionCaseBld2.setExtension(extension);
            actionCase = generalExtActionCaseBld2.build();
            break;
        case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
            ExtensionNotifGroupDescStatsUpdatedCaseBuilder generalExtActionCaseBld3 = new ExtensionNotifGroupDescStatsUpdatedCaseBuilder();
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
    private static ExtensionBuilder processExtension(Action action, OpenflowVersion ofVersion, ActionPath actionPath) {
        ExtensionBuilder extBld = null;
        
        /** TODO: EXTENSION PROPOSAL (action, OFJava to MD-SAL) */
        ExperimenterActionSerializerKey key = new ExperimenterActionSerializerKey(
                ofVersion.getVersion(), action.getAugmentation(ExperimenterIdAction.class).getExperimenter().getValue(), action.getAugmentation(ExperimenterIdAction.class).getSubType());
                
        ConvertorFromOFJava<Action, ActionPath> convertor = OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
        if (convertor != null) {
            
            ExtensionAugment<? extends Augmentation<Extension>> extensionAction = convertor.convert(
                    action, actionPath);
            extBld = new ExtensionBuilder();
            extBld.addAugmentation(extensionAction.getAugmentationClass(), extensionAction.getAugmentationObject());
            ExtensionNotifFlowsStatisticsUpdateWriteActionsCaseBuilder extCaseBld = new ExtensionNotifFlowsStatisticsUpdateWriteActionsCaseBuilder();
            extCaseBld.setExtension(extBld.build());
            extCaseBld.setExtensionKey(extensionAction.getKey());
        }
        return extBld;
    }

}
