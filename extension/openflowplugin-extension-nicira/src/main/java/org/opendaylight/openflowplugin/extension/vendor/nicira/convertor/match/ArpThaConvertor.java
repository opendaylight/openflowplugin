/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.arp.tha.grouping.ArpThaValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpThaCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpThaCaseValueBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpThaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpThaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.arp.tha.grouping.NxmNxArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.arp.tha.grouping.NxmNxArpThaBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * @author msunal
 */
public class ArpThaConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava#convert
     * (org.opendaylight.yangtools.yang.binding.DataContainer,
     * org.opendaylight.openflowplugin.extension.api.path.AugmentationPath)
     */
    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(MatchEntry input, MatchPath path) {
        ArpThaCaseValue arpThaCaseValue = ((ArpThaCaseValue) input.getMatchEntryValue());
        return resolveAugmentation(new NxmNxArpThaBuilder().setMacAddress(arpThaCaseValue.getArpThaValues().getMacAddress()).build(), path,
                NxmNxArpThaKey.class);
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(NxmNxArpTha value,
                                                                                           MatchPath path, Class<? extends ExtensionKey> key) {
        switch (path) {
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
                return new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                        new NxAugMatchNodesNodeTableFlowBuilder().setNxmNxArpTha(value).build(), key);
            case RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH:
                return new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                        new NxAugMatchRpcGetFlowStatsBuilder().setNxmNxArpTha(value).build(), key);
            case PACKETRECEIVED_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class, new NxAugMatchNotifPacketInBuilder()
                        .setNxmNxArpTha(value).build(), key);
            case SWITCHFLOWREMOVED_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                        new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmNxArpTha(value).build(), key);
            case PACKETINMESSAGE_MATCH:
                return new ExtensionAugment<>(NxAugMatchPacketInMessage.class,
                        new NxAugMatchPacketInMessageBuilder().setNxmNxArpTha(value).build(), key);
            default:
                throw new CodecPreconditionException(path);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava#convert
     * (org
     * .opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general
     * .rev140714.general.extension.grouping.Extension)
     */
    @Override
    public MatchEntry convert(Extension extension) {
        Optional<NxmNxArpThaGrouping> matchGrouping = MatchUtil.arpThaResolver.getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        MacAddress macAddress = matchGrouping.get().getNxmNxArpTha().getMacAddress();
        ArpThaCaseValueBuilder arpThaCaseValueBuilder = new ArpThaCaseValueBuilder();
        arpThaCaseValueBuilder.setArpThaValues(new ArpThaValuesBuilder()
                .setMacAddress(macAddress).build());
        return MatchUtil.createDefaultMatchEntryBuilder(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxArpTha.class,
                Nxm1Class.class,
                arpThaCaseValueBuilder.build()).build();

    }

}
