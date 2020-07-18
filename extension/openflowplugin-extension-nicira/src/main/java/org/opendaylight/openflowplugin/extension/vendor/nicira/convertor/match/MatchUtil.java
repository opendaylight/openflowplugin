/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;
import java.util.Iterator;
import org.opendaylight.openflowplugin.extension.api.AugmentationGroupingResolver;
import org.opendaylight.openflowplugin.extension.api.AugmentationGroupingResolver.Factory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.MatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchPacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcUpdateFlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpShaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpThaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtMarkGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtStateGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtTpDstGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtTpSrcGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtZoneGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshFlagsGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshMdtypeGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshNpGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshTtlGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc1Grouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc2Grouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc3Grouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc4Grouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNsiGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNspGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxPktMarkGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxRegGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIdGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4DstGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4SrcGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpOpGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpSpaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpTpaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthDstGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthSrcGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthTypeGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfInPortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpDstGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpSrcGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfUdpDstGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfUdpSrcGrouping;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Match utilities.
 *
 * @author msunal
 */
public final class MatchUtil {
    private static final Splitter SPLITTER = Splitter.on('.');
    private static final Joiner JOINER = Joiner.on('.');

    public static final AugmentationGroupingResolver<NxmNxRegGrouping, Extension> REG_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxTunIdGrouping, Extension> TUN_ID_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxArpShaGrouping, Extension> ARP_SHA_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxArpThaGrouping, Extension> ARP_THA_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfArpOpGrouping, Extension> ARP_OP_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfArpSpaGrouping, Extension> ARP_SPA_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfArpTpaGrouping, Extension> ARP_TPA_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxTunIpv4DstGrouping, Extension> TUN_IPV4_DST_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxTunIpv4SrcGrouping, Extension> TUN_IPV4_SRC_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfEthDstGrouping, Extension> ETH_DST_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfEthSrcGrouping, Extension> ETH_SRC_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfEthTypeGrouping, Extension> ETH_TYPE_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxNsiGrouping, Extension> NSI_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxNspGrouping, Extension> NSP_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxNshc1Grouping, Extension> NSC1_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxNshc2Grouping, Extension> NSC2_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxNshc3Grouping, Extension> NSC3_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxNshc4Grouping, Extension> NSC4_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxNshFlagsGrouping, Extension> NSH_FLAGS_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxNshMdtypeGrouping, Extension> NSH_MDTYPE_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxNshNpGrouping, Extension> NSH_NP_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxNshTtlGrouping, Extension> NSH_TTL_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfTcpSrcGrouping, Extension> TCP_SRC_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfTcpDstGrouping, Extension> TCP_DST_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfUdpSrcGrouping, Extension> UDP_SRC_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfUdpDstGrouping, Extension> UDP_DST_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxCtMarkGrouping, Extension> CT_MARK_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxCtStateGrouping, Extension> CT_STATE_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxCtZoneGrouping, Extension> CT_ZONE_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxCtTpSrcGrouping, Extension> CT_TP_SRC_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxCtTpDstGrouping, Extension> CT_TP_DST_RESOLVER;
    public static final AugmentationGroupingResolver<NxmNxPktMarkGrouping, Extension> PKT_MARK_RESOLVER;
    public static final AugmentationGroupingResolver<NxmOfInPortGrouping, Extension> NXM_OF_INPORT_RESOLVER;

    static {
        final Factory<Extension> factory = AugmentationGroupingResolver.factory(Extension.class, ImmutableSet.of(
            NxAugMatchRpcAddFlow.class,
            NxAugMatchRpcRemoveFlow.class,
            NxAugMatchRpcUpdateFlowOriginal.class,
            NxAugMatchRpcUpdateFlowUpdated.class,
            NxAugMatchNodesNodeTableFlow.class,
            NxAugMatchNotifSwitchFlowRemoved.class,
            NxAugMatchNotifPacketIn.class,
            // NxAugMatchNotifUpdateFlowStats.class,
            NxAugMatchPacketInMessage.class));

        REG_RESOLVER = factory.createResolver(NxmNxRegGrouping.class);
        TUN_ID_RESOLVER = factory.createResolver(NxmNxTunIdGrouping.class);
        ARP_SHA_RESOLVER = factory.createResolver(NxmNxArpShaGrouping.class);
        ARP_THA_RESOLVER = factory.createResolver(NxmNxArpThaGrouping.class);
        ARP_OP_RESOLVER = factory.createResolver(NxmOfArpOpGrouping.class);
        ARP_SPA_RESOLVER = factory.createResolver(NxmOfArpSpaGrouping.class);
        ARP_TPA_RESOLVER = factory.createResolver(NxmOfArpTpaGrouping.class);
        TUN_IPV4_DST_RESOLVER = factory.createResolver(NxmNxTunIpv4DstGrouping.class);
        TUN_IPV4_SRC_RESOLVER = factory.createResolver(NxmNxTunIpv4SrcGrouping.class);
        ETH_DST_RESOLVER = factory.createResolver(NxmOfEthDstGrouping.class);
        ETH_SRC_RESOLVER = factory.createResolver(NxmOfEthSrcGrouping.class);
        ETH_TYPE_RESOLVER = factory.createResolver(NxmOfEthTypeGrouping.class);
        NSP_RESOLVER = factory.createResolver(NxmNxNspGrouping.class);
        NSI_RESOLVER = factory.createResolver(NxmNxNsiGrouping.class);
        NSC1_RESOLVER = factory.createResolver(NxmNxNshc1Grouping.class);
        NSC2_RESOLVER = factory.createResolver(NxmNxNshc2Grouping.class);
        NSC3_RESOLVER = factory.createResolver(NxmNxNshc3Grouping.class);
        NSC4_RESOLVER = factory.createResolver(NxmNxNshc4Grouping.class);
        NSH_FLAGS_RESOLVER = factory.createResolver(NxmNxNshFlagsGrouping.class);
        NSH_MDTYPE_RESOLVER = factory.createResolver(NxmNxNshMdtypeGrouping.class);
        NSH_NP_RESOLVER = factory.createResolver(NxmNxNshNpGrouping.class);
        NSH_TTL_RESOLVER = factory.createResolver(NxmNxNshTtlGrouping.class);
        TCP_SRC_RESOLVER = factory.createResolver(NxmOfTcpSrcGrouping.class);
        TCP_DST_RESOLVER = factory.createResolver(NxmOfTcpDstGrouping.class);
        UDP_SRC_RESOLVER = factory.createResolver(NxmOfUdpSrcGrouping.class);
        UDP_DST_RESOLVER = factory.createResolver(NxmOfUdpDstGrouping.class);
        CT_STATE_RESOLVER = factory.createResolver(NxmNxCtStateGrouping.class);
        CT_ZONE_RESOLVER = factory.createResolver(NxmNxCtZoneGrouping.class);
        NXM_OF_INPORT_RESOLVER = factory.createResolver(NxmOfInPortGrouping.class);
        CT_MARK_RESOLVER = factory.createResolver(NxmNxCtMarkGrouping.class);
        CT_TP_SRC_RESOLVER = factory.createResolver(NxmNxCtTpSrcGrouping.class);
        CT_TP_DST_RESOLVER = factory.createResolver(NxmNxCtTpDstGrouping.class);
        PKT_MARK_RESOLVER = factory.createResolver(NxmNxPktMarkGrouping.class);
    }

    private MatchUtil() {
    }

    public static MatchEntryBuilder createDefaultMatchEntryBuilder(final Class<? extends MatchField> matchField,
                                                                   final Class<? extends OxmClassBase> oxmClass,
                                                                   final MatchEntryValue matchEntryValue) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(matchField);
        matchEntryBuilder.setOxmClass(oxmClass);
        matchEntryBuilder.setMatchEntryValue(matchEntryValue);
        return matchEntryBuilder;
    }

    public static <V extends Augmentation<ExperimenterIdCase>> MatchEntryBuilder createExperimenterMatchEntryBuilder(
            final Class<? extends MatchField> matchField,
            final Uint32 experimenterId,
            final NxExpMatchEntryValue value) {
        return createDefaultMatchEntryBuilder(matchField, ExperimenterClass.class, new ExperimenterIdCaseBuilder()
            .setExperimenter(new ExperimenterBuilder().setExperimenter(new ExperimenterId(experimenterId)).build())
            .addAugmentation(new OfjAugNxExpMatchBuilder().setNxExpMatchEntryValue(value).build())
            .build());
    }

    public static Long ipv4ToLong(final Ipv4Address ipv4) {
        Iterator<String> iterator = SPLITTER.split(ipv4.getValue()).iterator();
        byte[] bytes = new byte[8];
        for (int i = 0; i < bytes.length; i++) {
            if (i < 4) {
                bytes[i] = 0;
            } else {
                bytes[i] = UnsignedBytes.parseUnsignedByte(iterator.next());
            }
        }
        Long result = Longs.fromByteArray(bytes);
        return result;
    }

    public static Ipv4Address longToIpv4Address(final Uint32 value) {
        return longToIpv4Address(value.toJava());
    }

    public static Ipv4Address longToIpv4Address(final Long value) {
        return longToIpv4Address(value.longValue());
    }

    public static Ipv4Address longToIpv4Address(final long value) {
        byte[] bytes = Longs.toByteArray(value);
        String[] strArray = new String[4];
        for (int i = 4; i < bytes.length; i++) {
            strArray[i - 4] = UnsignedBytes.toString(bytes[i]);
        }
        String str = JOINER.join(strArray);
        Ipv4Address result = new Ipv4Address(str);
        return result;
    }
}
