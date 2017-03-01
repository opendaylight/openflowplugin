/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.ArpOpMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.ArpShaMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.ArpSpaMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.ArpThaMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.ArpTpaMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.EthDstMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.EthSrcMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.EthTypeMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Icmpv4CodeMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Icmpv4TypeMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Icmpv6CodeMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Icmpv6TypeMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.InPhyPortMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.InPortMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.IpDscpMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.IpEcnMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.IpProtoMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Ipv4DstMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Ipv4SrcMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Ipv6DstMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Ipv6ExtHdrMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Ipv6FlabelMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Ipv6NdSllMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Ipv6NdTargetMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Ipv6NdTllMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.Ipv6SrcMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.MetadataMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.MplsBosMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.MplsLabelMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.MplsTcMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.PbbIsidMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.SctpDstMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.SctpSrcMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.TcpDstMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.TcpFlagsMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.TcpSrcMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.TunnelIdMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.UdpDstMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.UdpSrcMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.VlanPcpMatchFieldSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield.VlanVidMatchFieldSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;

/**
 * Util class for injecting new multipart match field serializers into OpenflowJava
 */
class MultipartMatchFieldSerializerInjector {

    /**
     * Injects multipart match field serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    static void injectSerializers(final SerializerExtensionProvider provider) {
        // Inject new message serializers here using injector created by createInjector method
        final Function<Class<? extends MatchField>, Consumer<OFSerializer<SetFieldMatch>>> injector =
            createInjector(provider, EncodeConstants.OF13_VERSION_ID);

        injector.apply(ArpOp.class).accept(new ArpOpMatchFieldSerializer());
        injector.apply(ArpSha.class).accept(new ArpShaMatchFieldSerializer());
        injector.apply(ArpSpa.class).accept(new ArpSpaMatchFieldSerializer());
        injector.apply(ArpTha.class).accept(new ArpThaMatchFieldSerializer());
        injector.apply(ArpTpa.class).accept(new ArpTpaMatchFieldSerializer());
        injector.apply(EthDst.class).accept(new EthDstMatchFieldSerializer());
        injector.apply(EthSrc.class).accept(new EthSrcMatchFieldSerializer());
        injector.apply(EthType.class).accept(new EthTypeMatchFieldSerializer());
        injector.apply(Icmpv4Code.class).accept(new Icmpv4CodeMatchFieldSerializer());
        injector.apply(Icmpv4Type.class).accept(new Icmpv4TypeMatchFieldSerializer());
        injector.apply(Icmpv6Code.class).accept(new Icmpv6CodeMatchFieldSerializer());
        injector.apply(Icmpv6Type.class).accept(new Icmpv6TypeMatchFieldSerializer());
        injector.apply(InPhyPort.class).accept(new InPhyPortMatchFieldSerializer());
        injector.apply(InPort.class).accept(new InPortMatchFieldSerializer());
        injector.apply(IpDscp.class).accept(new IpDscpMatchFieldSerializer());
        injector.apply(IpEcn.class).accept(new IpEcnMatchFieldSerializer());
        injector.apply(IpProto.class).accept(new IpProtoMatchFieldSerializer());
        injector.apply(Ipv4Dst.class).accept(new Ipv4DstMatchFieldSerializer());
        injector.apply(Ipv4Src.class).accept(new Ipv4SrcMatchFieldSerializer());
        injector.apply(Ipv6Dst.class).accept(new Ipv6DstMatchFieldSerializer());
        injector.apply(Ipv6Exthdr.class).accept(new Ipv6ExtHdrMatchFieldSerializer());
        injector.apply(Ipv6Flabel.class).accept(new Ipv6FlabelMatchFieldSerializer());
        injector.apply(Ipv6NdSll.class).accept(new Ipv6NdSllMatchFieldSerializer());
        injector.apply(Ipv6NdTarget.class).accept(new Ipv6NdTargetMatchFieldSerializer());
        injector.apply(Ipv6NdTll.class).accept(new Ipv6NdTllMatchFieldSerializer());
        injector.apply(Ipv6Src.class).accept(new Ipv6SrcMatchFieldSerializer());
        injector.apply(Metadata.class).accept(new MetadataMatchFieldSerializer());
        injector.apply(MplsBos.class).accept(new MplsBosMatchFieldSerializer());
        injector.apply(MplsLabel.class).accept(new MplsLabelMatchFieldSerializer());
        injector.apply(MplsTc.class).accept(new MplsTcMatchFieldSerializer());
        injector.apply(PbbIsid.class).accept(new PbbIsidMatchFieldSerializer());
        injector.apply(SctpDst.class).accept(new SctpDstMatchFieldSerializer());
        injector.apply(SctpSrc.class).accept(new SctpSrcMatchFieldSerializer());
        injector.apply(TcpDst.class).accept(new TcpDstMatchFieldSerializer());
        injector.apply(TcpFlags.class).accept(new TcpFlagsMatchFieldSerializer());
        injector.apply(TcpSrc.class).accept(new TcpSrcMatchFieldSerializer());
        injector.apply(TunnelId.class).accept(new TunnelIdMatchFieldSerializer());
        // TODO: Finish implementation of Tunnel Ipv4 src and dst
        injector.apply(TunnelIpv4Dst.class).accept(new Ipv4DstMatchFieldSerializer());
        injector.apply(TunnelIpv4Src.class).accept(new Ipv4SrcMatchFieldSerializer());
        injector.apply(UdpDst.class).accept(new UdpDstMatchFieldSerializer());
        injector.apply(UdpSrc.class).accept(new UdpSrcMatchFieldSerializer());
        injector.apply(VlanPcp.class).accept(new VlanPcpMatchFieldSerializer());
        injector.apply(VlanVid.class).accept(new VlanVidMatchFieldSerializer());
    }

    /**
     * Create injector that will inject new multipart match field features serializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Class<? extends MatchField>, Consumer<OFSerializer<SetFieldMatch>>> createInjector(
        final SerializerExtensionProvider provider,
        final byte version) {
        return type -> serializer ->
            provider.registerSerializer(
                new MessageTypeKey<>(version, type),
                serializer);
    }

}
