/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.IpConverter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.OfjAugNxMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.OfjAugNxMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.ofj.nxm.of.match.arp.tpa.grouping.ArpTpaValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.ofj.nxm.of.match.arp.tpa.grouping.ArpTpaValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpTpaGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpTpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.arp.tpa.grouping.NxmOfArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.arp.tpa.grouping.NxmOfArpTpaBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import com.google.common.base.Optional;

/**
 * @author msunal
 *
 */
public class ArpTpaConvertor implements ConvertorToOFJava<MatchEntries>, ConvertorFromOFJava<MatchEntries, MatchPath> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava#convert
     * (org.opendaylight.yangtools.yang.binding.DataContainer,
     * org.opendaylight.openflowplugin.extension.api.path.AugmentationPath)
     */
    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(MatchEntries input, MatchPath path) {
        ArpTpaValues values = input.getAugmentation(OfjAugNxMatch.class).getArpTpaValues();
        Ipv4Address ipv4Address = IpConverter.longToIpv4Address(values.getValue());
        return resolveAugmentation(new NxmOfArpTpaBuilder().setIpv4Address(ipv4Address).build(), path,
                NxmOfArpTpaKey.class);
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(NxmOfArpTpa value,
            MatchPath path, Class<? extends ExtensionKey> key) {
        switch (path) {
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifUpdateFlowStats.class,
                    new NxAugMatchNotifUpdateFlowStatsBuilder().setNxmOfArpTpa(value).build(), key);
        case PACKETRECEIVED_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class, new NxAugMatchNotifPacketInBuilder()
                    .setNxmOfArpTpa(value).build(), key);
        case SWITCHFLOWREMOVED_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                    new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmOfArpTpa(value).build(), key);
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
    public MatchEntries convert(Extension extension) {
        Optional<NxmOfArpTpaGrouping> matchGrouping = MatchUtil.arpTpaResolver.getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        Long value = IpConverter.Ipv4AddressToLong(matchGrouping.get().getNxmOfArpTpa().getIpv4Address());
        OfjAugNxMatchBuilder augNxMatchBuilder = new OfjAugNxMatchBuilder().setArpTpaValues(new ArpTpaValuesBuilder()
                .setValue(value).build());
        return MatchUtil
                .createNiciraMatchEntries(
                        Nxm0Class.class,
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.extension.nicira.match.rev140421.NxmOfArpTpa.class,
                        false, augNxMatchBuilder.build());
    }

}
