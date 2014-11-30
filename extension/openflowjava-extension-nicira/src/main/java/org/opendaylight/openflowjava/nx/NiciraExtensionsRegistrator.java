package org.opendaylight.openflowjava.nx;

import org.opendaylight.openflowjava.nx.api.NiciraExtensionCodecRegistrator;
import org.opendaylight.openflowjava.nx.codec.action.NiciraActionCodecs;
import org.opendaylight.openflowjava.nx.codec.action.OutputRegCodec;
import org.opendaylight.openflowjava.nx.codec.action.RegLoadCodec;
import org.opendaylight.openflowjava.nx.codec.action.RegMoveCodec;
import org.opendaylight.openflowjava.nx.codec.action.ResubmitCodec;
import org.opendaylight.openflowjava.nx.codec.action.MultipathCodec;
import org.opendaylight.openflowjava.nx.codec.action.SetNspCodec;
import org.opendaylight.openflowjava.nx.codec.action.SetNsiCodec;
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
import org.opendaylight.openflowjava.nx.codec.match.NspCodec;
import org.opendaylight.openflowjava.nx.codec.match.NsiCodec;

import com.google.common.base.Preconditions;

public class NiciraExtensionsRegistrator implements AutoCloseable {

    private final NiciraExtensionCodecRegistrator registrator;

    /**
     * @param providers cannot be null
     */
    public NiciraExtensionsRegistrator(NiciraExtensionCodecRegistrator registrator) {
        Preconditions.checkNotNull(registrator);
        this.registrator = registrator;
    }

    public void registerNiciraExtensions() {
        registrator.registerActionDeserializer(RegLoadCodec.DESERIALIZER_KEY, NiciraActionCodecs.REG_LOAD_CODEC);
        registrator.registerActionSerializer(RegLoadCodec.SERIALIZER_KEY, NiciraActionCodecs.REG_LOAD_CODEC);
        registrator.registerActionDeserializer(RegMoveCodec.DESERIALIZER_KEY, NiciraActionCodecs.REG_MOVE_CODEC);
        registrator.registerActionSerializer(RegMoveCodec.SERIALIZER_KEY, NiciraActionCodecs.REG_MOVE_CODEC);
        registrator.registerActionDeserializer(OutputRegCodec.DESERIALIZER_KEY, NiciraActionCodecs.OUTPUT_REG_CODEC);
        registrator.registerActionSerializer(OutputRegCodec.SERIALIZER_KEY, NiciraActionCodecs.OUTPUT_REG_CODEC);
        registrator.registerActionSerializer(ResubmitCodec.SERIALIZER_KEY, NiciraActionCodecs.RESUBMIT_CODEC);
        registrator.registerActionDeserializer(ResubmitCodec.DESERIALIZER_KEY, NiciraActionCodecs.RESUBMIT_CODEC);
        registrator.registerActionDeserializer(ResubmitCodec.TABLE_DESERIALIZER_KEY, NiciraActionCodecs.RESUBMIT_CODEC);
        registrator.registerActionSerializer(MultipathCodec.SERIALIZER_KEY, NiciraActionCodecs.MULTIPATH_CODEC);
        registrator.registerActionDeserializer(MultipathCodec.DESERIALIZER_KEY, NiciraActionCodecs.MULTIPATH_CODEC);
        registrator.registerActionDeserializer(SetNspCodec.DESERIALIZER_KEY, NiciraActionCodecs.SET_NSP_CODEC);
        registrator.registerActionSerializer(SetNspCodec.SERIALIZER_KEY, NiciraActionCodecs.SET_NSP_CODEC);
        registrator.registerActionDeserializer(SetNsiCodec.DESERIALIZER_KEY, NiciraActionCodecs.SET_NSI_CODEC);
        registrator.registerActionSerializer(SetNsiCodec.SERIALIZER_KEY, NiciraActionCodecs.SET_NSI_CODEC);

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
        registrator.registerMatchEntrySerializer(EthDstCodec.SERIALIZER_KEY, NiciraMatchCodecs.ETH_DST_CODEC);
        registrator.registerMatchEntryDeserializer(EthDstCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ETH_DST_CODEC);
        registrator.registerMatchEntrySerializer(EthSrcCodec.SERIALIZER_KEY, NiciraMatchCodecs.ETH_SRC_CODEC);
        registrator.registerMatchEntryDeserializer(EthSrcCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ETH_SRC_CODEC);
        registrator.registerMatchEntrySerializer(TunIpv4DstCodec.SERIALIZER_KEY, NiciraMatchCodecs.TUN_IPV4_DST_CODEC);
        registrator.registerMatchEntryDeserializer(TunIpv4DstCodec.DESERIALIZER_KEY, NiciraMatchCodecs.TUN_IPV4_DST_CODEC);
        registrator.registerMatchEntrySerializer(TunIpv4SrcCodec.SERIALIZER_KEY, NiciraMatchCodecs.TUN_IPV4_SRC_CODEC);
        registrator.registerMatchEntryDeserializer(TunIpv4SrcCodec.DESERIALIZER_KEY, NiciraMatchCodecs.TUN_IPV4_SRC_CODEC);
        registrator.registerMatchEntrySerializer(EthTypeCodec.SERIALIZER_KEY, NiciraMatchCodecs.ETH_TYPE_CODEC);
        registrator.registerMatchEntryDeserializer(EthTypeCodec.DESERIALIZER_KEY, NiciraMatchCodecs.ETH_TYPE_CODEC);
        registrator.registerMatchEntrySerializer(NspCodec.SERIALIZER_KEY, NiciraMatchCodecs.NSP_CODEC);
        registrator.registerMatchEntryDeserializer(NspCodec.DESERIALIZER_KEY, NiciraMatchCodecs.NSP_CODEC);
        registrator.registerMatchEntrySerializer(NsiCodec.SERIALIZER_KEY, NiciraMatchCodecs.NSI_CODEC);
        registrator.registerMatchEntryDeserializer(NsiCodec.DESERIALIZER_KEY, NiciraMatchCodecs.NSI_CODEC);
    }

    public void unregisterExtensions() {
        registrator.unregisterActionDeserializer(RegLoadCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(RegLoadCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(RegMoveCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(RegMoveCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(OutputRegCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(OutputRegCodec.SERIALIZER_KEY);
        registrator.unregisterActionSerializer(ResubmitCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(ResubmitCodec.TABLE_DESERIALIZER_KEY);
        registrator.unregisterActionDeserializer(ResubmitCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(MultipathCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(MultipathCodec.DESERIALIZER_KEY);
        registrator.unregisterActionDeserializer(SetNsiCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(SetNsiCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(SetNspCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(SetNspCodec.SERIALIZER_KEY);

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
        registrator.unregisterMatchEntrySerializer(EthDstCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(EthDstCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(EthSrcCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(EthSrcCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(TunIpv4DstCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(TunIpv4DstCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(TunIpv4SrcCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(TunIpv4SrcCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(EthTypeCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(EthTypeCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(NspCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(NspCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(NsiCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(NsiCodec.DESERIALIZER_KEY);
    }

    @Override
    public void close() throws Exception {
        unregisterExtensions();
    }

}
