/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.in.port.type.grouping.NxmOfInPortValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.OfInPortCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.OfInPortCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfInPortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfInPortKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.in.port.grouping.OfInPortBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

public class NxmInPortConvertor implements ConvertorToOFJava<MatchEntry>,
        ConvertorFromOFJava<MatchEntry, MatchPath> {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava#convert
     * (org.opendaylight.yangtools.yang.binding.DataContainer,
     * org.opendaylight.openflowplugin.extension.api.path.AugmentationPath)
     */
    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(
            MatchEntry input, MatchPath path) {
        OfInPortCaseValue inPortCaseValue = ((OfInPortCaseValue) input
                .getMatchEntryValue());
        return NxmInPortConvertor.resolveAugmentation(new OfInPortBuilder()
                .setValue(inPortCaseValue.getNxmOfInPortValues().getValue())
                .build(), path, NxmOfInPortKey.class);
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.in.port.grouping.OfInPort value,
            MatchPath path, Class<? extends ExtensionKey> key) {
        switch (path) {
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
            return new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                    new NxAugMatchNodesNodeTableFlowBuilder().setOfInPort(value).build(), key);
        case RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH:
            return new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                     new NxAugMatchRpcGetFlowStatsBuilder().setOfInPort(value).build(), key);
        case PACKETRECEIVED_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class,
                    new NxAugMatchNotifPacketInBuilder().setOfInPort(value).build(), key);
        case SWITCHFLOWREMOVED_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                    new NxAugMatchNotifSwitchFlowRemovedBuilder().setOfInPort(value).build(), key);
        default:
            throw new CodecPreconditionException(path);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava#convert
     * (org .opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.
     * general .rev140714.general.extension.grouping.Extension)
     */
    @Override
    public MatchEntry convert(Extension extension) {
        Optional<NxmOfInPortGrouping> matchGrouping = MatchUtil.nxmOfInportResolver
                .getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        Integer value = matchGrouping.get().getOfInPort().getValue();
        OfInPortCaseValueBuilder inPortCaseValueBuilder = new OfInPortCaseValueBuilder();
        inPortCaseValueBuilder.setNxmOfInPortValues(
                new NxmOfInPortValuesBuilder().setValue(value).build());
        return MatchUtil
                .createDefaultMatchEntryBuilder(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfInPort.class,
                        Nxm0Class.class, inPortCaseValueBuilder.build())
                .build();

    }
}
