/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;
import org.opendaylight.openflowjava.nx.api.NiciraUtil;
import org.opendaylight.openflowjava.nx.codec.action.ConntrackCodec;
import org.opendaylight.openflowjava.nx.codec.action.CtClearCodec;
import org.opendaylight.openflowjava.nx.codec.action.DecNshTtlCodec;
import org.opendaylight.openflowjava.nx.codec.action.DecapCodec;
import org.opendaylight.openflowjava.nx.codec.action.EncapCodec;
import org.opendaylight.openflowjava.nx.codec.action.FinTimeoutCodec;
import org.opendaylight.openflowjava.nx.codec.action.LearnCodec;
import org.opendaylight.openflowjava.nx.codec.action.MultipathCodec;
import org.opendaylight.openflowjava.nx.codec.action.OutputReg2Codec;
import org.opendaylight.openflowjava.nx.codec.action.OutputRegCodec;
import org.opendaylight.openflowjava.nx.codec.action.RegLoad2Codec;
import org.opendaylight.openflowjava.nx.codec.action.RegLoadCodec;
import org.opendaylight.openflowjava.nx.codec.action.RegMoveCodec;
import org.opendaylight.openflowjava.nx.codec.action.ResubmitCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpOpCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpShaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpSpaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpThaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpTpaCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtMarkCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtStateCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtTpDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtTpSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtZoneCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthTypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.InPortCodec;
import org.opendaylight.openflowjava.nx.codec.match.NshFlagsCodec;
import org.opendaylight.openflowjava.nx.codec.match.NshMdtypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.NshNpCodec;
import org.opendaylight.openflowjava.nx.codec.match.NshTtlCodec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc1Codec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc2Codec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc3Codec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc4Codec;
import org.opendaylight.openflowjava.nx.codec.match.NsiCodec;
import org.opendaylight.openflowjava.nx.codec.match.NspCodec;
import org.opendaylight.openflowjava.nx.codec.match.PktMarkCodec;
import org.opendaylight.openflowjava.nx.codec.match.Reg0Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg1Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg2Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg3Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg4Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg5Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg6Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg7Codec;
import org.opendaylight.openflowjava.nx.codec.match.TcpDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.TcpSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunIdCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunIpv4DstCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunIpv4SrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.UdpDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.UdpSrcCodec;
import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.ConntrackConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.CtClearConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.DecNshTtlConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.DecapConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.EncapConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.FinTimeoutConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.LearnConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.MultipathConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.OutputReg2Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.OutputRegConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.OutputRegConvertorProxy;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.RegLoad2Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.RegLoadConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.RegLoadConvertorProxy;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.RegMoveConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.ResubmitConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpOpConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpShaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpSpaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpThaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpTpaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.CtMarkConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.CtStateConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.CtTpDstConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.CtTpSrcConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.CtZoneConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EthDstConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EthSrcConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EthTypeConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NshFlagsConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NshMdtypeConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NshNpConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NshTtlConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc1Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc2Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc3Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc4Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NsiConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NspConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NxmInPortConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.PktMarkConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.RegConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TcpDstConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TcpSrcConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunIPv4DstConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunIPv4SrcConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunIdConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.UdpDstConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.UdpSrcConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionCtClear;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionEncap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionFinTimeout;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionLearn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionConntrackRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionCtClearRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionDecNshTtlRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionDecapRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionEncapRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionFinTimeoutRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionLearnRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionMultipathRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionOutputRegRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionCtClearRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecNshTtlRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecapRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionEncapRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionFinTimeoutRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionLearnRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionMultipathRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionResubmitRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionConntrackRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionCtClearRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionDecNshTtlRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionDecapRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionEncapRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionFinTimeoutRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionLearnRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionMultipathRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionOutputRegRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionRegLoadRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionRegMoveRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionResubmitRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionConntrackNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionCtClearNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionDecNshTtlNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionDecapNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionEncapNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionFinTimeoutNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionLearnNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionMultipathNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionOutputRegNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionRegLoadNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionRegMoveNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionResubmitNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionConntrackNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionCtClearNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionDecNshTtlNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionDecapNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionEncapNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionFinTimeoutNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionLearnNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionMultipathNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionOutputRegNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionConntrackNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionCtClearNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecNshTtlNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecapNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionEncapNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionFinTimeoutNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionLearnNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionMultipathNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionResubmitNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadRpcRemoveFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveRpcRemoveFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitRpcRemoveFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionConntrackRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionCtClearRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionDecNshTtlRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionDecapRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionEncapRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionFinTimeoutRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionLearnRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionMultipathRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionOutputRegRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionRegLoadRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionRegMoveRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionResubmitRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.transmit.packet.input.action.action.NxActionRegLoadRpcTransmitPacketCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.transmit.packet.input.action.action.NxActionResubmitRpcTransmitPacketCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.flow.input.original.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadRpcUpdateFlowOriginalApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.flow.input.updated.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadRpcUpdateFlowUpdatedApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.flow.input.updated.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveRpcUpdateFlowUpdatedApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.flow.input.updated.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitRpcUpdateFlowUpdatedApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionConntrackRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionCtClearRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionDecNshTtlRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionDecapRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionEncapRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionFinTimeoutRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionLearnRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionMultipathRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionOutputRegRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionRegLoadRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionRegMoveRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionResubmitRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionConntrackRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionCtClearRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionDecNshTtlRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionDecapRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionEncapRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionFinTimeoutRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionLearnRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionMultipathRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionOutputRegRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionRegLoadRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionRegMoveRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionResubmitRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpShaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpThaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtMarkKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtStateKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtTpDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtTpSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtZoneKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshFlagsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshMdtypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshNpKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshTtlKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc1Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc2Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc3Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc4Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNsiKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNspKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxPktMarkKey;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfInPortKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfUdpDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfUdpSrcKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NiciraExtensionProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(NiciraExtensionProvider.class);

    private static final RegConvertor REG_CONVERTOR = new RegConvertor();
    private static final TunIdConvertor TUN_ID_CONVERTOR = new TunIdConvertor();
    private static final ArpOpConvertor ARP_OP_CONVERTOR = new ArpOpConvertor();
    private static final ArpShaConvertor ARP_SHA_CONVERTOR = new ArpShaConvertor();
    private static final ArpSpaConvertor ARP_SPA_CONVERTOR = new ArpSpaConvertor();
    private static final ArpTpaConvertor ARP_TPA_CONVERTOR = new ArpTpaConvertor();
    private static final ArpThaConvertor ARP_THA_CONVERTOR = new ArpThaConvertor();
    private static final NxmInPortConvertor NXM_IN_PORT_CONVERTOR = new NxmInPortConvertor();
    private static final EthDstConvertor ETH_DST_CONVERTOR = new EthDstConvertor();
    private static final EthSrcConvertor ETH_SRC_CONVERTOR = new EthSrcConvertor();
    private static final RegLoadConvertor REG_LOAD_CONVERTOR = new RegLoadConvertor();
    private static final RegLoad2Convertor REG_LOAD2_CONVERTOR = new RegLoad2Convertor();
    private static final RegLoadConvertorProxy REG_LOAD_CONVERTOR_PROXY = new RegLoadConvertorProxy();
    private static final RegMoveConvertor REG_MOVE_CONVERTOR = new RegMoveConvertor();
    private static final OutputRegConvertor OUTPUT_REG_CONVERTOR = new OutputRegConvertor();
    private static final OutputReg2Convertor OUTPUT_REG2_CONVERTOR = new OutputReg2Convertor();
    private static final OutputRegConvertorProxy OUTPUT_REG_CONVERTOR_PROXY = new OutputRegConvertorProxy();
    private static final EthTypeConvertor ETH_TYPE_CONVERTOR = new EthTypeConvertor();
    private static final ResubmitConvertor RESUBMIT_CONVERTOR = new ResubmitConvertor();
    private static final FinTimeoutConvertor FIN_TIMEOUT_CONVERTOR = new FinTimeoutConvertor();
    private static final MultipathConvertor MULTIPATH_CONVERTOR = new MultipathConvertor();
    private static final NspConvertor NSP_CONVERTOR = new NspConvertor();
    private static final NsiConvertor NSI_CONVERTOR = new NsiConvertor();
    private static final Nshc1Convertor NSC1_CONVERTOR = new Nshc1Convertor();
    private static final Nshc2Convertor NSC2_CONVERTOR = new Nshc2Convertor();
    private static final Nshc3Convertor NSC3_CONVERTOR = new Nshc3Convertor();
    private static final Nshc4Convertor NSC4_CONVERTOR = new Nshc4Convertor();
    private static final NshFlagsConvertor NSH_FLAGS_CONVERTOR = new NshFlagsConvertor();
    private static final NshTtlConvertor NSH_TTL_CONVERTOR = new NshTtlConvertor();
    private static final TunIPv4SrcConvertor TUN_IPV4_SRC_CONVERTOR = new TunIPv4SrcConvertor();
    private static final TunIPv4DstConvertor TUN_IPV4_DST_CONVERTOR = new TunIPv4DstConvertor();
    private static final NshMdtypeConvertor NSH_MDTYPE_CONVERTOR = new NshMdtypeConvertor();
    private static final NshNpConvertor NSH_NP_CONVERTOR = new NshNpConvertor();
    private static final TcpSrcConvertor TCP_SRC_CONVERTOR = new TcpSrcConvertor();
    private static final TcpDstConvertor TCP_DST_CONVERTOR = new TcpDstConvertor();
    private static final UdpSrcConvertor UDP_SRC_CONVERTOR = new UdpSrcConvertor();
    private static final UdpDstConvertor UDP_DST_CONVERTOR = new UdpDstConvertor();
    private static final ConntrackConvertor CONNTRACK_CONVERTOR = new ConntrackConvertor();
    private static final CtClearConvertor CT_CLEAR_CONVERTOR = new CtClearConvertor();
    private static final LearnConvertor LEARN_CONVERTOR = new LearnConvertor();
    private static final CtMarkConvertor CT_MARK_CONVERTOR = new CtMarkConvertor();
    private static final CtStateConvertor CT_STATE_CONVERTOR = new CtStateConvertor();
    private static final CtZoneConvertor CT_ZONE_CONVERTOR = new CtZoneConvertor();
    private static final CtTpSrcConvertor CT_TP_SRC_CONVERTOR = new CtTpSrcConvertor();
    private static final CtTpDstConvertor CT_TP_DST_CONVERTOR = new CtTpDstConvertor();
    private static final PktMarkConvertor PKT_MARK_CONVERTOR = new PktMarkConvertor();
    private static final EncapConvertor ENCAP_CONVERTOR = new EncapConvertor();
    private static final DecapConvertor DECAP_CONVERTOR = new DecapConvertor();
    private static final DecNshTtlConvertor DEC_NSH_TTL_CONVERTOR = new DecNshTtlConvertor();

    private final ExtensionConverterRegistrator extensionConverterRegistrator;
    private final Set<ObjectRegistration<?>> registrations;

    /**
     * Register appropriate converters.
     */
    public NiciraExtensionProvider(final OpenFlowPluginExtensionRegistratorProvider provider) {
        this.extensionConverterRegistrator = requireNonNull(provider.getExtensionConverterRegistrator());
        registrations = new HashSet<>();
        // src=dataStore/config
        registerAction13(NxActionRegLoadNodesNodeTableFlowApplyActionsCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegMoveNodesNodeTableFlowApplyActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegNodesNodeTableFlowApplyActionsCase.class, OUTPUT_REG_CONVERTOR_PROXY);
        registerAction13(NxActionResubmitNodesNodeTableFlowApplyActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionFinTimeoutNodesNodeTableFlowApplyActionsCase.class, FIN_TIMEOUT_CONVERTOR);
        registerAction13(NxActionMultipathNodesNodeTableFlowApplyActionsCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionConntrackNodesNodeTableFlowApplyActionsCase.class, CONNTRACK_CONVERTOR);
        registerAction13(NxActionCtClearNodesNodeTableFlowApplyActionsCase.class, CT_CLEAR_CONVERTOR);
        registerAction13(NxActionLearnNodesNodeTableFlowApplyActionsCase.class, LEARN_CONVERTOR);
        registerAction13(NxActionEncapNodesNodeTableFlowApplyActionsCase.class, ENCAP_CONVERTOR);
        registerAction13(NxActionDecapNodesNodeTableFlowApplyActionsCase.class, DECAP_CONVERTOR);
        registerAction13(NxActionDecNshTtlNodesNodeTableFlowApplyActionsCase.class, DEC_NSH_TTL_CONVERTOR);

        registerAction13(NxActionRegLoadNodesNodeTableFlowWriteActionsCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegMoveNodesNodeTableFlowWriteActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegNodesNodeTableFlowWriteActionsCase.class, OUTPUT_REG_CONVERTOR_PROXY);
        registerAction13(NxActionResubmitNodesNodeTableFlowWriteActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionFinTimeoutNodesNodeTableFlowWriteActionsCase.class, FIN_TIMEOUT_CONVERTOR);
        registerAction13(NxActionMultipathNodesNodeTableFlowWriteActionsCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionConntrackNodesNodeTableFlowWriteActionsCase.class, CONNTRACK_CONVERTOR);
        registerAction13(NxActionCtClearNodesNodeTableFlowWriteActionsCase.class, CT_CLEAR_CONVERTOR);
        registerAction13(NxActionLearnNodesNodeTableFlowWriteActionsCase.class, LEARN_CONVERTOR);
        registerAction13(NxActionEncapNodesNodeTableFlowWriteActionsCase.class, ENCAP_CONVERTOR);
        registerAction13(NxActionDecapNodesNodeTableFlowWriteActionsCase.class, DECAP_CONVERTOR);
        registerAction13(NxActionDecNshTtlNodesNodeTableFlowWriteActionsCase.class, DEC_NSH_TTL_CONVERTOR);

        registerAction13(NxActionRegLoadNodesNodeGroupBucketsBucketActionsCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegMoveNodesNodeGroupBucketsBucketActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegNodesNodeGroupBucketsBucketActionsCase.class, OUTPUT_REG_CONVERTOR_PROXY);
        registerAction13(NxActionResubmitNodesNodeGroupBucketsBucketActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionFinTimeoutNodesNodeGroupBucketsBucketActionsCase.class, FIN_TIMEOUT_CONVERTOR);
        registerAction13(NxActionMultipathNodesNodeGroupBucketsBucketActionsCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionConntrackNodesNodeGroupBucketsBucketActionsCase.class, CONNTRACK_CONVERTOR);
        registerAction13(NxActionCtClearNodesNodeGroupBucketsBucketActionsCase.class, CT_CLEAR_CONVERTOR);
        registerAction13(NxActionLearnNodesNodeGroupBucketsBucketActionsCase.class, LEARN_CONVERTOR);
        registerAction13(NxActionEncapNodesNodeGroupBucketsBucketActionsCase.class, ENCAP_CONVERTOR);
        registerAction13(NxActionDecapNodesNodeGroupBucketsBucketActionsCase.class, DECAP_CONVERTOR);
        registerAction13(NxActionDecNshTtlNodesNodeGroupBucketsBucketActionsCase.class, DEC_NSH_TTL_CONVERTOR);

        // src=rpc-addFlow
        registerAction13(NxActionRegLoadRpcAddFlowApplyActionsCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegMoveRpcAddFlowApplyActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegRpcAddFlowApplyActionsCase.class, OUTPUT_REG_CONVERTOR_PROXY);
        registerAction13(NxActionResubmitRpcAddFlowApplyActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionFinTimeoutRpcAddFlowApplyActionsCase.class, FIN_TIMEOUT_CONVERTOR);
        registerAction13(NxActionMultipathRpcAddFlowApplyActionsCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionConntrackRpcAddFlowApplyActionsCase.class, CONNTRACK_CONVERTOR);
        registerAction13(NxActionCtClearRpcAddFlowApplyActionsCase.class, CT_CLEAR_CONVERTOR);
        registerAction13(NxActionLearnRpcAddFlowApplyActionsCase.class, LEARN_CONVERTOR);
        registerAction13(NxActionEncapRpcAddFlowApplyActionsCase.class, ENCAP_CONVERTOR);
        registerAction13(NxActionDecapRpcAddFlowApplyActionsCase.class, DECAP_CONVERTOR);
        registerAction13(NxActionDecNshTtlRpcAddFlowApplyActionsCase.class, DEC_NSH_TTL_CONVERTOR);

        registerAction13(NxActionRegLoadRpcAddFlowWriteActionsCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegMoveRpcAddFlowWriteActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegRpcAddFlowWriteActionsCase.class, OUTPUT_REG_CONVERTOR_PROXY);
        registerAction13(NxActionResubmitRpcAddFlowWriteActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionFinTimeoutRpcAddFlowWriteActionsCase.class, FIN_TIMEOUT_CONVERTOR);
        registerAction13(NxActionMultipathRpcAddFlowWriteActionsCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionConntrackRpcAddFlowWriteActionsCase.class, CONNTRACK_CONVERTOR);
        registerAction13(NxActionCtClearRpcAddFlowWriteActionsCase.class, CT_CLEAR_CONVERTOR);
        registerAction13(NxActionLearnRpcAddFlowWriteActionsCase.class, LEARN_CONVERTOR);
        registerAction13(NxActionEncapRpcAddFlowWriteActionsCase.class, ENCAP_CONVERTOR);
        registerAction13(NxActionDecapRpcAddFlowWriteActionsCase.class, DECAP_CONVERTOR);
        registerAction13(NxActionDecNshTtlRpcAddFlowWriteActionsCase.class, DEC_NSH_TTL_CONVERTOR);

        registerAction13(NxActionRegLoadRpcAddGroupCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegLoadRpcRemoveGroupCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegLoadRpcUpdateGroupOriginalCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegLoadRpcUpdateGroupUpdatedCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegMoveRpcAddGroupCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionRegMoveRpcRemoveGroupCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionRegMoveRpcUpdateGroupOriginalCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionRegMoveRpcUpdateGroupUpdatedCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegRpcAddGroupCase.class, OUTPUT_REG_CONVERTOR_PROXY);
        registerAction13(NxActionOutputRegRpcRemoveGroupCase.class, OUTPUT_REG_CONVERTOR_PROXY);
        registerAction13(NxActionOutputRegRpcUpdateGroupOriginalCase.class, OUTPUT_REG_CONVERTOR_PROXY);
        registerAction13(NxActionOutputRegRpcUpdateGroupUpdatedCase.class, OUTPUT_REG_CONVERTOR_PROXY);
        registerAction13(NxActionResubmitRpcAddGroupCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionResubmitRpcRemoveGroupCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionResubmitRpcUpdateGroupOriginalCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionResubmitRpcUpdateGroupUpdatedCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionFinTimeoutRpcAddGroupCase.class, FIN_TIMEOUT_CONVERTOR);
        registerAction13(NxActionFinTimeoutRpcRemoveGroupCase.class, FIN_TIMEOUT_CONVERTOR);
        registerAction13(NxActionFinTimeoutRpcUpdateGroupOriginalCase.class, FIN_TIMEOUT_CONVERTOR);
        registerAction13(NxActionFinTimeoutRpcUpdateGroupUpdatedCase.class, FIN_TIMEOUT_CONVERTOR);
        registerAction13(NxActionMultipathRpcAddGroupCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionMultipathRpcRemoveGroupCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionMultipathRpcUpdateGroupOriginalCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionMultipathRpcUpdateGroupUpdatedCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionConntrackRpcAddGroupCase.class, CONNTRACK_CONVERTOR);
        registerAction13(NxActionConntrackRpcRemoveGroupCase.class, CONNTRACK_CONVERTOR);
        registerAction13(NxActionConntrackRpcUpdateGroupOriginalCase.class, CONNTRACK_CONVERTOR);
        registerAction13(NxActionConntrackRpcUpdateGroupUpdatedCase.class, CONNTRACK_CONVERTOR);
        registerAction13(NxActionCtClearRpcAddGroupCase.class, CT_CLEAR_CONVERTOR);
        registerAction13(NxActionCtClearRpcRemoveGroupCase.class, CT_CLEAR_CONVERTOR);
        registerAction13(NxActionCtClearRpcUpdateGroupOriginalCase.class, CT_CLEAR_CONVERTOR);
        registerAction13(NxActionCtClearRpcUpdateGroupUpdatedCase.class, CT_CLEAR_CONVERTOR);
        registerAction13(NxActionLearnRpcAddGroupCase.class, LEARN_CONVERTOR);
        registerAction13(NxActionLearnRpcRemoveGroupCase.class, LEARN_CONVERTOR);
        registerAction13(NxActionLearnRpcUpdateGroupOriginalCase.class, LEARN_CONVERTOR);
        registerAction13(NxActionLearnRpcUpdateGroupUpdatedCase.class, LEARN_CONVERTOR);
        registerAction13(NxActionEncapRpcAddGroupCase.class, ENCAP_CONVERTOR);
        registerAction13(NxActionEncapRpcRemoveGroupCase.class, ENCAP_CONVERTOR);
        registerAction13(NxActionEncapRpcUpdateGroupOriginalCase.class, ENCAP_CONVERTOR);
        registerAction13(NxActionEncapRpcUpdateGroupUpdatedCase.class, ENCAP_CONVERTOR);
        registerAction13(NxActionDecapRpcAddGroupCase.class, DECAP_CONVERTOR);
        registerAction13(NxActionDecapRpcRemoveGroupCase.class, DECAP_CONVERTOR);
        registerAction13(NxActionDecapRpcUpdateGroupOriginalCase.class, DECAP_CONVERTOR);
        registerAction13(NxActionDecapRpcUpdateGroupUpdatedCase.class, DECAP_CONVERTOR);
        registerAction13(NxActionDecNshTtlRpcAddGroupCase.class, DEC_NSH_TTL_CONVERTOR);
        registerAction13(NxActionDecNshTtlRpcRemoveGroupCase.class, DEC_NSH_TTL_CONVERTOR);
        registerAction13(NxActionDecNshTtlRpcUpdateGroupOriginalCase.class, DEC_NSH_TTL_CONVERTOR);
        registerAction13(NxActionDecNshTtlRpcUpdateGroupUpdatedCase.class, DEC_NSH_TTL_CONVERTOR);

        registerAction13(NxActionRegLoadRpcTransmitPacketCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionResubmitRpcTransmitPacketCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionRegLoadRpcUpdateFlowOriginalApplyActionsCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegLoadRpcUpdateFlowUpdatedApplyActionsCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionResubmitRpcUpdateFlowUpdatedApplyActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionRegMoveRpcUpdateFlowUpdatedApplyActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionRegLoadRpcRemoveFlowApplyActionsCase.class, REG_LOAD_CONVERTOR_PROXY);
        registerAction13(NxActionRegMoveRpcRemoveFlowApplyActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionResubmitRpcRemoveFlowApplyActionsCase.class, RESUBMIT_CONVERTOR);

        registerAction13(ActionRegLoad.class, REG_LOAD_CONVERTOR);
        registerAction13(ActionRegLoad2.class, REG_LOAD2_CONVERTOR);
        registerAction13(ActionRegMove.class, REG_MOVE_CONVERTOR);
        registerAction13(ActionOutputReg.class, OUTPUT_REG_CONVERTOR);
        registerAction13(ActionOutputReg2.class, OUTPUT_REG2_CONVERTOR);
        registerAction13(ActionResubmit.class, RESUBMIT_CONVERTOR);
        registerAction13(ActionFinTimeout.class, FIN_TIMEOUT_CONVERTOR);
        registerAction13(ActionMultipath.class, MULTIPATH_CONVERTOR);
        registerAction13(ActionConntrack.class, CONNTRACK_CONVERTOR);
        registerAction13(ActionCtClear.class, CT_CLEAR_CONVERTOR);
        registerAction13(ActionLearn.class, LEARN_CONVERTOR);
        registerAction13(ActionEncap.class, ENCAP_CONVERTOR);
        registerAction13(ActionDecap.class, DECAP_CONVERTOR);
        registerAction13(ActionDecNshTtl.class, DEC_NSH_TTL_CONVERTOR);

        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(RegLoadCodec.SERIALIZER_KEY), REG_LOAD_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(RegLoad2Codec.SERIALIZER_KEY), REG_LOAD2_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(RegMoveCodec.SERIALIZER_KEY), REG_MOVE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(OutputRegCodec.SERIALIZER_KEY), OUTPUT_REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(OutputReg2Codec.SERIALIZER_KEY), OUTPUT_REG2_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(ResubmitCodec.SERIALIZER_KEY), RESUBMIT_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(FinTimeoutCodec.SERIALIZER_KEY), FIN_TIMEOUT_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(MultipathCodec.SERIALIZER_KEY), MULTIPATH_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(ConntrackCodec.SERIALIZER_KEY), CONNTRACK_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(CtClearCodec.SERIALIZER_KEY), CT_CLEAR_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(LearnCodec.SERIALIZER_KEY), LEARN_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(EncapCodec.SERIALIZER_KEY), ENCAP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(DecapCodec.SERIALIZER_KEY), DECAP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(LearnCodec.SERIALIZER_KEY), LEARN_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                NiciraUtil.createOfJavaKeyFrom(DecNshTtlCodec.SERIALIZER_KEY), DEC_NSH_TTL_CONVERTOR));

        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxReg0Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxReg1Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxReg2Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxReg3Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxReg4Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxReg5Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxReg6Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxReg7Key.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));

        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Reg0Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Reg1Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Reg2Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Reg3Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Reg4Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Reg5Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Reg6Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Reg7Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxTunIdKey.class, EncodeConstants.OF13_VERSION_ID), TUN_ID_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(TunIdCodec.SERIALIZER_KEY, TUN_ID_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxArpShaKey.class, EncodeConstants.OF13_VERSION_ID), ARP_SHA_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(ArpShaCodec.SERIALIZER_KEY, ARP_SHA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxArpThaKey.class, EncodeConstants.OF13_VERSION_ID), ARP_THA_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(ArpThaCodec.SERIALIZER_KEY, ARP_THA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfArpOpKey.class, EncodeConstants.OF13_VERSION_ID), ARP_OP_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(ArpOpCodec.SERIALIZER_KEY, ARP_OP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfArpSpaKey.class, EncodeConstants.OF13_VERSION_ID), ARP_SPA_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(ArpSpaCodec.SERIALIZER_KEY, ARP_SPA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfArpTpaKey.class, EncodeConstants.OF13_VERSION_ID), ARP_TPA_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(ArpTpaCodec.SERIALIZER_KEY, ARP_TPA_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfInPortKey.class, EncodeConstants.OF13_VERSION_ID),
                NXM_IN_PORT_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(InPortCodec.SERIALIZER_KEY,
                NXM_IN_PORT_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfEthSrcKey.class, EncodeConstants.OF13_VERSION_ID), ETH_SRC_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(EthSrcCodec.SERIALIZER_KEY, ETH_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfEthDstKey.class, EncodeConstants.OF13_VERSION_ID), ETH_DST_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(EthDstCodec.SERIALIZER_KEY, ETH_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfEthTypeKey.class, EncodeConstants.OF13_VERSION_ID),
                ETH_TYPE_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(EthTypeCodec.SERIALIZER_KEY, ETH_TYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxNspKey.class, EncodeConstants.OF13_VERSION_ID), NSP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(NspCodec.SERIALIZER_KEY, NSP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxNsiKey.class, EncodeConstants.OF13_VERSION_ID), NSI_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(NsiCodec.SERIALIZER_KEY, NSI_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxNshc1Key.class, EncodeConstants.OF13_VERSION_ID), NSC1_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Nshc1Codec.SERIALIZER_KEY, NSC1_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxNshc2Key.class, EncodeConstants.OF13_VERSION_ID), NSC2_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Nshc2Codec.SERIALIZER_KEY, NSC2_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxNshc3Key.class, EncodeConstants.OF13_VERSION_ID), NSC3_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Nshc3Codec.SERIALIZER_KEY, NSC3_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxNshc4Key.class, EncodeConstants.OF13_VERSION_ID), NSC4_CONVERTOR));
        registrations
                .add(extensionConverterRegistrator.registerMatchConvertor(Nshc4Codec.SERIALIZER_KEY, NSC4_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxNshFlagsKey.class, EncodeConstants.OF13_VERSION_ID),
                NSH_FLAGS_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(NshFlagsCodec.SERIALIZER_KEY,
                NSH_FLAGS_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxNshTtlKey.class, EncodeConstants.OF13_VERSION_ID),
                NSH_TTL_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(NshTtlCodec.SERIALIZER_KEY,
                NSH_TTL_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxTunIpv4SrcKey.class, EncodeConstants.OF13_VERSION_ID),
                TUN_IPV4_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(TunIpv4SrcCodec.SERIALIZER_KEY,
                TUN_IPV4_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxTunIpv4DstKey.class, EncodeConstants.OF13_VERSION_ID),
                TUN_IPV4_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(TunIpv4DstCodec.SERIALIZER_KEY,
                TUN_IPV4_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxNshMdtypeKey.class, EncodeConstants.OF13_VERSION_ID),
                NSH_MDTYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(NshMdtypeCodec.SERIALIZER_KEY,
                NSH_MDTYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxNshNpKey.class, EncodeConstants.OF13_VERSION_ID), NSH_NP_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(NshNpCodec.SERIALIZER_KEY, NSH_NP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfTcpSrcKey.class, EncodeConstants.OF13_VERSION_ID), TCP_SRC_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(TcpSrcCodec.SERIALIZER_KEY, TCP_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfTcpDstKey.class, EncodeConstants.OF13_VERSION_ID), TCP_DST_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(TcpDstCodec.SERIALIZER_KEY, TCP_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfUdpSrcKey.class, EncodeConstants.OF13_VERSION_ID), UDP_SRC_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(UdpSrcCodec.SERIALIZER_KEY, UDP_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmOfUdpDstKey.class, EncodeConstants.OF13_VERSION_ID), UDP_DST_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(UdpDstCodec.SERIALIZER_KEY, UDP_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxCtStateKey.class, EncodeConstants.OF13_VERSION_ID),
                CT_STATE_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(CtStateCodec.SERIALIZER_KEY, CT_STATE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxCtZoneKey.class, EncodeConstants.OF13_VERSION_ID), CT_ZONE_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(CtZoneCodec.SERIALIZER_KEY, CT_ZONE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxCtMarkKey.class, EncodeConstants.OF13_VERSION_ID), CT_MARK_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(CtMarkCodec.SERIALIZER_KEY, CT_MARK_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxCtTpSrcKey.class, EncodeConstants.OF13_VERSION_ID),
                CT_TP_SRC_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(CtTpSrcCodec.SERIALIZER_KEY, CT_TP_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxCtTpDstKey.class, EncodeConstants.OF13_VERSION_ID),
                CT_TP_DST_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(CtTpDstCodec.SERIALIZER_KEY, CT_TP_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(NxmNxPktMarkKey.class, EncodeConstants.OF13_VERSION_ID),
                PKT_MARK_CONVERTOR));
        registrations.add(
                extensionConverterRegistrator.registerMatchConvertor(PktMarkCodec.SERIALIZER_KEY, PKT_MARK_CONVERTOR));
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void close() {
        registrations.forEach(janitor -> {
            try {
                janitor.close();
            } catch (RuntimeException e) {
                LOG.warn("closing of extension converter failed", e);
            }
        });

        registrations.clear();
    }

    private void registerAction13(final Class<? extends Action> actionCaseType, final ConvertorActionToOFJava<
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action>
                actionConvertor) {
        TypeVersionKey<? extends Action> key = new TypeVersionKey<>(actionCaseType, EncodeConstants.OF13_VERSION_ID);
        registrations.add(extensionConverterRegistrator.registerActionConvertor(key, actionConvertor));
    }

    private void registerAction13(final Class<? extends ActionChoice> actionCaseType,
        final ConvertorActionFromOFJava<
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action,
                ActionPath> actionConvertor) {
        ActionSerializerKey<?> key = new ActionSerializerKey<>(EncodeConstants.OF13_VERSION_ID, actionCaseType,
                (Uint32) null);
        registrations.add(extensionConverterRegistrator.registerActionConvertor(key, actionConvertor));
    }
}
