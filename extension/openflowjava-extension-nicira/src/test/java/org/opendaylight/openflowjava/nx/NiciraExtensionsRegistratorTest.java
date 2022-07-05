/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.nx.api.NiciraExtensionCodecRegistrator;
import org.opendaylight.openflowjava.nx.codec.action.DecNshTtlCodec;
import org.opendaylight.openflowjava.nx.codec.action.DecapCodec;
import org.opendaylight.openflowjava.nx.codec.action.EncapCodec;
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
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionDecNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionDecap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionEncap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionOutputReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtMark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtTpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtTpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtZone;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshMdtype;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshNp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNsi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxPktMark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg0;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg5;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg7;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxTunId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxTunIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxTunIpv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfEthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfEthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfEthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfTcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfTcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfUdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfUdpSrc;

@RunWith(MockitoJUnitRunner.class)
public class NiciraExtensionsRegistratorTest {

    NiciraExtensionsRegistrator niciraExtensionsRegistrator;

    @Mock
    NiciraExtensionCodecRegistrator registrator;

    @Before
    public void setUp() {
        niciraExtensionsRegistrator = new NiciraExtensionsRegistrator(registrator);
    }

    @Test
    public void registerNiciraExtensionsTest() {
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 7)),
                any(RegLoadCodec.class));
        Mockito.verify(registrator).registerActionSerializer(
                eq(new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionRegLoad.class)),
                any(RegLoadCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 33)),
                any(RegLoad2Codec.class));
        Mockito.verify(registrator).registerActionSerializer(
                eq(new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionRegLoad2.class)),
                any(RegLoad2Codec.class));
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 6)),
                any(RegMoveCodec.class));
        Mockito.verify(registrator).registerActionSerializer(
                eq(new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionRegMove.class)),
                any(RegMoveCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 15)),
                any(OutputRegCodec.class));
        Mockito.verify(registrator).registerActionSerializer(
                eq(new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionOutputReg.class)),
                any(OutputRegCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 32)),
                any(OutputReg2Codec.class));
        Mockito.verify(registrator).registerActionSerializer(
                eq(new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionOutputReg2.class)),
                any(OutputReg2Codec.class));
        Mockito.verify(registrator).registerActionSerializer(
                eq(new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionResubmit.class)),
                any(ResubmitCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 1)),
                any(ResubmitCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 14)),
                any(ResubmitCodec.class));
        Mockito.verify(registrator).registerActionSerializer(
                eq(new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionMultipath.class)),
                any(MultipathCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 10)),
                any(MultipathCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 46)),
                any(EncapCodec.class));
        Mockito.verify(registrator).registerActionSerializer(
                eq(new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionEncap.class)),
                any(EncapCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 47)),
                any(DecapCodec.class));
        Mockito.verify(registrator).registerActionSerializer(
                eq(new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionDecap.class)),
                any(DecapCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg0.VALUE)),
                any(Reg0Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 0)),
                any(Reg0Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg1.VALUE)),
                any(Reg1Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 1)),
                any(Reg1Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg2.VALUE)),
                any(Reg2Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 2)),
                any(Reg2Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg3.VALUE)),
                any(Reg3Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 3)),
                any(Reg3Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg4.VALUE)),
                any(Reg4Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 4)),
                any(Reg4Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg5.VALUE)),
                any(Reg5Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 5)),
                any(Reg5Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg6.VALUE)),
                any(Reg6Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 6)),
                any(Reg6Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg7.VALUE)),
                any(Reg7Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 7)),
                any(Reg7Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxTunId.VALUE)),
                any(TunIdCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 16)),
                any(TunIdCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfArpOp.VALUE)),
                any(ArpOpCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 15)),
                any(ArpOpCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxArpSha.VALUE)),
                any(ArpShaCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 17)),
                any(ArpShaCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfArpSpa.VALUE)),
                any(ArpSpaCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 16)),
                any(ArpSpaCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxArpTha.VALUE)),
                any(ArpThaCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 18)),
                any(ArpThaCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfArpTpa.VALUE)),
                any(ArpTpaCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 17)),
                any(ArpTpaCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfEthDst.VALUE)),
                any(EthDstCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 1)),
                any(EthDstCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfEthSrc.VALUE)),
                any(EthSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 2)),
                any(EthSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfEthType.VALUE)),
                any(EthTypeCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 3)),
                any(EthTypeCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxTunIpv4Dst.VALUE)),
                any(TunIpv4DstCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 32)),
                any(TunIpv4DstCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxTunIpv4Src.VALUE)),
                any(TunIpv4SrcCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 31)),
                any(TunIpv4SrcCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfTcpSrc.VALUE)),
                any(TcpSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 9)),
                any(TcpSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfTcpDst.VALUE)),
                any(TcpDstCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 10)),
                any(TcpDstCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfUdpSrc.VALUE)),
                any(UdpSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 11)),
                any(UdpSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfUdpDst.VALUE)),
                any(UdpDstCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 12)),
                any(UdpDstCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxCtState.VALUE)),
                any(CtStateCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 105)),
                any(CtStateCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxCtZone.VALUE)),
                any(CtZoneCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 106)),
                any(CtZoneCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxCtMark.VALUE)),
                any(CtMarkCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 107)),
                any(CtMarkCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE,
                                NxmNxCtTpSrc.VALUE)), any(CtTpSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3,
                                OxmMatchConstants.NXM_1_CLASS, 124)), any(CtTpSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE,
                                NxmNxCtTpDst.VALUE)), any(CtTpDstCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3,
                                OxmMatchConstants.NXM_1_CLASS, 125)), any(CtTpDstCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(eq(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE,
                        NxmNxPktMark.VALUE)), any(PktMarkCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(eq(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3,
                        OxmMatchConstants.NXM_1_CLASS, 33)), any(PktMarkCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(
                eq(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 48)),
                any(DecNshTtlCodec.class));
        Mockito.verify(registrator).registerActionSerializer(
                eq(new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionDecNshTtl.class)),
                any(DecNshTtlCodec.class));

        // experimenters
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshFlags> nshFlagsSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshFlags.VALUE);
        nshFlagsSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(nshFlagsSerializerKey),
                any(NshFlagsCodec.class));
        MatchEntryDeserializerKey nshFlagsDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                1);
        nshFlagsDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntryDeserializer(
                eq(nshFlagsDeserializerKey),
                any(NshFlagsCodec.class));
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshMdtype> mdtypeSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshMdtype.VALUE);
        mdtypeSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(mdtypeSerializerKey),
                any(NshMdtypeCodec.class));
        MatchEntryDeserializerKey mdtypeDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                2);
        mdtypeDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntryDeserializer(
                eq(mdtypeDeserializerKey),
                any(NshMdtypeCodec.class));
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshNp> nshNpSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshNp.VALUE);
        nshNpSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(nshNpSerializerKey),
                any(NshNpCodec.class));
        MatchEntryDeserializerKey nshNpDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                3);
        nshNpDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntryDeserializer(
                eq(nshNpDeserializerKey),
                any(NshNpCodec.class));
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNsp> nspSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNsp.VALUE);
        nspSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(nspSerializerKey),
                any(NspCodec.class));
        MatchEntryDeserializerKey nspDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                4);
        nspDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntryDeserializer(
                eq(nspDeserializerKey),
                any(NspCodec.class));
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNsi> nsiSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNsi.VALUE);
        nsiSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(nsiSerializerKey),
                any(NsiCodec.class));
        MatchEntryDeserializerKey nsiDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                5);
        nsiDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntryDeserializer(
                eq(nsiDeserializerKey),
                any(NsiCodec.class));
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshc1> nshc1SerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshc1.VALUE);
        nshc1SerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(nshc1SerializerKey),
                any(Nshc1Codec.class));
        MatchEntryDeserializerKey nshc1DeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                6);
        nshc1DeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntryDeserializer(
                eq(nshc1DeserializerKey),
                any(Nshc1Codec.class));
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshc2> nshc2SerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshc2.VALUE);
        nshc2SerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(nshc2SerializerKey),
                any(Nshc2Codec.class));
        MatchEntryDeserializerKey nshc2DeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                7);
        nshc2DeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntryDeserializer(
                eq(nshc2DeserializerKey),
                any(Nshc2Codec.class));
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshc3> nshc3SerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshc3.VALUE);
        nshc3SerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(nshc3SerializerKey),
                any(Nshc3Codec.class));
        MatchEntryDeserializerKey nshc3DeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                8);
        nshc3DeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntryDeserializer(
                eq(nshc3DeserializerKey),
                any(Nshc3Codec.class));
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshc4> nshc4SerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshc4.VALUE);
        nshc4SerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(nshc4SerializerKey),
                any(Nshc4Codec.class));
        MatchEntryDeserializerKey nshc4DeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                9);
        nshc4DeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntryDeserializer(
                eq(nshc4DeserializerKey),
                any(Nshc4Codec.class));
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshTtl> nshTtlSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshTtl.VALUE);
        nshTtlSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntrySerializer(
                eq(nshTtlSerializerKey),
                any(NshTtlCodec.class));
        MatchEntryDeserializerKey nshTtlDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                10);
        nshTtlDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).registerMatchEntryDeserializer(
                eq(nshTtlDeserializerKey),
                any(NshTtlCodec.class));
    }

    @Test
    public void unregisterExtensionsTest() {
        niciraExtensionsRegistrator.close();

        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 7));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionRegLoad.class));
        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 33));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionRegLoad2.class));
        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 6));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionRegMove.class));
        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 15));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionOutputReg.class));
        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 32));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionOutputReg2.class));
        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 1));
        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 14));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionResubmit.class));
        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 10));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionMultipath.class));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionConntrack.class));
        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 46));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionEncap.class));
        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 47));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionDecap.class));
        Mockito.verify(registrator)
                .unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 48));
        Mockito.verify(registrator).unregisterActionSerializer(
                new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionDecNshTtl.class));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg0.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 0));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg1.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 1));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg2.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 2));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg3.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 3));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg4.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 4));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg5.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 5));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg6.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 6));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxReg7.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 7));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxTunId.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 16));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfArpOp.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 15));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxArpSha.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 17));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfArpSpa.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 16));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxArpTha.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 18));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfArpTpa.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 17));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfEthDst.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 1));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfEthSrc.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 2));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfEthType.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 3));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxTunIpv4Dst.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 32));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxTunIpv4Src.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 31));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfTcpSrc.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 9));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfTcpDst.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 10));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfUdpSrc.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 11));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfUdpDst.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, 12));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxCtState.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 105));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxCtZone.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 106));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxCtMark.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 107));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxCtTpSrc.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 124));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxCtTpDst.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 125));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxPktMark.VALUE));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, 33));

        // experimenters
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshFlags> nshFlagsSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshFlags.VALUE);
        nshFlagsSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntrySerializer(nshFlagsSerializerKey);
        MatchEntryDeserializerKey nshFlagsDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                1);
        nshFlagsDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(nshFlagsDeserializerKey);
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshMdtype> mdtypeSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshMdtype.VALUE);
        mdtypeSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntrySerializer(mdtypeSerializerKey);
        MatchEntryDeserializerKey mdtypeDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                2);
        mdtypeDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(mdtypeDeserializerKey);
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshNp> nshNpSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshNp.VALUE);
        nshNpSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntrySerializer(nshNpSerializerKey);
        MatchEntryDeserializerKey nshNpDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                3);
        nshNpDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(nshNpDeserializerKey);
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNsp> nspSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNsp.VALUE);
        nspSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntrySerializer(nspSerializerKey);
        MatchEntryDeserializerKey nspDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                4);
        nspDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(nspDeserializerKey);
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNsi> nsiSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNsi.VALUE);
        nsiSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntrySerializer(nsiSerializerKey);
        MatchEntryDeserializerKey nsiDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                5);
        nsiDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(nsiDeserializerKey);
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshc1> nshc1SerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshc1.VALUE);
        nshc1SerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntrySerializer(nshc1SerializerKey);
        MatchEntryDeserializerKey nshc1DeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                6);
        nshc1DeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(nshc1DeserializerKey);
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshc2> nshc2SerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshc2.VALUE);
        nshc2SerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntrySerializer(nshc2SerializerKey);
        MatchEntryDeserializerKey nshc2DeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                7);
        nshc2DeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(nshc2DeserializerKey);
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshc3> nshc3SerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshc3.VALUE);
        nshc3SerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntrySerializer(nshc3SerializerKey);
        MatchEntryDeserializerKey nshc3DeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                8);
        nshc3DeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(nshc3DeserializerKey);
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshc4> nshc4SerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshc4.VALUE);
        nshc4SerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntrySerializer(nshc1SerializerKey);
        MatchEntryDeserializerKey nshc4DeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                9);
        nshc4DeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(nshc4DeserializerKey);
        MatchEntrySerializerKey<ExperimenterClass, NxmNxNshTtl> nshTtlSerializerKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                ExperimenterClass.VALUE,
                NxmNxNshTtl.VALUE);
        nshTtlSerializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntrySerializer(nshTtlSerializerKey);
        MatchEntryDeserializerKey nshTtlDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.EXPERIMENTER_CLASS,
                10);
        nshTtlDeserializerKey.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(nshTtlDeserializerKey);
    }
}
