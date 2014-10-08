/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.cisco;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.openflowjava.cof.api.CiscoUtil;
import org.opendaylight.openflowjava.cof.codec.action.NextHopCodec;
import org.opendaylight.openflowjava.cof.codec.action.VrfCodec;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action.NextHopConvertor;
import org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action.VrfConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.CofActionNextHopRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.CofActionVrfRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionNextHopRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionVrfRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.add.group.input.buckets.bucket.action.action.CofActionNextHopRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.add.group.input.buckets.bucket.action.action.CofActionVrfRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.group.buckets.bucket.action.action.CofActionNextHopNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.group.buckets.bucket.action.action.CofActionVrfNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.CofActionNextHopNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.CofActionVrfNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionNextHopNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionVrfNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.remove.group.input.buckets.bucket.action.action.CofActionNextHopRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.remove.group.input.buckets.bucket.action.action.CofActionVrfRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.update.group.input.original.group.buckets.bucket.action.action.CofActionNextHopRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.update.group.input.original.group.buckets.bucket.action.action.CofActionVrfRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.update.group.input.updated.group.buckets.bucket.action.action.CofActionNextHopRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.update.group.input.updated.group.buckets.bucket.action.action.CofActionVrfRpcUpdateGroupUpdatedCase;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * 
 */
public class CiscoExtensionProvider implements AutoCloseable {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(CiscoExtensionProvider.class);

    private ExtensionConverterRegistrator extensionConverterRegistrator;
    private Set<ObjectRegistration<?>> registrations;
    private final static NextHopConvertor NEXT_HOP_CONVERTOR = new NextHopConvertor();
    private final static VrfConvertor VRF_CONVERTOR = new VrfConvertor();

    @Override
    public void close() {
        for (AutoCloseable janitor : registrations) {
            try {
                janitor.close();
            } catch (Exception e) {
                LOG.warn("closing of extension converter failed", e);
            }
        }
        extensionConverterRegistrator = null;
    }

    /**
     * @param extensionConverterRegistrator
     */
    public void setExtensionConverterRegistrator(
            ExtensionConverterRegistrator extensionConverterRegistrator) {
                this.extensionConverterRegistrator = extensionConverterRegistrator;
    }

    /**
     * register appropriate converters
     */
    public void registerConverters() {
        Preconditions.checkNotNull(extensionConverterRegistrator);
        registrations = new HashSet<>();
        // src=dataStore/config
        registerAction13(CofActionNextHopNodesNodeTableFlowApplyActionsCase.class, NEXT_HOP_CONVERTOR);
        registerAction13(CofActionVrfNodesNodeTableFlowApplyActionsCase.class, VRF_CONVERTOR);
        registerAction13(CofActionNextHopNodesNodeTableFlowWriteActionsCase.class, NEXT_HOP_CONVERTOR);
        registerAction13(CofActionVrfNodesNodeTableFlowWriteActionsCase.class, VRF_CONVERTOR);
        registerAction13(CofActionNextHopNodesNodeGroupBucketsBucketActionsCase.class, NEXT_HOP_CONVERTOR);
        registerAction13(CofActionVrfNodesNodeGroupBucketsBucketActionsCase.class, VRF_CONVERTOR);

        // src=rpc-addFlow
        registerAction13(CofActionNextHopRpcAddFlowApplyActionsCase.class, NEXT_HOP_CONVERTOR);
        registerAction13(CofActionVrfRpcAddFlowApplyActionsCase.class, VRF_CONVERTOR);

        registerAction13(CofActionNextHopRpcAddFlowWriteActionsCase.class, NEXT_HOP_CONVERTOR);
        registerAction13(CofActionVrfRpcAddFlowWriteActionsCase.class, VRF_CONVERTOR);

        registerAction13(CofActionNextHopRpcAddGroupCase.class, NEXT_HOP_CONVERTOR);
        registerAction13(CofActionNextHopRpcRemoveGroupCase.class, NEXT_HOP_CONVERTOR);
        registerAction13(CofActionNextHopRpcUpdateGroupOriginalCase.class, NEXT_HOP_CONVERTOR);
        registerAction13(CofActionNextHopRpcUpdateGroupUpdatedCase.class, NEXT_HOP_CONVERTOR);
        registerAction13(CofActionVrfRpcAddGroupCase.class, VRF_CONVERTOR);
        registerAction13(CofActionVrfRpcRemoveGroupCase.class, VRF_CONVERTOR);
        registerAction13(CofActionVrfRpcUpdateGroupOriginalCase.class, VRF_CONVERTOR);
        registerAction13(CofActionVrfRpcUpdateGroupUpdatedCase.class, VRF_CONVERTOR);
        
        registrations.add(extensionConverterRegistrator.registerActionConvertor(CiscoUtil.createOfJavaKeyFrom(NextHopCodec.SERIALIZER_KEY), NEXT_HOP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(CiscoUtil.createOfJavaKeyFrom(VrfCodec.SERIALIZER_KEY), VRF_CONVERTOR));

        // OF-1.0
        registerAction10(CofActionNextHopNodesNodeTableFlowApplyActionsCase.class, NEXT_HOP_CONVERTOR);
        registerAction10(CofActionVrfNodesNodeTableFlowApplyActionsCase.class, VRF_CONVERTOR);
        registerAction10(CofActionNextHopNodesNodeTableFlowWriteActionsCase.class, NEXT_HOP_CONVERTOR);
        registerAction10(CofActionVrfNodesNodeTableFlowWriteActionsCase.class, VRF_CONVERTOR);
        
        registerAction10(CofActionNextHopRpcAddFlowApplyActionsCase.class, NEXT_HOP_CONVERTOR);
        registerAction10(CofActionVrfRpcAddFlowApplyActionsCase.class, VRF_CONVERTOR);
        registerAction10(CofActionNextHopRpcAddFlowWriteActionsCase.class, NEXT_HOP_CONVERTOR);
        registerAction10(CofActionVrfRpcAddFlowWriteActionsCase.class, VRF_CONVERTOR);

        registrations.add(extensionConverterRegistrator.registerActionConvertor(CiscoUtil.createOfJavaKeyFrom(NextHopCodec.SERIALIZER_KEY_10), NEXT_HOP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(CiscoUtil.createOfJavaKeyFrom(VrfCodec.SERIALIZER_KEY_10), VRF_CONVERTOR));
    }

    /**
     * @param actionCaseType
     * @param actionConvertor
     */
    private void registerAction13(
            Class<? extends Action> actionCaseType,
            ConvertorActionToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action> actionConvertor) {
        TypeVersionKey<? extends Action> key = new TypeVersionKey<>(actionCaseType, EncodeConstants.OF13_VERSION_ID);
        registrations.add(extensionConverterRegistrator.registerActionConvertor(key, actionConvertor));
    }
    
    /**
     * @param actionCaseType
     * @param actionConvertor
     */
    private void registerAction10(
            Class<? extends Action> actionCaseType,
            ConvertorActionToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action> actionConvertor) {
        TypeVersionKey<? extends Action> key = new TypeVersionKey<>(actionCaseType, EncodeConstants.OF10_VERSION_ID);
        registrations.add(extensionConverterRegistrator.registerActionConvertor(key, actionConvertor));
    }

}
