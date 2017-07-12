/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.AugmentTuple;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifUpdateFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcOutputFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcOutputFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.ExtensionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class MatchExtensionHelper {

    private static final Logger LOG = LoggerFactory
            .getLogger(MatchExtensionHelper.class);

    private MatchExtensionHelper() {
        throw new IllegalAccessError("singleton enforcement");
    }


    /**
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

        if (Objects.isNull(extBuilder)) {
            LOG.warn("Convertor for {} for version {} with match path {} not found.",
                    matchEntry.toString(),
                    ofVersion,
                    matchPath.name());
        }

        final GeneralAugMatchNodesNodeTableFlowBuilder builder = Optional
                .ofNullable(matchBuilder.getAugmentation(GeneralAugMatchNodesNodeTableFlow.class))
                .map(GeneralAugMatchNodesNodeTableFlowBuilder::new)
                .orElse(new GeneralAugMatchNodesNodeTableFlowBuilder().setExtensionList(new ArrayList<>()));

        builder.getExtensionList().add(extBuilder.build());
        matchBuilder.addAugmentation(GeneralAugMatchNodesNodeTableFlow.class, builder.build());
    }

    /**
     * @param matchEntries match entries
     * @param ofVersion openflow version
     * @param matchPath match path
     * @param <EXT_POINT> extension point
     * @return augmentation wrapper containing augmentation depending on matchPath
     */
    @SuppressWarnings("unchecked")
    public static <EXT_POINT extends Augmentable<EXT_POINT>>
    AugmentTuple<EXT_POINT> processAllExtensions(Collection<MatchEntry> matchEntries,
                                                 OpenflowVersion ofVersion, MatchPath matchPath) {
        List<ExtensionList> extensionsList = new ArrayList<>();

        for (MatchEntry matchEntry : matchEntries) {
            ExtensionListBuilder extensionListBld = processExtension(matchEntry, ofVersion.getVersion(), matchPath);
            if (extensionListBld == null) {
                continue;
            }

            extensionsList.add(extensionListBld.build());
        }

        AugmentTuple<EXT_POINT> augmentTuple = null;
        if (!extensionsList.isEmpty()) {
            switch (matchPath) {
                case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
                    GeneralAugMatchNotifUpdateFlowStatsBuilder generalExtMatchAugBld1 = new GeneralAugMatchNotifUpdateFlowStatsBuilder();
                    generalExtMatchAugBld1.setExtensionList(extensionsList);
                    augmentTuple = (AugmentTuple<EXT_POINT>)
                            new AugmentTuple<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match>(
                                    GeneralAugMatchNotifUpdateFlowStats.class, generalExtMatchAugBld1.build());
                    break;
                case PACKETRECEIVED_MATCH:
                    GeneralAugMatchNotifPacketInBuilder generalExtMatchAugBld2 = new GeneralAugMatchNotifPacketInBuilder();
                    generalExtMatchAugBld2.setExtensionList(extensionsList);
                    augmentTuple = (AugmentTuple<EXT_POINT>)
                            new AugmentTuple<Match>(GeneralAugMatchNotifPacketIn.class, generalExtMatchAugBld2.build());
                    break;
                case SWITCHFLOWREMOVED_MATCH:
                    GeneralAugMatchNotifSwitchFlowRemovedBuilder generalExtMatchAugBld3 = new GeneralAugMatchNotifSwitchFlowRemovedBuilder();
                    generalExtMatchAugBld3.setExtensionList(extensionsList);
                    augmentTuple = (AugmentTuple<EXT_POINT>)
                            new AugmentTuple<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.mod.removed.Match>(
                                    GeneralAugMatchNotifSwitchFlowRemoved.class, generalExtMatchAugBld3.build());
                    break;
                case RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH:
                    GeneralAugMatchRpcOutputFlowStatsBuilder generalExtMatchAugBld4 = new GeneralAugMatchRpcOutputFlowStatsBuilder();
                    generalExtMatchAugBld4.setExtensionList(extensionsList);
                    augmentTuple = (AugmentTuple<EXT_POINT>)
                            new AugmentTuple<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match>(
                                    GeneralAugMatchRpcOutputFlowStats.class, generalExtMatchAugBld4.build());
                    break;
                default:
                    LOG.warn("matchPath not supported: {}", matchPath);
            }
        }

        return augmentTuple;
    }

    /**
     * @param ofVersion
     * @param matchPath
     * @param matchEntry
     * @return
     */
    private static ExtensionListBuilder processExtension(MatchEntry matchEntry, short ofVersion, MatchPath matchPath) {
        ExtensionListBuilder extListBld = null;

        /** TODO: EXTENSION PROPOSAL (match, OFJava to MD-SAL) */
        MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key = new MatchEntrySerializerKey<>(
                ofVersion, matchEntry.getOxmClass(), matchEntry.getOxmMatchField());
        if (null != OFSessionUtil.getExtensionConvertorProvider()) {
            ConvertorFromOFJava<MatchEntry, MatchPath> convertor = OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
            if (convertor != null) {
                ExtensionAugment<? extends Augmentation<Extension>> extensionMatch =
                        convertor.convert(matchEntry, matchPath);
                ExtensionBuilder extBld = new ExtensionBuilder();
                extBld.addAugmentation(extensionMatch.getAugmentationClass(), extensionMatch.getAugmentationObject());

                extListBld = new ExtensionListBuilder();
                extListBld.setExtension(extBld.build());
                extListBld.setExtensionKey(extensionMatch.getKey());
            }
        }
        return extListBld;
    }

}
