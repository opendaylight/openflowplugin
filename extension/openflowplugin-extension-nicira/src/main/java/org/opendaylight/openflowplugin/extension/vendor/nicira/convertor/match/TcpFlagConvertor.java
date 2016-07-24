/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import com.google.common.base.Optional;

import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpFlagGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpFlagKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.tcp.flag.grouping.NxmOfTcpFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.tcp.flag.grouping.NxmOfTcpFlagBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.tcp.flag.grouping.TcpFlagValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TcpFlagCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TcpFlagCaseValueBuilder;

public class TcpFlagConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath> {
    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(MatchEntry input, MatchPath path) {
        TcpFlagCaseValue tcpFlagCaseValue = ((TcpFlagCaseValue) input.getMatchEntryValue());
        NxmOfTcpFlagBuilder tcpFlagBuilder = new NxmOfTcpFlagBuilder();
        tcpFlagBuilder.setTcpFlag(tcpFlagCaseValue.getTcpFlagValues().getTcpFlag());
        tcpFlagBuilder.setMask(tcpFlagCaseValue.getTcpFlagValues().getMask());
        return resolveAugmentation(tcpFlagBuilder.build(), path, NxmOfTcpFlagKey.class);
    }

    public MatchEntry convert(Extension extension) {
        Optional<NxmOfTcpFlagGrouping> matchGrouping = MatchUtil.tcpFlagResolver.getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        TcpFlagCaseValueBuilder tcpFlagCaseValueBuilder = new TcpFlagCaseValueBuilder();
        TcpFlagValuesBuilder tcpFlagValuesBuilder = new TcpFlagValuesBuilder();
        tcpFlagValuesBuilder.setTcpFlag(matchGrouping.get().getNxmOfTcpFlag().getTcpFlag());
        tcpFlagValuesBuilder.setMask(matchGrouping.get().getNxmOfTcpFlag().getMask());
        tcpFlagCaseValueBuilder.setTcpFlagValues(tcpFlagValuesBuilder.build());
        MatchEntryBuilder ofMatch = MatchUtil
                .createDefaultMatchEntryBuilder(org.opendaylight.yang.gen.v1.urn
                                                .opendaylight.openflowjava.nx.match.rev140421.NxmOfTcpFlag.class,
                                                Nxm0Class.class, tcpFlagCaseValueBuilder.build());
        ofMatch.setHasMask(true);
        return ofMatch.build();
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(NxmOfTcpFlag value,
                                                                  MatchPath path, Class<? extends ExtensionKey> key) {
        switch (path) {
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifUpdateFlowStats.class,
                        new NxAugMatchNotifUpdateFlowStatsBuilder().setNxmOfTcpFlag(value).build(), key);
            case PACKETRECEIVED_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class, new NxAugMatchNotifPacketInBuilder()
                         .setNxmOfTcpFlag(value).build(), key);
            case SWITCHFLOWREMOVED_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                         new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmOfTcpFlag(value).build(), key);
            default:
                throw new CodecPreconditionException(path);
        }
    }
}


