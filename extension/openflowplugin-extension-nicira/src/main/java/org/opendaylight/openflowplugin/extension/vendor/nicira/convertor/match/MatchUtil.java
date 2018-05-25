/**
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.opendaylight.openflowplugin.extension.api.GroupingResolver;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatch;
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

/**
 * Match utilities.
 *
 * @author msunal
 */
public final class MatchUtil {
    private static final Splitter SPLITTER = Splitter.on('.');
    private static final Joiner JOINER = Joiner.on('.');

    private static final Set<Class<? extends Augmentation<Extension>>> AUGMENTATIONS_OF_EXTENSION = new HashSet<>();
    public static final GroupingResolver<NxmNxRegGrouping, Extension> REG_RESOLVER = new GroupingResolver<>(
            NxmNxRegGrouping.class);
    public static final GroupingResolver<NxmNxTunIdGrouping, Extension> TUN_ID_RESOLVER = new GroupingResolver<>(
            NxmNxTunIdGrouping.class);
    public static final GroupingResolver<NxmNxArpShaGrouping, Extension> ARP_SHA_RESOLVER = new GroupingResolver<>(
            NxmNxArpShaGrouping.class);
    public static final GroupingResolver<NxmNxArpThaGrouping, Extension> ARP_THA_RESOLVER = new GroupingResolver<>(
            NxmNxArpThaGrouping.class);
    public static final GroupingResolver<NxmOfArpOpGrouping, Extension> ARP_OP_RESOLVER = new GroupingResolver<>(
            NxmOfArpOpGrouping.class);
    public static final GroupingResolver<NxmOfArpSpaGrouping, Extension> ARP_SPA_RESOLVER = new GroupingResolver<>(
            NxmOfArpSpaGrouping.class);
    public static final GroupingResolver<NxmOfArpTpaGrouping, Extension> ARP_TPA_RESOLVER = new GroupingResolver<>(
            NxmOfArpTpaGrouping.class);
    public static final GroupingResolver<NxmNxTunIpv4DstGrouping, Extension> TUN_IPV4_DST_RESOLVER =
            new GroupingResolver<>(NxmNxTunIpv4DstGrouping.class);
    public static final GroupingResolver<NxmNxTunIpv4SrcGrouping, Extension> TUN_IPV4_SRC_RESOLVER =
            new GroupingResolver<>(NxmNxTunIpv4SrcGrouping.class);
    public static final GroupingResolver<NxmOfEthDstGrouping, Extension> ETH_DST_RESOLVER = new GroupingResolver<>(
            NxmOfEthDstGrouping.class);
    public static final GroupingResolver<NxmOfEthSrcGrouping, Extension> ETH_SRC_RESOLVER = new GroupingResolver<>(
            NxmOfEthSrcGrouping.class);
    public static final GroupingResolver<NxmOfEthTypeGrouping, Extension> ETH_TYPE_RESOLVER = new GroupingResolver<>(
            NxmOfEthTypeGrouping.class);
    public static final GroupingResolver<NxmNxNsiGrouping, Extension> NSI_RESOLVER = new GroupingResolver<>(
            NxmNxNsiGrouping.class);
    public static final GroupingResolver<NxmNxNspGrouping, Extension> NSP_RESOLVER = new GroupingResolver<>(
            NxmNxNspGrouping.class);
    public static final GroupingResolver<NxmNxNshc1Grouping, Extension> NSC1_RESOLVER = new GroupingResolver<>(
            NxmNxNshc1Grouping.class);
    public static final GroupingResolver<NxmNxNshc2Grouping, Extension> NSC2_RESOLVER = new GroupingResolver<>(
            NxmNxNshc2Grouping.class);
    public static final GroupingResolver<NxmNxNshc3Grouping, Extension> NSC3_RESOLVER = new GroupingResolver<>(
            NxmNxNshc3Grouping.class);
    public static final GroupingResolver<NxmNxNshc4Grouping, Extension> NSC4_RESOLVER = new GroupingResolver<>(
            NxmNxNshc4Grouping.class);
    public static final GroupingResolver<NxmNxNshFlagsGrouping, Extension> NSH_FLAGS_RESOLVER =
            new GroupingResolver<>(NxmNxNshFlagsGrouping.class);
    public static final GroupingResolver<NxmNxNshMdtypeGrouping, Extension> NSH_MDTYPE_RESOLVER =
            new GroupingResolver<>(NxmNxNshMdtypeGrouping.class);
    public static final GroupingResolver<NxmNxNshNpGrouping, Extension> NSH_NP_RESOLVER = new GroupingResolver<>(
            NxmNxNshNpGrouping.class);
    public static final GroupingResolver<NxmNxNshTtlGrouping, Extension> NSH_TTL_RESOLVER =
            new GroupingResolver<>(NxmNxNshTtlGrouping.class);
    public static final GroupingResolver<NxmOfTcpSrcGrouping, Extension> TCP_SRC_RESOLVER = new GroupingResolver<>(
            NxmOfTcpSrcGrouping.class);
    public static final GroupingResolver<NxmOfTcpDstGrouping, Extension> TCP_DST_RESOLVER = new GroupingResolver<>(
            NxmOfTcpDstGrouping.class);
    public static final GroupingResolver<NxmOfUdpSrcGrouping, Extension> UDP_SRC_RESOLVER = new GroupingResolver<>(
            NxmOfUdpSrcGrouping.class);
    public static final GroupingResolver<NxmOfUdpDstGrouping, Extension> UDP_DST_RESOLVER = new GroupingResolver<>(
            NxmOfUdpDstGrouping.class);
    public static final GroupingResolver<NxmNxCtMarkGrouping, Extension> CT_MARK_RESOLVER = new GroupingResolver<>(
            NxmNxCtMarkGrouping.class);
    public static final GroupingResolver<NxmNxCtStateGrouping, Extension> CT_STATE_RESOLVER = new GroupingResolver<>(
            NxmNxCtStateGrouping.class);
    public static final GroupingResolver<NxmNxCtZoneGrouping, Extension> CT_ZONE_RESOLVER = new GroupingResolver<>(
            NxmNxCtZoneGrouping.class);
    public static final GroupingResolver<NxmNxCtTpSrcGrouping, Extension> CT_TP_SRC_RESOLVER = new GroupingResolver<>(
            NxmNxCtTpSrcGrouping.class);
    public static final GroupingResolver<NxmNxCtTpDstGrouping, Extension> CT_TP_DST_RESOLVER = new GroupingResolver<>(
            NxmNxCtTpDstGrouping.class);
    public static final GroupingResolver<NxmNxPktMarkGrouping, Extension> PKT_MARK_RESOLVER = new GroupingResolver<>(
            NxmNxPktMarkGrouping.class);
    public static final GroupingResolver<NxmOfInPortGrouping, Extension> NXM_OF_INPORT_RESOLVER =
            new GroupingResolver<>(NxmOfInPortGrouping.class);

    static {
        AUGMENTATIONS_OF_EXTENSION.add(NxAugMatchRpcAddFlow.class);
        AUGMENTATIONS_OF_EXTENSION.add(NxAugMatchRpcRemoveFlow.class);
        AUGMENTATIONS_OF_EXTENSION.add(NxAugMatchRpcUpdateFlowOriginal.class);
        AUGMENTATIONS_OF_EXTENSION.add(NxAugMatchRpcUpdateFlowUpdated.class);
        AUGMENTATIONS_OF_EXTENSION.add(NxAugMatchNodesNodeTableFlow.class);
        AUGMENTATIONS_OF_EXTENSION.add(NxAugMatchNotifSwitchFlowRemoved.class);
        AUGMENTATIONS_OF_EXTENSION.add(NxAugMatchNotifPacketIn.class);
        //augmentationsOfExtension.add(NxAugMatchNotifUpdateFlowStats.class);
        AUGMENTATIONS_OF_EXTENSION.add(NxAugMatchPacketInMessage.class);
        REG_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        TUN_ID_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        ARP_SHA_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        ARP_THA_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        ARP_OP_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        ARP_SPA_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        ARP_TPA_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        TUN_IPV4_DST_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        TUN_IPV4_SRC_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        ETH_DST_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        ETH_SRC_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        ETH_TYPE_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NSP_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NSI_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NSC1_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NSC2_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NSC3_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NSC4_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NSH_FLAGS_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NSH_MDTYPE_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NSH_NP_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NSH_TTL_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        TCP_SRC_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        TCP_DST_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        UDP_SRC_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        UDP_DST_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        CT_STATE_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        CT_ZONE_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        NXM_OF_INPORT_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        CT_MARK_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        CT_TP_SRC_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        CT_TP_DST_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        PKT_MARK_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
    }

    private MatchUtil() {
    }

    public static MatchEntryBuilder createDefaultMatchEntryBuilder(Class<? extends MatchField> matchField,
                                                                   Class<? extends OxmClassBase> oxmClass,
                                                                   MatchEntryValue matchEntryValue) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(matchField);
        matchEntryBuilder.setOxmClass(oxmClass);
        matchEntryBuilder.setMatchEntryValue(matchEntryValue);
        return matchEntryBuilder;
    }

    public static <V extends Augmentation<ExperimenterIdCase>> MatchEntryBuilder createExperimenterMatchEntryBuilder(
            Class<? extends MatchField> matchField,
            long experimenterId,
            NxExpMatchEntryValue value) {
        ExperimenterBuilder experimenterBuilder = new ExperimenterBuilder();
        experimenterBuilder.setExperimenter(new ExperimenterId(experimenterId));
        ExperimenterIdCaseBuilder expCaseBuilder = new ExperimenterIdCaseBuilder();
        expCaseBuilder.setExperimenter(experimenterBuilder.build());
        OfjAugNxExpMatch ofjAugNxExpMatch = new OfjAugNxExpMatchBuilder().setNxExpMatchEntryValue(value).build();
        expCaseBuilder.addAugmentation(OfjAugNxExpMatch.class, ofjAugNxExpMatch);
        return createDefaultMatchEntryBuilder(matchField, ExperimenterClass.class, expCaseBuilder.build());
    }

    public static Long ipv4ToLong(Ipv4Address ipv4) {
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

    public static Ipv4Address longToIpv4Address(Long value) {
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
