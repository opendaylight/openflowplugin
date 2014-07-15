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
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjAugNxMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmNxMatchArpShaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmNxMatchArpThaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmNxMatchTunIdGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmNxMatchTunIpv4DstGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmNxMatchTunIpv4SrcGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmOfMatchArpOpGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmOfMatchArpSpaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmOfMatchArpTpaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmOfMatchEthDstGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmOfMatchEthSrcGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxMatchRegGrouping;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * @author msunal
 *
 */
public class MatchUtil {

    private final static Set<Class<? extends Augmentation<Extension>>> augmentationsOfExtension = new HashSet<>();
    public final static GroupingResolver<NxmNxMatchRegGrouping, Extension> regResolver = new GroupingResolver<>(
            NxmNxMatchRegGrouping.class);
    public final static GroupingResolver<OfjNxmNxMatchTunIdGrouping, Extension> tunIdResolver = new GroupingResolver<>(
            OfjNxmNxMatchTunIdGrouping.class);
    public final static GroupingResolver<OfjNxmNxMatchArpShaGrouping, Extension> arpShaResolver = new GroupingResolver<>(
            OfjNxmNxMatchArpShaGrouping.class);
    public final static GroupingResolver<OfjNxmNxMatchArpThaGrouping, Extension> arpThaResolver = new GroupingResolver<>(
            OfjNxmNxMatchArpThaGrouping.class);
    public final static GroupingResolver<OfjNxmOfMatchArpOpGrouping, Extension> arpOpResolver = new GroupingResolver<>(
            OfjNxmOfMatchArpOpGrouping.class);
    public final static GroupingResolver<OfjNxmOfMatchArpSpaGrouping, Extension> arpSpaResolver = new GroupingResolver<>(
            OfjNxmOfMatchArpSpaGrouping.class);
    public final static GroupingResolver<OfjNxmOfMatchArpTpaGrouping, Extension> arpTpaResolver = new GroupingResolver<>(
            OfjNxmOfMatchArpTpaGrouping.class);
    public final static GroupingResolver<OfjNxmNxMatchTunIpv4DstGrouping, Extension> tunIpv4DstResolver = new GroupingResolver<>(
            OfjNxmNxMatchTunIpv4DstGrouping.class);
    public final static GroupingResolver<OfjNxmNxMatchTunIpv4SrcGrouping, Extension> tunIpv4SrcResolver = new GroupingResolver<>(
            OfjNxmNxMatchTunIpv4SrcGrouping.class);
    public final static GroupingResolver<OfjNxmOfMatchEthDstGrouping, Extension> ethDstResolver = new GroupingResolver<>(
            OfjNxmOfMatchEthDstGrouping.class);
    public final static GroupingResolver<OfjNxmOfMatchEthSrcGrouping, Extension> ethSrcResolver = new GroupingResolver<>(
            OfjNxmOfMatchEthSrcGrouping.class);
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
