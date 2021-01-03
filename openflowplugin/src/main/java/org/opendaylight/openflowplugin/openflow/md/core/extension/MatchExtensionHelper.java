/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import java.util.Collection;
import java.util.HashMap;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.AugmentTuple;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifUpdateFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchPacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchPacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcOutputFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcOutputFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.ExtensionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListKey;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.util.BindingMap.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MatchExtensionHelper {
    private static final Logger LOG = LoggerFactory.getLogger(MatchExtensionHelper.class);

    private MatchExtensionHelper() {
        // Hidden on purpose
    }

    /**
     * Injects an extension.
     *
     * @param matchEntry match entry
     * @param ofVersion openflow version
     * @param matchPath match path
     */
    public static void injectExtension(
            final short ofVersion,
            final MatchEntry matchEntry,
            final MatchBuilder matchBuilder,
            final MatchPath matchPath) {
        final ExtensionListBuilder extBuilder = processExtension(matchEntry, ofVersion, matchPath);
        final GeneralAugMatchNodesNodeTableFlowBuilder builder = flowBuilderFor(matchBuilder);
        if (extBuilder != null) {
            final ExtensionList ext = extBuilder.build();
            builder.getExtensionList().put(ext.key(), ext);
        } else {
            LOG.warn("Convertor for {} for version {} with match path {} not found.",
                    matchEntry, ofVersion, matchPath.name());
        }

        matchBuilder.addAugmentation(builder.build());
    }

    private static GeneralAugMatchNodesNodeTableFlowBuilder flowBuilderFor(final MatchBuilder matchBuilder) {
        final var aug = matchBuilder.augmentation(GeneralAugMatchNodesNodeTableFlow.class);
        return aug != null
            // Reuse existing augmentation
            ? new GeneralAugMatchNodesNodeTableFlowBuilder(aug)
                // Allocate space for extension
                : new GeneralAugMatchNodesNodeTableFlowBuilder().setExtensionList(new HashMap<>());
    }

    /**
     * Processes all extensions.
     *
     * @param matchEntries match entries
     * @param ofVersion openflow version
     * @param matchPath match path
     * @param <E> extension point
     * @return augmentation wrapper containing augmentation depending on matchPath
     */
    @SuppressWarnings("unchecked")
    public static <E extends Augmentable<E>> AugmentTuple<E> processAllExtensions(
            final Collection<MatchEntry> matchEntries, final OpenflowVersion ofVersion, final MatchPath matchPath) {
        if (matchEntries == null) {
            return null;
        }

        final Builder<ExtensionListKey, ExtensionList> extensions = BindingMap.orderedBuilder(matchEntries.size());
        for (MatchEntry matchEntry : matchEntries) {
            final var extensionListBld = processExtension(matchEntry, ofVersion.getVersion(), matchPath);
            if (extensionListBld != null) {
                extensions.add(extensionListBld.build());
            }
        }

        final var extensionsList = extensions.build();
        if (extensionsList.isEmpty()) {
            return null;
        }

        // TODO: use a switch expression when we have JDK14+
        switch (matchPath) {
            case FLOWS_STATISTICS_UPDATE_MATCH:
                return (AugmentTuple<E>) new AugmentTuple<>(GeneralAugMatchNotifUpdateFlowStats.class,
                    new GeneralAugMatchNotifUpdateFlowStatsBuilder().setExtensionList(extensionsList).build());
            case PACKET_RECEIVED_MATCH:
                return (AugmentTuple<E>) new AugmentTuple<>(GeneralAugMatchNotifPacketIn.class,
                    new GeneralAugMatchNotifPacketInBuilder().setExtensionList(extensionsList).build());
            case PACKET_IN_MESSAGE_MATCH:
                return (AugmentTuple<E>) new AugmentTuple<>(GeneralAugMatchPacketInMessage.class,
                    new GeneralAugMatchPacketInMessageBuilder().setExtensionList(extensionsList).build());
            case SWITCH_FLOW_REMOVED_MATCH:
                return (AugmentTuple<E>)new AugmentTuple<>(GeneralAugMatchNotifSwitchFlowRemoved.class,
                    new GeneralAugMatchNotifSwitchFlowRemovedBuilder().setExtensionList(extensionsList).build());
            case FLOWS_STATISTICS_RPC_MATCH:
                return (AugmentTuple<E>) new AugmentTuple<>(GeneralAugMatchRpcOutputFlowStats.class,
                    new GeneralAugMatchRpcOutputFlowStatsBuilder().setExtensionList(extensionsList).build());
            default:
                LOG.warn("matchPath not supported: {}", matchPath);
                return null;
        }
    }

    /**
     * Processes an extension.
     *
     * @param ofVersion openflow version
     * @param matchPath match path
     * @param matchEntry match entry
     * @return an ExtensionListBuilder
     */
    private static ExtensionListBuilder processExtension(final MatchEntry matchEntry, final short ofVersion,
            final MatchPath matchPath) {
        final var convertorProvider = OFSessionUtil.getExtensionConvertorProvider();
        if (convertorProvider == null) {
            return null;
        }

        // TODO: EXTENSION PROPOSAL (match, OFJava to MD-SAL)
        final var key = new MatchEntrySerializerKey<>(ofVersion, matchEntry.getOxmClass(),
            matchEntry.getOxmMatchField());

        // If entry is experimenter, set experimenter ID to key
        if (matchEntry.getOxmClass().equals(ExperimenterClass.class)) {
            key.setExperimenterId(
                ((ExperimenterIdCase) matchEntry.getMatchEntryValue()).getExperimenter().getExperimenter().getValue());
        }

        final var convertor = convertorProvider.getConverter(key);
        if (convertor != null) {
            final var extensionMatch = convertor.convert(matchEntry, matchPath);
            return new ExtensionListBuilder()
                .setExtension(new ExtensionBuilder().addAugmentation(extensionMatch.getAugmentationObject()).build())
                .setExtensionKey(extensionMatch.getKey());
        }
        return null;
    }
}
