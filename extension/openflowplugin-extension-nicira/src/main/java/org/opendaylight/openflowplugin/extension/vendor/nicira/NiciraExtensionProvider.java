/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.openflowjava.nx.api.NiciraUtil;
import org.opendaylight.openflowjava.nx.codec.action.OutputRegCodec;
import org.opendaylight.openflowjava.nx.codec.action.RegLoadCodec;
import org.opendaylight.openflowjava.nx.codec.action.RegMoveCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpOpCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpShaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpSpaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpThaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpTpaCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthTypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.Reg0Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg1Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg2Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg3Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg4Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg5Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg6Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg7Codec;
import org.opendaylight.openflowjava.nx.codec.match.TunIdCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunIpv4DstCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunIpv4SrcCodec;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.OutputRegConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.RegLoadConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.RegMoveConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpOpConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpShaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpSpaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpThaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpTpaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EthDstConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EthSrcConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EthTypeConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.RegConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunIdConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunIpv4DstConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunIpv4SrcConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionOutputRegRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionOutputRegRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionRegLoadRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionRegMoveRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionOutputRegNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionRegLoadNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionRegMoveNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionOutputRegNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionOutputRegRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionRegLoadRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionRegMoveRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionOutputRegRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionRegLoadRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionRegMoveRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionOutputRegRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionRegLoadRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionRegMoveRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpShaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpThaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg0Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg1Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg2Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg3Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg4Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg5Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg6Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg7Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIdKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4DstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4SrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpOpKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpSpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpTpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthTypeKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * 
 */
public class NiciraExtensionProvider implements AutoCloseable {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(NiciraExtensionProvider.class);

    private ExtensionConverterRegistrator extensionConverterRegistrator;
    private Set<ObjectRegistration<?>> registrations;

    private final static RegConvertor REG_CONVERTOR = new RegConvertor();
    private final static TunIdConvertor TUN_ID_CONVERTOR = new TunIdConvertor();
    private final static ArpOpConvertor ARP_OP_CONVERTOR = new ArpOpConvertor();
    private final static ArpShaConvertor ARP_SHA_CONVERTOR = new ArpShaConvertor();
    private final static ArpSpaConvertor ARP_SPA_CONVERTOR = new ArpSpaConvertor();
    private final static ArpTpaConvertor ARP_TPA_CONVERTOR = new ArpTpaConvertor();
    private final static ArpThaConvertor ARP_THA_CONVERTOR = new ArpThaConvertor();
    private final static EthDstConvertor ETH_DST_CONVERTOR = new EthDstConvertor();
    private final static EthSrcConvertor ETH_SRC_CONVERTOR = new EthSrcConvertor();
    private final static TunIpv4DstConvertor TUN_IPV4_DST_CONVERTOR = new TunIpv4DstConvertor();
    private final static TunIpv4SrcConvertor TUN_IPV4_SRC_CONVERTOR = new TunIpv4SrcConvertor();
    private final static RegLoadConvertor REG_LOAD_CONVERTOR = new RegLoadConvertor();
    private final static RegMoveConvertor REG_MOVE_CONVERTOR = new RegMoveConvertor();
    private final static OutputRegConvertor OUTPUT_REG_CONVERTOR = new OutputRegConvertor();
    private final static EthTypeConvertor ETH_TYPE_CONVERTOR = new EthTypeConvertor();

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
        registerAction13(NxActionRegLoadNodesNodeTableFlowApplyActionsCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegMoveNodesNodeTableFlowApplyActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegNodesNodeTableFlowApplyActionsCase.class, OUTPUT_REG_CONVERTOR);
        registerAction13(NxActionRegLoadNodesNodeTableFlowWriteActionsCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegMoveNodesNodeTableFlowWriteActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegNodesNodeTableFlowWriteActionsCase.class, OUTPUT_REG_CONVERTOR);
        registerAction13(NxActionRegLoadNodesNodeGroupBucketsBucketActionsCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegMoveNodesNodeGroupBucketsBucketActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegNodesNodeGroupBucketsBucketActionsCase.class, OUTPUT_REG_CONVERTOR);

        // src=rpc-addFlow
        registerAction13(NxActionRegLoadRpcAddFlowApplyActionsCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegMoveRpcAddFlowApplyActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegRpcAddFlowApplyActionsCase.class, OUTPUT_REG_CONVERTOR);

        registerAction13(NxActionRegLoadRpcAddFlowWriteActionsCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegMoveRpcAddFlowWriteActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegRpcAddFlowWriteActionsCase.class, OUTPUT_REG_CONVERTOR);

        registerAction13(NxActionRegLoadRpcAddGroupCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegLoadRpcRemoveGroupCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegLoadRpcUpdateGroupOriginalCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegLoadRpcUpdateGroupUpdatedCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegMoveRpcAddGroupCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionRegMoveRpcRemoveGroupCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionRegMoveRpcUpdateGroupOriginalCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionRegMoveRpcUpdateGroupUpdatedCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegRpcAddGroupCase.class, OUTPUT_REG_CONVERTOR);
        registerAction13(NxActionOutputRegRpcRemoveGroupCase.class, OUTPUT_REG_CONVERTOR);
        registerAction13(NxActionOutputRegRpcUpdateGroupOriginalCase.class, OUTPUT_REG_CONVERTOR);
        registerAction13(NxActionOutputRegRpcUpdateGroupUpdatedCase.class, OUTPUT_REG_CONVERTOR);

        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(RegLoadCodec.SERIALIZER_KEY), REG_LOAD_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(RegMoveCodec.SERIALIZER_KEY), REG_MOVE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(OutputRegCodec.SERIALIZER_KEY), OUTPUT_REG_CONVERTOR));

        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxReg0Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxReg1Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxReg2Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxReg3Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxReg4Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxReg5Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxReg6Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxReg7Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));

        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg0Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg1Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg2Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg3Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg4Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg5Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg6Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg7Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxTunIdKey.class, EncodeConstants.OF13_VERSION_ID), TUN_ID_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(TunIdCodec.SERIALIZER_KEY, TUN_ID_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxArpShaKey.class, EncodeConstants.OF13_VERSION_ID), ARP_SHA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(ArpShaCodec.SERIALIZER_KEY, ARP_SHA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxArpThaKey.class, EncodeConstants.OF13_VERSION_ID), ARP_THA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(ArpThaCodec.SERIALIZER_KEY, ARP_THA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmOfArpOpKey.class, EncodeConstants.OF13_VERSION_ID), ARP_OP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(ArpOpCodec.SERIALIZER_KEY, ARP_OP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmOfArpSpaKey.class, EncodeConstants.OF13_VERSION_ID), ARP_SPA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(ArpSpaCodec.SERIALIZER_KEY, ARP_SPA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmOfArpTpaKey.class, EncodeConstants.OF13_VERSION_ID), ARP_TPA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(ArpTpaCodec.SERIALIZER_KEY, ARP_TPA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxTunIpv4DstKey.class, EncodeConstants.OF13_VERSION_ID), TUN_IPV4_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(TunIpv4DstCodec.SERIALIZER_KEY, TUN_IPV4_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxTunIpv4SrcKey.class, EncodeConstants.OF13_VERSION_ID), TUN_IPV4_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(TunIpv4SrcCodec.SERIALIZER_KEY, TUN_IPV4_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmOfEthSrcKey.class, EncodeConstants.OF13_VERSION_ID), ETH_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(EthSrcCodec.SERIALIZER_KEY, ETH_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmOfEthDstKey.class, EncodeConstants.OF13_VERSION_ID), ETH_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(EthDstCodec.SERIALIZER_KEY, ETH_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmOfEthTypeKey.class, EncodeConstants.OF13_VERSION_ID), ETH_TYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(EthTypeCodec.SERIALIZER_KEY, ETH_TYPE_CONVERTOR));
    }

    /**
     * @param class1
     * @param regLoadConvertor
     */
    private void registerAction13(
            Class<? extends Action> actionCaseType,
            ConvertorActionToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action> actionConvertor) {
        TypeVersionKey<? extends Action> key = new TypeVersionKey<>(actionCaseType, EncodeConstants.OF13_VERSION_ID);
        registrations.add(extensionConverterRegistrator.registerActionConvertor(key, actionConvertor));
    }

}
