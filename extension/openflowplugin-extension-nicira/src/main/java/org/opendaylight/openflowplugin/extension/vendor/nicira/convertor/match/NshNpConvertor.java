/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.np.grouping.NshNpValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.NshNpCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.NshNpCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshNpGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshNpKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsh.np.grouping.NxmNxNshNp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsh.np.grouping.NxmNxNshNpBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

public class NshNpConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath> {
    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(MatchEntry input, MatchPath path) {
        NshNpCaseValue nshNpCaseValue = ((NshNpCaseValue) input.getMatchEntryValue());

        return resolveAugmentation(new NxmNxNshNpBuilder().setValue(nshNpCaseValue.getNshNpValues().getValue()).build(), path,
                NxmNxNshNpKey.class);
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(NxmNxNshNp value,
                                                                                           MatchPath path, Class<? extends ExtensionKey> key) {
        switch (path) {
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifUpdateFlowStats.class,
                        new NxAugMatchNotifUpdateFlowStatsBuilder().setNxmNxNshNp(value).build(), key);
            case PACKETRECEIVED_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class, new NxAugMatchNotifPacketInBuilder()
                        .setNxmNxNshNp(value).build(), key);
            case SWITCHFLOWREMOVED_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                        new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmNxNshNp(value).build(), key);
            default:
                throw new CodecPreconditionException(path);
        }
    }

    @Override
    public MatchEntry convert(Extension extension) {
        Optional<NxmNxNshNpGrouping> matchGrouping = MatchUtil.nshNpResolver.getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        Short value = matchGrouping.get().getNxmNxNshNp().getValue();
        NshNpCaseValueBuilder nshNpCaseValueBuilder = new NshNpCaseValueBuilder();
        nshNpCaseValueBuilder.setNshNpValues(new NshNpValuesBuilder()
                .setValue(value).build());


        return MatchUtil.createDefaultMatchEntryBuilder(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshNp.class,
                Nxm1Class.class,
                nshNpCaseValueBuilder.build()).build();
    }

}
