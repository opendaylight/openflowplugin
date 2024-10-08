/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.IpConverter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.ArpSpaCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.ArpSpaCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.arp.spa.grouping.ArpSpaValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpSpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.arp.spa.grouping.NxmOfArpSpaBuilder;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link ArpSpaConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArpSpaConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final Ipv4Address IPV4_ADDRESS = Ipv4Address.getDefaultInstance("1.2.3.4");

    private ArpSpaConvertor arpSpaConvertor;

    @Before
    public void setUp() {
        arpSpaConvertor = new ArpSpaConvertor();
    }

    @Test
    public void testConvertToOFJava() {
        final NxmOfArpSpaBuilder nxmOfArpSpaBuilder = new NxmOfArpSpaBuilder()
                .setIpv4Address(IPV4_ADDRESS);
        final NxAugMatchRpcAddFlowBuilder nxAugMatchRpcAddFlowBuilder = new NxAugMatchRpcAddFlowBuilder();
        nxAugMatchRpcAddFlowBuilder.setNxmOfArpSpa(nxmOfArpSpaBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchRpcAddFlowBuilder.build();
        when(extension.augmentation(ArgumentMatchers.any()))
                .thenReturn(extensionAugmentation);

        final MatchEntry converted = arpSpaConvertor.convert(extension);
        Assert.assertEquals(IpConverter.ipv4AddressToLong(IPV4_ADDRESS),
                ((ArpSpaCaseValue) converted.getMatchEntryValue()).getArpSpaValues().getValue().longValue());
    }

    @Test
    public void testConvertFromOFJava() {
        final ArpSpaValuesBuilder arpSpaValuesBuilder = new ArpSpaValuesBuilder()
                .setValue(Uint32.valueOf(IpConverter.ipv4AddressToLong(IPV4_ADDRESS)));
        final ArpSpaCaseValueBuilder arpSpaCaseValueBuilder = new ArpSpaCaseValueBuilder()
                .setArpSpaValues(arpSpaValuesBuilder.build());

        final ArpSpaCaseValue arpSpaCaseValue = arpSpaCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(arpSpaCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = arpSpaConvertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(IPV4_ADDRESS,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmOfArpSpa().getIpv4Address());
        Assert.assertEquals(NxmOfArpSpaKey.VALUE, extensionAugment.getKey());

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = arpSpaConvertor
                .convert(matchEntry, MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(IPV4_ADDRESS, ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                .getNxmOfArpSpa().getIpv4Address());
        Assert.assertEquals(NxmOfArpSpaKey.VALUE, extensionAugment.getKey());

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = arpSpaConvertor
                .convert(matchEntry, MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(IPV4_ADDRESS, ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject())
                .getNxmOfArpSpa().getIpv4Address());
        Assert.assertEquals(NxmOfArpSpaKey.VALUE, extensionAugment.getKey());

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = arpSpaConvertor
                .convert(matchEntry, MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(IPV4_ADDRESS, ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject())
                .getNxmOfArpSpa().getIpv4Address());
        Assert.assertEquals(NxmOfArpSpaKey.VALUE, extensionAugment.getKey());
    }
}
