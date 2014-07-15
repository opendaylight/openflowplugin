/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.openflowjava.nx.NiciraConstants;
import org.opendaylight.openflowplugin.extension.api.GroupingResolver;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.OfjAugNxMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;
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
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * @author msunal
 *
 */
public class MatchUtil {

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
    public final static ExperimenterIdMatchEntry EXPERIMENTER_ID_MATCH_ENTRY;

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
        ExperimenterIdMatchEntryBuilder experimenterIdMatchEntryBuilder = new ExperimenterIdMatchEntryBuilder();
        experimenterIdMatchEntryBuilder.setExperimenter(new ExperimenterId(NiciraConstants.NX_VENDOR_ID));
        EXPERIMENTER_ID_MATCH_ENTRY = experimenterIdMatchEntryBuilder.build();
    }

    public static MatchEntries createNiciraMatchEntries(Class<? extends OxmClassBase> oxmClass,
            Class<? extends MatchField> oxmMatchField, boolean hasMask, OfjAugNxMatch augNxMatch) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(oxmClass).setOxmMatchField(oxmMatchField).setHasMask(hasMask);
        matchEntriesBuilder.addAugmentation(ExperimenterIdMatchEntry.class, EXPERIMENTER_ID_MATCH_ENTRY);
        matchEntriesBuilder.addAugmentation(OfjAugNxMatch.class, augNxMatch);
        return matchEntriesBuilder.build();
    }

}
