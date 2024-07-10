/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.match.entry.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshFlagsCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshFlagsCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.flags.grouping.NshFlagsValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.flags.grouping.NshFlagsValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchPacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchPacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshFlagsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsh.flags.grouping.NxmNxNshFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsh.flags.grouping.NxmNxNshFlagsBuilder;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint8;

public class NshFlagsConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath> {

    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(final MatchEntry input, final MatchPath path) {
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) input.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NshFlagsCaseValue nshFlagsCaseValue = (NshFlagsCaseValue) ofjAugNxExpMatch.getNxExpMatchEntryValue();
        NshFlagsValues nshFlagsValues = nshFlagsCaseValue.getNshFlagsValues();
        NxmNxNshFlags nxmNxNshFlags = new NxmNxNshFlagsBuilder()
                .setNshFlags(nshFlagsValues.getNshFlags())
                .setMask(nshFlagsValues.getMask())
                .build();
        return resolveAugmentation(nxmNxNshFlags, path, NxmNxNshFlagsKey.VALUE);
    }

    @Override
    public MatchEntry convert(final Extension extension) {
        final var matchGrouping = MatchUtil.NSH_FLAGS_RESOLVER.findExtension(extension);
        if (matchGrouping.isEmpty()) {
            throw new CodecPreconditionException(extension);
        }
        final var nshFlags = matchGrouping.orElseThrow().getNxmNxNshFlags();
        return buildMatchEntry(nshFlags.getNshFlags(), nshFlags.getMask());
    }

    public static MatchEntry buildMatchEntry(final Uint8 flags, final Uint8 mask) {
        NshFlagsValues nshFlagsValues = new NshFlagsValuesBuilder().setNshFlags(flags).setMask(mask).build();
        NxExpMatchEntryValue value = new NshFlagsCaseValueBuilder().setNshFlagsValues(nshFlagsValues).build();
        return MatchUtil.createExperimenterMatchEntryBuilder(
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshFlags.VALUE,
                NiciraConstants.NX_NSH_VENDOR_ID,
                value).setHasMask(mask != null).build();
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(final NxmNxNshFlags value,
            final MatchPath path, final ExtensionKey key) {
        return switch (path) {
            case FLOWS_STATISTICS_UPDATE_MATCH -> new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                new NxAugMatchNodesNodeTableFlowBuilder().setNxmNxNshFlags(value).build(), key);
            case FLOWS_STATISTICS_RPC_MATCH -> new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                new NxAugMatchRpcGetFlowStatsBuilder().setNxmNxNshFlags(value).build(), key);
            case PACKET_RECEIVED_MATCH -> new ExtensionAugment<>(NxAugMatchNotifPacketIn.class,
                new NxAugMatchNotifPacketInBuilder().setNxmNxNshFlags(value).build(), key);
            case SWITCH_FLOW_REMOVED_MATCH -> new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmNxNshFlags(value).build(), key);
            case PACKET_IN_MESSAGE_MATCH -> new ExtensionAugment<>(NxAugMatchPacketInMessage.class,
                new NxAugMatchPacketInMessageBuilder().setNxmNxNshFlags(value).build(), key);
        };
    }
}
