/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx;

import static java.util.Objects.requireNonNull;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.openflowjava.nx.api.NiciraExtensionCodecRegistrator;
import org.opendaylight.openflowjava.nx.codec.action.ConntrackCodec;
import org.opendaylight.openflowjava.nx.codec.action.CtClearCodec;
import org.opendaylight.openflowjava.nx.codec.action.DecNshTtlCodec;
import org.opendaylight.openflowjava.nx.codec.action.DecapCodec;
import org.opendaylight.openflowjava.nx.codec.action.EncapCodec;
import org.opendaylight.openflowjava.nx.codec.action.FinTimeoutCodec;
import org.opendaylight.openflowjava.nx.codec.action.LearnCodec;
import org.opendaylight.openflowjava.nx.codec.action.MultipathCodec;
import org.opendaylight.openflowjava.nx.codec.action.NiciraActionCodecs;
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
import org.opendaylight.openflowjava.nx.codec.match.MplsLabelCodec;
import org.opendaylight.openflowjava.nx.codec.match.NiciraMatchCodecs;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component(service = { })
public final class NiciraExtensionsRegistrator implements AutoCloseable {
    private final NiciraExtensionCodecRegistrator registrator;

    @Inject
    @Activate
    public NiciraExtensionsRegistrator(@Reference final NiciraExtensionCodecRegistrator registrator) {
        this.registrator = requireNonNull(registrator);

        registrator.registerActionDeserializer(RegLoadCodec.DESERIALIZER_KEY, NiciraActionCodecs.REG_LOAD_CODEC);
        registrator.registerActionSerializer(RegLoadCodec.SERIALIZER_KEY, NiciraActionCodecs.REG_LOAD_CODEC);
        registrator.registerActionDeserializer(RegLoad2Codec.DESERIALIZER_KEY, NiciraActionCodecs.REG_LOAD2_CODEC);
        registrator.registerActionSerializer(RegLoad2Codec.SERIALIZER_KEY, NiciraActionCodecs.REG_LOAD2_CODEC);
        registrator.registerActionDeserializer(RegMoveCodec.DESERIALIZER_KEY, NiciraActionCodecs.REG_MOVE_CODEC);
        registrator.registerActionSerializer(RegMoveCodec.SERIALIZER_KEY, NiciraActionCodecs.REG_MOVE_CODEC);
        registrator.registerActionDeserializer(OutputRegCodec.DESERIALIZER_KEY, NiciraActionCodecs.OUTPUT_REG_CODEC);
        registrator.registerActionSerializer(OutputRegCodec.SERIALIZER_KEY, NiciraActionCodecs.OUTPUT_REG_CODEC);
        registrator.registerActionDeserializer(OutputReg2Codec.DESERIALIZER_KEY, NiciraActionCodecs.OUTPUT_REG2_CODEC);
        registrator.registerActionSerializer(OutputReg2Codec.SERIALIZER_KEY, NiciraActionCodecs.OUTPUT_REG2_CODEC);
        registrator.registerActionSerializer(ResubmitCodec.SERIALIZER_KEY, NiciraActionCodecs.RESUBMIT_CODEC);
        registrator.registerActionDeserializer(ResubmitCodec.DESERIALIZER_KEY, NiciraActionCodecs.RESUBMIT_CODEC);
        registrator.registerActionDeserializer(ResubmitCodec.TABLE_DESERIALIZER_KEY, NiciraActionCodecs.RESUBMIT_CODEC);
        registrator.registerActionSerializer(FinTimeoutCodec.SERIALIZER_KEY, NiciraActionCodecs.FIN_TIMEOUT_CODEC);
        registrator.registerActionDeserializer(FinTimeoutCodec.DESERIALIZER_KEY, NiciraActionCodecs.FIN_TIMEOUT_CODEC);
        registrator.registerActionSerializer(MultipathCodec.SERIALIZER_KEY, NiciraActionCodecs.MULTIPATH_CODEC);
        registrator.registerActionDeserializer(MultipathCodec.DESERIALIZER_KEY, NiciraActionCodecs.MULTIPATH_CODEC);

        registrator.registerActionSerializer(ConntrackCodec.SERIALIZER_KEY, NiciraActionCodecs.CONNTRACK_CODEC);
        registrator.registerActionDeserializer(ConntrackCodec.DESERIALIZER_KEY, NiciraActionCodecs.CONNTRACK_CODEC);
        registrator.registerActionSerializer(CtClearCodec.SERIALIZER_KEY, NiciraActionCodecs.CT_CLEAR_CODEC);
        registrator.registerActionDeserializer(CtClearCodec.DESERIALIZER_KEY, NiciraActionCodecs.CT_CLEAR_CODEC);
        registrator.registerActionSerializer(LearnCodec.SERIALIZER_KEY, NiciraActionCodecs.LEARN_CODEC);
        registrator.registerActionDeserializer(LearnCodec.DESERIALIZER_KEY, NiciraActionCodecs.LEARN_CODEC);
        registrator.registerActionDeserializer(EncapCodec.DESERIALIZER_KEY, NiciraActionCodecs.ENCAP_CODEC);
        registrator.registerActionSerializer(EncapCodec.SERIALIZER_KEY, NiciraActionCodecs.ENCAP_CODEC);
        registrator.registerActionDeserializer(DecapCodec.DESERIALIZER_KEY, NiciraActionCodecs.DECAP_CODEC);
        registrator.registerActionSerializer(DecapCodec.SERIALIZER_KEY, NiciraActionCodecs.DECAP_CODEC);
        registrator.registerActionDeserializer(DecNshTtlCodec.DESERIALIZER_KEY, NiciraActionCodecs.DEC_NSH_TTL_CODEC);
        registrator.registerActionSerializer(DecNshTtlCodec.SERIALIZER_KEY, NiciraActionCodecs.DEC_NSH_TTL_CODEC);
        // Continue

        registrator.registerMatchEntrySerializer(Reg0Codec.SERIALIZER_KEY, NiciraMatchCodecs.REG0_CODEC);
        registrator.registerMatchEntryDeserializer(Reg0Codec.DESERIALIZER_KEY, NiciraMatchCodecs.REG0_CODEC);
        registrator.registerMatchEntrySerializer(Reg1Codec.SERIALIZER_KEY, NiciraMatchCodecs.REG1_CODEC);
        registrator.registerMatchEntryDeserializer(Reg1Codec.DESERIALIZER_KEY, NiciraMatchCodecs.REG1_CODEC);
        registrator.registerMatchEntrySerializer(Reg2Codec.SERIALIZER_KEY, NiciraMatchCodecs.REG2_CODEC);
        registrator.registerMatchEntryDeserializer(Reg2Codec.DESERIALIZER_KEY, NiciraMatchCodecs.REG2_CODEC);
        registrator.registerMatchEntrySerializer(Reg3Codec.SERIALIZER_KEY, NiciraMatchCodecs.REG3_CODEC);
        registrator.registerMatchEntryDeserializer(Reg3Codec.DESERIALIZER_KEY, NiciraMatchCodecs.REG3_CODEC);
        registrator.registerMatchEntrySerializer(Reg4Codec.SERIALIZER_KEY, NiciraMatchCodecs.REG4_CODEC);
        registrator.registerMatchEntryDeserializer(Reg4Codec.DESERIALIZER_KEY, NiciraMatchCodecs.REG4_CODEC);
        registrator.registerMatchEntrySerializer(Reg5Codec.SERIALIZER_KEY, NiciraMatchCodecs.REG5_CODEC);
        registrator.registerMatchEntryDeserializer(Reg5Codec.DESERIALIZER_KEY, NiciraMatchCodecs.REG5_CODEC);
        registrator.registerMatchEntrySerializer(Reg6Codec.SERIALIZER_KEY, NiciraMatchCodecs.REG6_CODEC);
        registrator.registerMatchEntryDeserializer(Reg6Codec.DESERIALIZER_KEY, NiciraMatchCodecs.REG6_CODEC);
        registrator.registerMatchEntrySerializer(Reg7Codec.SERIALIZER_KEY, NiciraMatchCodecs.REG7_CODEC);
        registrator.registerMatchEntryDeserializer(Reg7Codec.DESERIALIZER_KEY, NiciraMatchCodecs.REG7_CODEC);
        registrator.registerMatchEntrySerializer(TunIdCodec.SERIALIZER_KEY, NiciraMatchCodecs.TUN_ID_CODEC);
        registrator.registerMatchEntryDeserializer(TunIdCodec.DESERIALIZER_KEY, NiciraMatchCodecs.TUN_ID_CODEC);
        registrator.registerMatchEntrySerializer(ArpOpCodec.SERIALIZER_KEY, NiciraMatchCodecs.ARP_OP_CODEC);
        registrator.registerMatchEntryDeserializer(ArpOpCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ARP_OP_CODEC);
        registrator.registerMatchEntrySerializer(ArpShaCodec.SERIALIZER_KEY, NiciraMatchCodecs.ARP_SHA_CODEC);
        registrator.registerMatchEntryDeserializer(ArpShaCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ARP_SHA_CODEC);
        registrator.registerMatchEntrySerializer(ArpSpaCodec.SERIALIZER_KEY, NiciraMatchCodecs.ARP_SPA_CODEC);
        registrator.registerMatchEntryDeserializer(ArpSpaCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ARP_SPA_CODEC);
        registrator.registerMatchEntrySerializer(ArpThaCodec.SERIALIZER_KEY, NiciraMatchCodecs.ARP_THA_CODEC);
        registrator.registerMatchEntryDeserializer(ArpThaCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ARP_THA_CODEC);
        registrator.registerMatchEntrySerializer(ArpTpaCodec.SERIALIZER_KEY, NiciraMatchCodecs.ARP_TPA_CODEC);
        registrator.registerMatchEntryDeserializer(ArpTpaCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ARP_TPA_CODEC);
        registrator.registerMatchEntrySerializer(InPortCodec.SERIALIZER_KEY, NiciraMatchCodecs.NXM_OF_IN_PORT_CODEC);
        registrator.registerMatchEntryDeserializer(InPortCodec.DESERIALIZER_KEY,
                NiciraMatchCodecs.NXM_OF_IN_PORT_CODEC);
        registrator.registerMatchEntrySerializer(MplsLabelCodec.SERIALIZER_KEY, NiciraMatchCodecs.OXM_OF_MPLS_LABEL);
        registrator.registerMatchEntryDeserializer(MplsLabelCodec.DESERIALIZER_KEY,
                NiciraMatchCodecs.OXM_OF_MPLS_LABEL);
        registrator.registerMatchEntrySerializer(EthDstCodec.SERIALIZER_KEY, NiciraMatchCodecs.ETH_DST_CODEC);
        registrator.registerMatchEntryDeserializer(EthDstCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ETH_DST_CODEC);
        registrator.registerMatchEntrySerializer(EthSrcCodec.SERIALIZER_KEY, NiciraMatchCodecs.ETH_SRC_CODEC);
        registrator.registerMatchEntryDeserializer(EthSrcCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ETH_SRC_CODEC);
        registrator.registerMatchEntrySerializer(EthTypeCodec.SERIALIZER_KEY, NiciraMatchCodecs.ETH_TYPE_CODEC);
        registrator.registerMatchEntryDeserializer(EthTypeCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ETH_TYPE_CODEC);
        registrator.registerMatchEntrySerializer(NspCodec.SERIALIZER_KEY, NiciraMatchCodecs.NSP_CODEC);
        registrator.registerMatchEntryDeserializer(NspCodec.DESERIALIZER_KEY, NiciraMatchCodecs.NSP_CODEC);
        registrator.registerMatchEntrySerializer(Nshc1Codec.SERIALIZER_KEY, NiciraMatchCodecs.NSC1_CODEC);
        registrator.registerMatchEntryDeserializer(Nshc1Codec.DESERIALIZER_KEY, NiciraMatchCodecs.NSC1_CODEC);
        registrator.registerMatchEntrySerializer(Nshc2Codec.SERIALIZER_KEY, NiciraMatchCodecs.NSC2_CODEC);
        registrator.registerMatchEntryDeserializer(Nshc2Codec.DESERIALIZER_KEY, NiciraMatchCodecs.NSC2_CODEC);
        registrator.registerMatchEntrySerializer(Nshc3Codec.SERIALIZER_KEY, NiciraMatchCodecs.NSC3_CODEC);
        registrator.registerMatchEntryDeserializer(Nshc3Codec.DESERIALIZER_KEY, NiciraMatchCodecs.NSC3_CODEC);
        registrator.registerMatchEntrySerializer(Nshc4Codec.SERIALIZER_KEY, NiciraMatchCodecs.NSC4_CODEC);
        registrator.registerMatchEntryDeserializer(Nshc4Codec.DESERIALIZER_KEY, NiciraMatchCodecs.NSC4_CODEC);
        registrator.registerMatchEntrySerializer(NsiCodec.SERIALIZER_KEY, NiciraMatchCodecs.NSI_CODEC);
        registrator.registerMatchEntryDeserializer(NsiCodec.DESERIALIZER_KEY, NiciraMatchCodecs.NSI_CODEC);
        registrator.registerMatchEntrySerializer(NshFlagsCodec.SERIALIZER_KEY, NiciraMatchCodecs.NSH_FLAGS_CODEC);
        registrator.registerMatchEntryDeserializer(NshFlagsCodec.DESERIALIZER_KEY, NiciraMatchCodecs.NSH_FLAGS_CODEC);
        registrator.registerMatchEntrySerializer(NshTtlCodec.SERIALIZER_KEY, NiciraMatchCodecs.NSH_TTL_CODEC);
        registrator.registerMatchEntryDeserializer(NshTtlCodec.DESERIALIZER_KEY, NiciraMatchCodecs.NSH_TTL_CODEC);
        registrator.registerMatchEntrySerializer(TunIpv4DstCodec.SERIALIZER_KEY, NiciraMatchCodecs.TUN_IPV4_DST_CODEC);
        registrator.registerMatchEntryDeserializer(TunIpv4DstCodec.DESERIALIZER_KEY,
                NiciraMatchCodecs.TUN_IPV4_DST_CODEC);
        registrator.registerMatchEntrySerializer(TunIpv4SrcCodec.SERIALIZER_KEY, NiciraMatchCodecs.TUN_IPV4_SRC_CODEC);
        registrator.registerMatchEntryDeserializer(TunIpv4SrcCodec.DESERIALIZER_KEY,
                NiciraMatchCodecs.TUN_IPV4_SRC_CODEC);
        registrator.registerMatchEntrySerializer(NshMdtypeCodec.SERIALIZER_KEY, NiciraMatchCodecs.NSH_MDTYPE_CODEC);
        registrator.registerMatchEntryDeserializer(NshMdtypeCodec.DESERIALIZER_KEY, NiciraMatchCodecs.NSH_MDTYPE_CODEC);
        registrator.registerMatchEntrySerializer(NshNpCodec.SERIALIZER_KEY, NiciraMatchCodecs.NSH_NP_CODEC);
        registrator.registerMatchEntryDeserializer(NshNpCodec.DESERIALIZER_KEY, NiciraMatchCodecs.NSH_NP_CODEC);
        registrator.registerMatchEntrySerializer(TcpSrcCodec.SERIALIZER_KEY, NiciraMatchCodecs.TCP_SRC_CODEC);
        registrator.registerMatchEntryDeserializer(TcpSrcCodec.DESERIALIZER_KEY, NiciraMatchCodecs.TCP_SRC_CODEC);
        registrator.registerMatchEntrySerializer(TcpDstCodec.SERIALIZER_KEY, NiciraMatchCodecs.TCP_DST_CODEC);
        registrator.registerMatchEntryDeserializer(TcpDstCodec.DESERIALIZER_KEY, NiciraMatchCodecs.TCP_DST_CODEC);
        registrator.registerMatchEntrySerializer(UdpSrcCodec.SERIALIZER_KEY, NiciraMatchCodecs.UDP_SRC_CODEC);
        registrator.registerMatchEntryDeserializer(UdpSrcCodec.DESERIALIZER_KEY, NiciraMatchCodecs.UDP_SRC_CODEC);
        registrator.registerMatchEntrySerializer(UdpDstCodec.SERIALIZER_KEY, NiciraMatchCodecs.UDP_DST_CODEC);
        registrator.registerMatchEntryDeserializer(UdpDstCodec.DESERIALIZER_KEY, NiciraMatchCodecs.UDP_DST_CODEC);
        registrator.registerMatchEntrySerializer(CtStateCodec.SERIALIZER_KEY, NiciraMatchCodecs.CT_ST_CODEC);
        registrator.registerMatchEntryDeserializer(CtStateCodec.DESERIALIZER_KEY, NiciraMatchCodecs.CT_ST_CODEC);
        registrator.registerMatchEntrySerializer(CtZoneCodec.SERIALIZER_KEY, NiciraMatchCodecs.CT_ZONE_CODEC);
        registrator.registerMatchEntryDeserializer(CtZoneCodec.DESERIALIZER_KEY, NiciraMatchCodecs.CT_ZONE_CODEC);
        registrator.registerMatchEntrySerializer(CtMarkCodec.SERIALIZER_KEY, NiciraMatchCodecs.CT_MARK_CODEC);
        registrator.registerMatchEntryDeserializer(CtMarkCodec.DESERIALIZER_KEY, NiciraMatchCodecs.CT_MARK_CODEC);
        registrator.registerMatchEntrySerializer(CtTpSrcCodec.SERIALIZER_KEY, NiciraMatchCodecs.CT_TP_SRC_CODEC);
        registrator.registerMatchEntryDeserializer(CtTpSrcCodec.DESERIALIZER_KEY, NiciraMatchCodecs.CT_TP_SRC_CODEC);
        registrator.registerMatchEntrySerializer(CtTpDstCodec.SERIALIZER_KEY, NiciraMatchCodecs.CT_TP_DST_CODEC);
        registrator.registerMatchEntryDeserializer(CtTpDstCodec.DESERIALIZER_KEY, NiciraMatchCodecs.CT_TP_DST_CODEC);
        registrator.registerMatchEntrySerializer(PktMarkCodec.SERIALIZER_KEY, NiciraMatchCodecs.PKT_MARK_CODEC);
        registrator.registerMatchEntryDeserializer(PktMarkCodec.DESERIALIZER_KEY, NiciraMatchCodecs.PKT_MARK_CODEC);
    }

    @Deactivate
    @PreDestroy
    @Override
    public void close() {
        registrator.unregisterActionDeserializer(RegLoadCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(RegLoadCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(RegLoad2Codec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(RegLoad2Codec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(RegMoveCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(RegMoveCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(OutputRegCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(OutputRegCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(OutputReg2Codec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(OutputReg2Codec.SERIALIZER_KEY);
        registrator.unregisterActionSerializer(ResubmitCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(ResubmitCodec.TABLE_DESERIALIZER_KEY);
        registrator.unregisterActionDeserializer(ResubmitCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(FinTimeoutCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(FinTimeoutCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(MultipathCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(MultipathCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(ConntrackCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(ConntrackCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(CtClearCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(CtClearCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(LearnCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(LearnCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(EncapCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(EncapCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(DecapCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(DecapCodec.DESERIALIZER_KEY);
        registrator.unregisterActionDeserializer(DecNshTtlCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(DecNshTtlCodec.SERIALIZER_KEY);
        // CONTINUE
        registrator.unregisterMatchEntrySerializer(Reg0Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Reg0Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Reg1Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Reg1Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Reg2Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Reg2Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Reg3Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Reg3Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Reg4Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Reg4Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Reg5Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Reg5Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Reg6Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Reg6Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Reg7Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Reg7Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(TunIdCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(TunIdCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(ArpOpCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(ArpOpCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(ArpShaCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(ArpShaCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(ArpSpaCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(ArpSpaCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(ArpThaCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(ArpThaCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(ArpTpaCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(ArpTpaCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(InPortCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(InPortCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(MplsLabelCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(MplsLabelCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(EthDstCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(EthDstCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(EthSrcCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(EthSrcCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(EthTypeCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(EthTypeCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(NspCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(NspCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(NsiCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(NsiCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Nshc1Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Nshc1Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Nshc2Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Nshc2Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Nshc3Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Nshc3Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Nshc4Codec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Nshc4Codec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(NshFlagsCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(NshFlagsCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(NshTtlCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(NshTtlCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(TunIpv4DstCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(TunIpv4DstCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(TunIpv4SrcCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(TunIpv4SrcCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(NshMdtypeCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(NshMdtypeCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(NshNpCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(NshNpCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(TcpSrcCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(TcpSrcCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(TcpDstCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(TcpDstCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(UdpSrcCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(UdpSrcCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(UdpDstCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(UdpDstCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(CtStateCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(CtStateCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(CtZoneCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(CtZoneCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(CtMarkCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(CtMarkCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(CtTpSrcCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(CtTpSrcCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(CtTpDstCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(CtTpDstCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(PktMarkCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(PktMarkCodec.DESERIALIZER_KEY);
    }
}
