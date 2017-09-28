/**
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraExtensionCodecRegistrator;
import org.opendaylight.openflowjava.nx.codec.action.MultipathCodec;
import org.opendaylight.openflowjava.nx.codec.action.OutputRegCodec;
import org.opendaylight.openflowjava.nx.codec.action.RegLoadCodec;
import org.opendaylight.openflowjava.nx.codec.action.RegMoveCodec;
import org.opendaylight.openflowjava.nx.codec.action.ResubmitCodec;
import org.opendaylight.openflowjava.nx.codec.action.PushNshCodec;
import org.opendaylight.openflowjava.nx.codec.action.PopNshCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpOpCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpShaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpSpaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpThaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpTpaCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtMarkCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtStateCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtZoneCodec;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPushNsh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPopNsh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtMark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtZone;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNsi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNsp;
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
        niciraExtensionsRegistrator.registerNiciraExtensions();

        Mockito.verify(registrator).registerActionDeserializer(Matchers.eq(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 7)), Matchers.any(RegLoadCodec.class));
        Mockito.verify(registrator).registerActionSerializer(Matchers.eq(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionRegLoad.class)), Matchers.any(RegLoadCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(Matchers.eq(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 6)), Matchers.any(RegMoveCodec.class));
        Mockito.verify(registrator).registerActionSerializer(Matchers.eq(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionRegMove.class)), Matchers.any(RegMoveCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(Matchers.eq(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 15)), Matchers.any(OutputRegCodec.class));
        Mockito.verify(registrator).registerActionSerializer(Matchers.eq(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionOutputReg.class)), Matchers.any(OutputRegCodec.class));
        Mockito.verify(registrator).registerActionSerializer(Matchers.eq(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionResubmit.class)), Matchers.any(ResubmitCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(Matchers.eq(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 1)), Matchers.any(ResubmitCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(Matchers.eq(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 14)), Matchers.any(ResubmitCodec.class));
        Mockito.verify(registrator).registerActionSerializer(Matchers.eq(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionMultipath.class)), Matchers.any(MultipathCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(Matchers.eq(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 10)), Matchers.any(MultipathCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(Matchers.eq(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 38)), Matchers.any(PushNshCodec.class));
        Mockito.verify(registrator).registerActionSerializer(Matchers.eq(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionPushNsh.class)), Matchers.any(PushNshCodec.class));
        Mockito.verify(registrator).registerActionDeserializer(Matchers.eq(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 39)), Matchers.any(PopNshCodec.class));
        Mockito.verify(registrator).registerActionSerializer(Matchers.eq(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionPopNsh.class)), Matchers.any(PopNshCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg0.class)), Matchers.any(Reg0Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 0)), Matchers.any(Reg0Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg1.class)), Matchers.any(Reg1Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 1)), Matchers.any(Reg1Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg2.class)), Matchers.any(Reg2Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 2)), Matchers.any(Reg2Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg3.class)), Matchers.any(Reg3Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 3)), Matchers.any(Reg3Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg4.class)), Matchers.any(Reg4Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 4)), Matchers.any(Reg4Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg5.class)), Matchers.any(Reg5Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 5)), Matchers.any(Reg5Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg6.class)), Matchers.any(Reg6Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 6)), Matchers.any(Reg6Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg7.class)), Matchers.any(Reg7Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 7)), Matchers.any(Reg7Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxTunId.class)), Matchers.any(TunIdCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 16)), Matchers.any(TunIdCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfArpOp.class)), Matchers.any(ArpOpCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 15)), Matchers.any(ArpOpCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxArpSha.class)), Matchers.any(ArpShaCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 17)), Matchers.any(ArpShaCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfArpSpa.class)), Matchers.any(ArpSpaCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 16)), Matchers.any(ArpSpaCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxArpTha.class)), Matchers.any(ArpThaCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 18)), Matchers.any(ArpThaCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfArpTpa.class)), Matchers.any(ArpTpaCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 17)), Matchers.any(ArpTpaCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfEthDst.class)), Matchers.any(EthDstCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 1)), Matchers.any(EthDstCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfEthSrc.class)), Matchers.any(EthSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 2)), Matchers.any(EthSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfEthType.class)), Matchers.any(EthTypeCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 3)), Matchers.any(EthTypeCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNsp.class)), Matchers.any(NspCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 113)), Matchers.any(NspCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNshc1.class)), Matchers.any(Nshc1Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 115)), Matchers.any(Nshc1Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNshc2.class)), Matchers.any(Nshc2Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 116)), Matchers.any(Nshc2Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNshc3.class)), Matchers.any(Nshc3Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 117)), Matchers.any(Nshc3Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNshc4.class)), Matchers.any(Nshc4Codec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 118)), Matchers.any(Nshc4Codec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNsi.class)), Matchers.any(NsiCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 114)), Matchers.any(NsiCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxTunIpv4Dst.class)), Matchers.any(TunIpv4DstCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 32)), Matchers.any(TunIpv4DstCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxTunIpv4Src.class)), Matchers.any(TunIpv4SrcCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 31)), Matchers.any(TunIpv4SrcCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfTcpSrc.class)), Matchers.any(TcpSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 9)), Matchers.any(TcpSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfTcpDst.class)), Matchers.any(TcpDstCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 10)), Matchers.any(TcpDstCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfUdpSrc.class)), Matchers.any(UdpSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 11)), Matchers.any(UdpSrcCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfUdpDst.class)), Matchers.any(UdpDstCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 12)), Matchers.any(UdpDstCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxCtState.class)), Matchers.any(CtStateCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 105)), Matchers.any(CtStateCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxCtZone.class)), Matchers.any(CtZoneCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 106)), Matchers.any(CtZoneCodec.class));
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers.eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxCtMark.class)), Matchers.any(CtMarkCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers.eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 107)), Matchers.any(CtMarkCodec.class));
    }

    @Test
    public void unregisterExtensionsTest() {
        niciraExtensionsRegistrator.unregisterExtensions();

        Mockito.verify(registrator).unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 7));
        Mockito.verify(registrator).unregisterActionSerializer(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionRegLoad.class));
        Mockito.verify(registrator).unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 6));
        Mockito.verify(registrator).unregisterActionSerializer(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionRegMove.class));
        Mockito.verify(registrator).unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 15));
        Mockito.verify(registrator).unregisterActionSerializer(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionOutputReg.class));
        Mockito.verify(registrator).unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 1));
        Mockito.verify(registrator).unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 14));
        Mockito.verify(registrator).unregisterActionSerializer(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionResubmit.class));
        Mockito.verify(registrator).unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 10));
        Mockito.verify(registrator).unregisterActionSerializer(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionMultipath.class));
        Mockito.verify(registrator).unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 38));
        Mockito.verify(registrator).unregisterActionSerializer(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionPushNsh.class));
        Mockito.verify(registrator).unregisterActionDeserializer(new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 39));
        Mockito.verify(registrator).unregisterActionSerializer(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionPopNsh.class));
        Mockito.verify(registrator).unregisterActionSerializer(new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionConntrack.class));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg0.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 0));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg1.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 1));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg2.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 2));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg3.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 3));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg4.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 4));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg5.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 5));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg6.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 6));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxReg7.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 7));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxTunId.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 16));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfArpOp.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 15));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxArpSha.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 17));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfArpSpa.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 16));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxArpTha.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 18));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfArpTpa.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 17));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfEthDst.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 1));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfEthSrc.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 2));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfEthType.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 3));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNsp.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 113));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNsi.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 114));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNshc1.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 115));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNshc2.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 116));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNshc3.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 117));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNshc4.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 118));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxTunIpv4Dst.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 32));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxTunIpv4Src.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 31));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfTcpSrc.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 9));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfTcpDst.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 10));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfUdpSrc.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 11));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfUdpDst.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, 12));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxCtState.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 105));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxCtZone.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 106));
        Mockito.verify(registrator).unregisterMatchEntrySerializer(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxCtMark.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, 107));
    }
}
