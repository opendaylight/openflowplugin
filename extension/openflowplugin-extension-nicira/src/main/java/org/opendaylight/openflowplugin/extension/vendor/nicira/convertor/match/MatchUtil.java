/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
import java.util.Iterator;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.openflowplugin.extension.api.GroupingResolver;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.MatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcUpdateFlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpShaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpThaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtStateGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtZoneGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc1Grouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc2Grouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc3Grouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc4Grouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNsiGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNspGrouping;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpDstGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpSrcGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfUdpDstGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfUdpSrcGrouping;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * @author msunal
 *
 */
public class MatchUtil {
    private static final Splitter SPLITTER = Splitter.on('.');
    private static final Joiner JOINER = Joiner.on('.');

    private final static Set<Class<? extends Augmentation<Extension>>> augmentationsOfExtension = new HashSet<>();
    public final static GroupingResolver<NxmNxRegGrouping, Extension> regResolver = new GroupingResolver<>(
            NxmNxRegGrouping.class);
    public final static GroupingResolver<NxmNxTunIdGrouping, Extension> tunIdResolver = new GroupingResolver<>(
            NxmNxTunIdGrouping.class);
    public final static GroupingResolver<NxmNxArpShaGrouping, Extension> arpShaResolver = new GroupingResolver<>(
            NxmNxArpShaGrouping.class);
    public final static GroupingResolver<NxmNxArpThaGrouping, Extension> arpThaResolver = new GroupingResolver<>(
            NxmNxArpThaGrouping.class);
    public final static GroupingResolver<NxmOfArpOpGrouping, Extension> arpOpResolver = new GroupingResolver<>(
            NxmOfArpOpGrouping.class);
    public final static GroupingResolver<NxmOfArpSpaGrouping, Extension> arpSpaResolver = new GroupingResolver<>(
            NxmOfArpSpaGrouping.class);
    public final static GroupingResolver<NxmOfArpTpaGrouping, Extension> arpTpaResolver = new GroupingResolver<>(
            NxmOfArpTpaGrouping.class);
    public final static GroupingResolver<NxmNxTunIpv4DstGrouping, Extension> tunIpv4DstResolver = new GroupingResolver<>(
            NxmNxTunIpv4DstGrouping.class);
    public final static GroupingResolver<NxmNxTunIpv4SrcGrouping, Extension> tunIpv4SrcResolver = new GroupingResolver<>(
            NxmNxTunIpv4SrcGrouping.class);
    public final static GroupingResolver<NxmOfEthDstGrouping, Extension> ethDstResolver = new GroupingResolver<>(
            NxmOfEthDstGrouping.class);
    public final static GroupingResolver<NxmOfEthSrcGrouping, Extension> ethSrcResolver = new GroupingResolver<>(
            NxmOfEthSrcGrouping.class);
    public final static GroupingResolver<NxmOfEthTypeGrouping, Extension> ethTypeResolver = new GroupingResolver<>(
            NxmOfEthTypeGrouping.class);
    public final static GroupingResolver<NxmNxNsiGrouping, Extension> nsiResolver = new GroupingResolver<>(
            NxmNxNsiGrouping.class);
    public final static GroupingResolver<NxmNxNspGrouping, Extension> nspResolver = new GroupingResolver<>(
            NxmNxNspGrouping.class);
    public final static GroupingResolver<NxmNxNshc1Grouping, Extension> nsc1Resolver = new GroupingResolver<>(
            NxmNxNshc1Grouping.class);
    public final static GroupingResolver<NxmNxNshc2Grouping, Extension> nsc2Resolver = new GroupingResolver<>(
            NxmNxNshc2Grouping.class);
    public final static GroupingResolver<NxmNxNshc3Grouping, Extension> nsc3Resolver = new GroupingResolver<>(
            NxmNxNshc3Grouping.class);
    public final static GroupingResolver<NxmNxNshc4Grouping, Extension> nsc4Resolver = new GroupingResolver<>(
            NxmNxNshc4Grouping.class);
    public final static GroupingResolver<NxmOfTcpSrcGrouping, Extension> tcpSrcResolver = new GroupingResolver<>(
            NxmOfTcpSrcGrouping.class);
    public final static GroupingResolver<NxmOfTcpDstGrouping, Extension> tcpDstResolver = new GroupingResolver<>(
            NxmOfTcpDstGrouping.class);
    public final static GroupingResolver<NxmOfUdpSrcGrouping, Extension> udpSrcResolver = new GroupingResolver<>(
            NxmOfUdpSrcGrouping.class);
    public final static GroupingResolver<NxmOfUdpDstGrouping, Extension> udpDstResolver = new GroupingResolver<>(
            NxmOfUdpDstGrouping.class);
    public final static GroupingResolver<NxmNxCtStateGrouping, Extension> ctStateResolver = new GroupingResolver<>(
            NxmNxCtStateGrouping.class);
    public final static GroupingResolver<NxmNxCtZoneGrouping, Extension> ctZoneResolver = new GroupingResolver<>(
            NxmNxCtZoneGrouping.class);


    static {
        augmentationsOfExtension.add(NxAugMatchRpcAddFlow.class);
        augmentationsOfExtension.add(NxAugMatchRpcRemoveFlow.class);
        augmentationsOfExtension.add(NxAugMatchRpcUpdateFlowOriginal.class);
        augmentationsOfExtension.add(NxAugMatchRpcUpdateFlowUpdated.class);
        augmentationsOfExtension.add(NxAugMatchNodesNodeTableFlow.class);
        augmentationsOfExtension.add(NxAugMatchNotifSwitchFlowRemoved.class);
        augmentationsOfExtension.add(NxAugMatchNotifPacketIn.class);
        augmentationsOfExtension.add(NxAugMatchNotifUpdateFlowStats.class);
        regResolver.setAugmentations(augmentationsOfExtension);
        tunIdResolver.setAugmentations(augmentationsOfExtension);
        arpShaResolver.setAugmentations(augmentationsOfExtension);
        arpThaResolver.setAugmentations(augmentationsOfExtension);
        arpOpResolver.setAugmentations(augmentationsOfExtension);
        arpSpaResolver.setAugmentations(augmentationsOfExtension);
        arpTpaResolver.setAugmentations(augmentationsOfExtension);
        tunIpv4DstResolver.setAugmentations(augmentationsOfExtension);
        tunIpv4SrcResolver.setAugmentations(augmentationsOfExtension);
        ethDstResolver.setAugmentations(augmentationsOfExtension);
        ethSrcResolver.setAugmentations(augmentationsOfExtension);
        ethTypeResolver.setAugmentations(augmentationsOfExtension);
        nspResolver.setAugmentations(augmentationsOfExtension);
        nsiResolver.setAugmentations(augmentationsOfExtension);
        nsc1Resolver.setAugmentations(augmentationsOfExtension);
        nsc2Resolver.setAugmentations(augmentationsOfExtension);
        nsc3Resolver.setAugmentations(augmentationsOfExtension);
        nsc4Resolver.setAugmentations(augmentationsOfExtension);
        tcpSrcResolver.setAugmentations(augmentationsOfExtension);
        tcpDstResolver.setAugmentations(augmentationsOfExtension);
        udpSrcResolver.setAugmentations(augmentationsOfExtension);
        udpDstResolver.setAugmentations(augmentationsOfExtension);
        ctStateResolver.setAugmentations(augmentationsOfExtension);
        ctZoneResolver.setAugmentations(augmentationsOfExtension);

    }

    public static MatchEntryBuilder createDefaultMatchEntryBuilder(Class<? extends MatchField> matchField,
                                                                   Class<? extends OxmClassBase> oxmClass,
                                                                   MatchEntryValue matchEntryValue){
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(matchField);
        matchEntryBuilder.setOxmClass(oxmClass);
        matchEntryBuilder.setMatchEntryValue(matchEntryValue);
        return matchEntryBuilder;
    }

    public static Long ipv4ToLong(Ipv4Address ipv4) {
        Iterator<String> iterator = SPLITTER.split(ipv4.getValue()).iterator();
        byte[] bytes = new byte[8];
        for(int i =0;i < bytes.length;i++) {
            if(i<4) {
                bytes[i] = 0;
            } else {
                bytes[i] = UnsignedBytes.parseUnsignedByte((iterator.next()));
            }
        }
        Long result = Longs.fromByteArray(bytes);
        return result;
    }

    public static Ipv4Address longToIpv4Address(Long l) {
        byte[] bytes = Longs.toByteArray(l);
        String[] strArray = new String[4];
        for(int i = 4;i < bytes.length;i++) {
            strArray[i-4]=UnsignedBytes.toString(bytes[i]);
        }
        String str = JOINER.join(strArray);
        Ipv4Address result = new Ipv4Address(str);
        return result;
    }
}
