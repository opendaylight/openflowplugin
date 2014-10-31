/*
 * Copyright (C) 2014 Red Hat, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors : Madhu Venugopal
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsp.grouping.NspValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsp.grouping.NspValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNspGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNspKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsp.grouping.NxmNxNsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsp.grouping.NxmNxNspBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import com.google.common.base.Optional;
public class NspConvertor implements ConvertorToOFJava<MatchEntries>, ConvertorFromOFJava<MatchEntries, MatchPath> {
    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(MatchEntries input, MatchPath path) {
        NspValues values = input.getAugmentation(OfjAugNxMatch.class).getNspValues();
        return resolveAugmentation(new NxmNxNspBuilder().setValue(values.getNsp()).build(), path,
                NxmNxNspKey.class);
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(NxmNxNsp value,
            MatchPath path, Class<? extends ExtensionKey> key) {
        switch (path) {
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifUpdateFlowStats.class,
                    new NxAugMatchNotifUpdateFlowStatsBuilder().setNxmNxNsp(value).build(), key);
        case PACKETRECEIVED_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class, new NxAugMatchNotifPacketInBuilder()
                    .setNxmNxNsp(value).build(), key);
        case SWITCHFLOWREMOVED_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                    new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmNxNsp(value).build(), key);
        default:
            throw new CodecPreconditionException(path);
        }
    }

    @Override
    public MatchEntries convert(Extension extension) {
        Optional<NxmNxNspGrouping> matchGrouping = MatchUtil.nspResolver.getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        Long value = matchGrouping.get().getNxmNxNsp().getValue();
        OfjAugNxMatchBuilder augNxMatchBuilder = new OfjAugNxMatchBuilder().setNspValues(new NspValuesBuilder()
                .setNsp(value).build());
        return MatchUtil
                .createNiciraMatchEntries(
                        Nxm1Class.class,
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNsp.class,
                        false, augNxMatchBuilder.build());
    }

}
