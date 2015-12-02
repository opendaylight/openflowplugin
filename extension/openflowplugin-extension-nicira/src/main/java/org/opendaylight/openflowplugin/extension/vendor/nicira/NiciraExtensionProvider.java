/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira;

import org.opendaylight.openflowjava.nx.codec.match.TunIpv4DstCodec;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4DstKey;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunIPv4DstConvertor;
import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.openflowjava.nx.api.NiciraUtil;
import org.opendaylight.openflowjava.nx.codec.action.MultipathCodec;
import org.opendaylight.openflowjava.nx.codec.action.OutputRegCodec;
import org.opendaylight.openflowjava.nx.codec.action.RegLoadCodec;
import org.opendaylight.openflowjava.nx.codec.action.RegMoveCodec;
import org.opendaylight.openflowjava.nx.codec.action.ResubmitCodec;
import org.opendaylight.openflowjava.nx.codec.action.PushNshCodec;
import org.opendaylight.openflowjava.nx.codec.action.PopNshCodec;
import org.opendaylight.openflowjava.nx.codec.action.PushEthCodec;
import org.opendaylight.openflowjava.nx.codec.action.PopEthCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpOpCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpShaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpSpaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpThaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpTpaCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthTypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc1Codec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc2Codec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc3Codec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc4Codec;
import org.opendaylight.openflowjava.nx.codec.match.NsiCodec;
import org.opendaylight.openflowjava.nx.codec.match.NspCodec;
import org.opendaylight.openflowjava.nx.codec.match.Reg0Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg1Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg2Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg3Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg4Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg5Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg6Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg7Codec;
import org.opendaylight.openflowjava.nx.codec.match.TunIdCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunIpv4SrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.EncapEthTypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.EncapEthSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.EncapEthDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.NshMdtypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.NshNpCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunGpeNpCodec;
import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.MultipathConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.OutputRegConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.RegLoadConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.RegMoveConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.ResubmitConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.PushNshConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.PopNshConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.PushEthConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.PopEthConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpOpConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpShaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpSpaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpThaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.ArpTpaConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EthDstConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EthSrcConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EthTypeConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc1Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc2Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc3Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc4Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NsiConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NspConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.RegConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunIPv4SrcConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunIdConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EncapEthTypeConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EncapEthSrcConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.EncapEthDstConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NshMdtypeConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NshNpConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunGpeNpConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPushNsh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPopNsh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPushEth;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPopEth;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionMultipathRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionOutputRegRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionPushNshRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionPopNshRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionPushEthRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionPopEthRpcAddFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionMultipathRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionResubmitRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionPushNshRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionPopNshRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionPushEthRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.flow.input.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionPopEthRpcAddFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionMultipathRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionOutputRegRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionRegLoadRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionRegMoveRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionResubmitRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionPushNshRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionPopNshRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionPushEthRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.add.group.input.buckets.bucket.action.action.NxActionPopEthRpcAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionMultipathNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionOutputRegNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionRegLoadNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionRegMoveNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionResubmitNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionPushNshNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionPopNshNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionPushEthNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionPopEthNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionMultipathNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionOutputRegNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegMoveNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionResubmitNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionPushNshNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionPopNshNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionPushEthNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionPopEthNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionMultipathNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionOutputRegNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegMoveNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionResubmitNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionPushNshNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionPopNshNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionPushEthNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionPopEthNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionMultipathRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionOutputRegRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionRegLoadRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionRegMoveRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionResubmitRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionPushNshRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionPopNshRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionPushEthRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.remove.group.input.buckets.bucket.action.action.NxActionPopEthRpcRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionMultipathRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionOutputRegRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionRegLoadRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionRegMoveRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionResubmitRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionPushNshRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionPopNshRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionPushEthRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.original.group.buckets.bucket.action.action.NxActionPopEthRpcUpdateGroupOriginalCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionMultipathRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionOutputRegRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionRegLoadRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionRegMoveRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionResubmitRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionPushNshRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionPopNshRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionPushEthRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.update.group.input.updated.group.buckets.bucket.action.action.NxActionPopEthRpcUpdateGroupUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpShaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpThaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc1Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc2Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc3Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc4Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNsiKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNspKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg0Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg1Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg2Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg3Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg4Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg5Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg6Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg7Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIdKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4SrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxEncapEthTypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxEncapEthSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxEncapEthDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshMdtypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshNpKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunGpeNpKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpOpKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpSpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpTpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthTypeKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static RegLoadConvertor REG_LOAD_CONVERTOR = new RegLoadConvertor();
    private final static RegMoveConvertor REG_MOVE_CONVERTOR = new RegMoveConvertor();
    private final static OutputRegConvertor OUTPUT_REG_CONVERTOR = new OutputRegConvertor();
    private final static EthTypeConvertor ETH_TYPE_CONVERTOR = new EthTypeConvertor();
    private final static ResubmitConvertor RESUBMIT_CONVERTOR = new ResubmitConvertor();
    private final static MultipathConvertor MULTIPATH_CONVERTOR = new MultipathConvertor();
    private final static PushNshConvertor PUSH_NSH_CONVERTOR = new PushNshConvertor();
    private final static PopNshConvertor POP_NSH_CONVERTOR = new PopNshConvertor();
    private final static PushEthConvertor PUSH_ETH_CONVERTOR = new PushEthConvertor();
    private final static PopEthConvertor POP_ETH_CONVERTOR = new PopEthConvertor();
    private final static NspConvertor NSP_CONVERTOR = new NspConvertor();
    private final static NsiConvertor NSI_CONVERTOR = new NsiConvertor();
    private final static Nshc1Convertor NSC1_CONVERTOR = new Nshc1Convertor();
    private final static Nshc2Convertor NSC2_CONVERTOR = new Nshc2Convertor();
    private final static Nshc3Convertor NSC3_CONVERTOR = new Nshc3Convertor();
    private final static Nshc4Convertor NSC4_CONVERTOR = new Nshc4Convertor();
    private final static TunIPv4SrcConvertor TUN_IPV4_SRC_CONVERTOR = new TunIPv4SrcConvertor();
    private final static TunIPv4DstConvertor TUN_IPV4_DST_CONVERTOR = new TunIPv4DstConvertor();
    private final static EncapEthTypeConvertor ENCAP_ETH_TYPE_CONVERTOR = new EncapEthTypeConvertor();
    private final static EncapEthSrcConvertor ENCAP_ETH_SRC_CONVERTOR = new EncapEthSrcConvertor();
    private final static EncapEthDstConvertor ENCAP_ETH_DST_CONVERTOR = new EncapEthDstConvertor();
    private final static NshMdtypeConvertor NSH_MDTYPE_CONVERTOR = new NshMdtypeConvertor();
    private final static NshNpConvertor NSH_NP_CONVERTOR = new NshNpConvertor();
    private final static TunGpeNpConvertor TUN_GPE_NP_CONVERTOR = new TunGpeNpConvertor();

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
            final ExtensionConverterRegistrator extensionConverterRegistrator) {
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
        registerAction13(NxActionResubmitNodesNodeTableFlowApplyActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionMultipathNodesNodeTableFlowApplyActionsCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionPushNshNodesNodeTableFlowApplyActionsCase.class, PUSH_NSH_CONVERTOR);
        registerAction13(NxActionPopNshNodesNodeTableFlowApplyActionsCase.class, POP_NSH_CONVERTOR);
        registerAction13(NxActionPushEthNodesNodeTableFlowApplyActionsCase.class, PUSH_ETH_CONVERTOR);
        registerAction13(NxActionPopEthNodesNodeTableFlowApplyActionsCase.class, POP_ETH_CONVERTOR);

        registerAction13(NxActionRegLoadNodesNodeTableFlowWriteActionsCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegMoveNodesNodeTableFlowWriteActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegNodesNodeTableFlowWriteActionsCase.class, OUTPUT_REG_CONVERTOR);
        registerAction13(NxActionResubmitNodesNodeTableFlowWriteActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionMultipathNodesNodeTableFlowWriteActionsCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionPushNshNodesNodeTableFlowWriteActionsCase.class, PUSH_NSH_CONVERTOR);
        registerAction13(NxActionPopNshNodesNodeTableFlowWriteActionsCase.class, POP_NSH_CONVERTOR);
        registerAction13(NxActionPushEthNodesNodeTableFlowWriteActionsCase.class, PUSH_ETH_CONVERTOR);
        registerAction13(NxActionPopEthNodesNodeTableFlowWriteActionsCase.class, POP_ETH_CONVERTOR);

        registerAction13(NxActionRegLoadNodesNodeGroupBucketsBucketActionsCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegMoveNodesNodeGroupBucketsBucketActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegNodesNodeGroupBucketsBucketActionsCase.class, OUTPUT_REG_CONVERTOR);
        registerAction13(NxActionResubmitNodesNodeGroupBucketsBucketActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionMultipathNodesNodeGroupBucketsBucketActionsCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionPushNshNodesNodeGroupBucketsBucketActionsCase.class, PUSH_NSH_CONVERTOR);
        registerAction13(NxActionPopNshNodesNodeGroupBucketsBucketActionsCase.class, POP_NSH_CONVERTOR);
        registerAction13(NxActionPushEthNodesNodeGroupBucketsBucketActionsCase.class, PUSH_ETH_CONVERTOR);
        registerAction13(NxActionPopEthNodesNodeGroupBucketsBucketActionsCase.class, POP_ETH_CONVERTOR);

        // src=rpc-addFlow
        registerAction13(NxActionRegLoadRpcAddFlowApplyActionsCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegMoveRpcAddFlowApplyActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegRpcAddFlowApplyActionsCase.class, OUTPUT_REG_CONVERTOR);
        registerAction13(NxActionResubmitRpcAddFlowApplyActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionMultipathRpcAddFlowApplyActionsCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionPushNshRpcAddFlowApplyActionsCase.class, PUSH_NSH_CONVERTOR);
        registerAction13(NxActionPopNshRpcAddFlowApplyActionsCase.class, POP_NSH_CONVERTOR);
        registerAction13(NxActionPushEthRpcAddFlowApplyActionsCase.class, PUSH_ETH_CONVERTOR);
        registerAction13(NxActionPopEthRpcAddFlowApplyActionsCase.class, POP_ETH_CONVERTOR);

        registerAction13(NxActionRegLoadRpcAddFlowWriteActionsCase.class, REG_LOAD_CONVERTOR);
        registerAction13(NxActionRegMoveRpcAddFlowWriteActionsCase.class, REG_MOVE_CONVERTOR);
        registerAction13(NxActionOutputRegRpcAddFlowWriteActionsCase.class, OUTPUT_REG_CONVERTOR);
        registerAction13(NxActionResubmitRpcAddFlowWriteActionsCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionMultipathRpcAddFlowWriteActionsCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionPushNshRpcAddFlowWriteActionsCase.class, PUSH_NSH_CONVERTOR);
        registerAction13(NxActionPopNshRpcAddFlowWriteActionsCase.class, POP_NSH_CONVERTOR);
        registerAction13(NxActionPushEthRpcAddFlowWriteActionsCase.class, PUSH_ETH_CONVERTOR);
        registerAction13(NxActionPopEthRpcAddFlowWriteActionsCase.class, POP_ETH_CONVERTOR);

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
        registerAction13(NxActionResubmitRpcAddGroupCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionResubmitRpcRemoveGroupCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionResubmitRpcUpdateGroupOriginalCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionResubmitRpcUpdateGroupUpdatedCase.class, RESUBMIT_CONVERTOR);
        registerAction13(NxActionMultipathRpcAddGroupCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionMultipathRpcRemoveGroupCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionMultipathRpcUpdateGroupOriginalCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionMultipathRpcUpdateGroupUpdatedCase.class, MULTIPATH_CONVERTOR);
        registerAction13(NxActionPushNshRpcAddGroupCase.class, PUSH_NSH_CONVERTOR);
        registerAction13(NxActionPushNshRpcRemoveGroupCase.class, PUSH_NSH_CONVERTOR);
        registerAction13(NxActionPushNshRpcUpdateGroupOriginalCase.class, PUSH_NSH_CONVERTOR);
        registerAction13(NxActionPushNshRpcUpdateGroupUpdatedCase.class, PUSH_NSH_CONVERTOR);
        registerAction13(NxActionPopNshRpcAddGroupCase.class, POP_NSH_CONVERTOR);
        registerAction13(NxActionPopNshRpcRemoveGroupCase.class, POP_NSH_CONVERTOR);
        registerAction13(NxActionPopNshRpcUpdateGroupOriginalCase.class, POP_NSH_CONVERTOR);
        registerAction13(NxActionPopNshRpcUpdateGroupUpdatedCase.class, POP_NSH_CONVERTOR);
        registerAction13(NxActionPushEthRpcAddGroupCase.class, PUSH_ETH_CONVERTOR);
        registerAction13(NxActionPushEthRpcRemoveGroupCase.class, PUSH_ETH_CONVERTOR);
        registerAction13(NxActionPushEthRpcUpdateGroupOriginalCase.class, PUSH_ETH_CONVERTOR);
        registerAction13(NxActionPushEthRpcUpdateGroupUpdatedCase.class, PUSH_ETH_CONVERTOR);
        registerAction13(NxActionPopEthRpcAddGroupCase.class, POP_ETH_CONVERTOR);
        registerAction13(NxActionPopEthRpcRemoveGroupCase.class, POP_ETH_CONVERTOR);
        registerAction13(NxActionPopEthRpcUpdateGroupOriginalCase.class, POP_ETH_CONVERTOR);
        registerAction13(NxActionPopEthRpcUpdateGroupUpdatedCase.class, POP_ETH_CONVERTOR);


        registerAction13(ActionRegLoad.class, REG_LOAD_CONVERTOR);
        registerAction13(ActionRegMove.class, REG_MOVE_CONVERTOR);
        registerAction13(ActionOutputReg.class, OUTPUT_REG_CONVERTOR);
        registerAction13(ActionResubmit.class, RESUBMIT_CONVERTOR);
        registerAction13(ActionMultipath.class, MULTIPATH_CONVERTOR);
        registerAction13(ActionPushNsh.class, PUSH_NSH_CONVERTOR);
        registerAction13(ActionPopNsh.class, POP_NSH_CONVERTOR);
        registerAction13(ActionPushEth.class, PUSH_ETH_CONVERTOR);
        registerAction13(ActionPopEth.class, POP_ETH_CONVERTOR);

        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(RegLoadCodec.SERIALIZER_KEY), REG_LOAD_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(RegMoveCodec.SERIALIZER_KEY), REG_MOVE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(OutputRegCodec.SERIALIZER_KEY), OUTPUT_REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(ResubmitCodec.SERIALIZER_KEY), RESUBMIT_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(MultipathCodec.SERIALIZER_KEY), MULTIPATH_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(PushNshCodec.SERIALIZER_KEY), PUSH_NSH_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(PopNshCodec.SERIALIZER_KEY), POP_NSH_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(PushEthCodec.SERIALIZER_KEY), PUSH_ETH_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(NiciraUtil.createOfJavaKeyFrom(PopEthCodec.SERIALIZER_KEY), POP_ETH_CONVERTOR));

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
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmOfEthSrcKey.class, EncodeConstants.OF13_VERSION_ID), ETH_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(EthSrcCodec.SERIALIZER_KEY, ETH_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmOfEthDstKey.class, EncodeConstants.OF13_VERSION_ID), ETH_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(EthDstCodec.SERIALIZER_KEY, ETH_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmOfEthTypeKey.class, EncodeConstants.OF13_VERSION_ID), ETH_TYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(EthTypeCodec.SERIALIZER_KEY, ETH_TYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxNspKey.class, EncodeConstants.OF13_VERSION_ID), NSP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(NspCodec.SERIALIZER_KEY, NSP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxNsiKey.class, EncodeConstants.OF13_VERSION_ID), NSI_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(NsiCodec.SERIALIZER_KEY, NSI_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxNshc1Key.class, EncodeConstants.OF13_VERSION_ID), NSC1_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Nshc1Codec.SERIALIZER_KEY, NSC1_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxNshc2Key.class, EncodeConstants.OF13_VERSION_ID), NSC2_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Nshc2Codec.SERIALIZER_KEY, NSC2_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxNshc3Key.class, EncodeConstants.OF13_VERSION_ID), NSC3_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Nshc3Codec.SERIALIZER_KEY, NSC3_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxNshc4Key.class, EncodeConstants.OF13_VERSION_ID), NSC4_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Nshc4Codec.SERIALIZER_KEY, NSC4_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxTunIpv4SrcKey.class, EncodeConstants.OF13_VERSION_ID), TUN_IPV4_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(TunIpv4SrcCodec.SERIALIZER_KEY, TUN_IPV4_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxTunIpv4DstKey.class, EncodeConstants.OF13_VERSION_ID), TUN_IPV4_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(TunIpv4DstCodec.SERIALIZER_KEY, TUN_IPV4_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxEncapEthTypeKey.class, EncodeConstants.OF13_VERSION_ID), ENCAP_ETH_TYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(EncapEthTypeCodec.SERIALIZER_KEY, ENCAP_ETH_TYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxEncapEthSrcKey.class, EncodeConstants.OF13_VERSION_ID), ENCAP_ETH_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(EncapEthSrcCodec.SERIALIZER_KEY, ENCAP_ETH_SRC_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxEncapEthDstKey.class, EncodeConstants.OF13_VERSION_ID), ENCAP_ETH_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(EncapEthDstCodec.SERIALIZER_KEY, ENCAP_ETH_DST_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxNshMdtypeKey.class, EncodeConstants.OF13_VERSION_ID), NSH_MDTYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(NshMdtypeCodec.SERIALIZER_KEY, NSH_MDTYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxNshNpKey.class, EncodeConstants.OF13_VERSION_ID), NSH_NP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(NshMdtypeCodec.SERIALIZER_KEY, NSH_NP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxTunGpeNpKey.class, EncodeConstants.OF13_VERSION_ID), TUN_GPE_NP_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(TunGpeNpCodec.SERIALIZER_KEY, TUN_GPE_NP_CONVERTOR));
    }

    /**
     * @param actionCaseType
     * @param actionConvertor
     */
    private void registerAction13(
            final Class<? extends Action> actionCaseType,
            final ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action> actionConvertor) {
        TypeVersionKey<? extends Action> key = new TypeVersionKey<>(actionCaseType, EncodeConstants.OF13_VERSION_ID);
        registrations.add(extensionConverterRegistrator.registerActionConvertor(key, actionConvertor));
    }
    /**
     * @param actionCaseType
     * @param actionConvertor
     */
    private void registerAction13(
            Class<? extends ActionChoice> actionCaseType,
            ConvertorActionFromOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action, ActionPath> actionConvertor) {
        ActionSerializerKey<?> key = new ActionSerializerKey(EncodeConstants.OF13_VERSION_ID, actionCaseType, null);
        registrations.add(extensionConverterRegistrator.registerActionConvertor(key, actionConvertor));
    }

}
