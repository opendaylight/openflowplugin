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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.arp.tpa.grouping.ArpTpaValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpTpaCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpTpaCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpTpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.arp.tpa.grouping.NxmOfArpTpaBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link ArpTpaConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArpTpaConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final Ipv4Address IPV4_ADDRESS = Ipv4Address.getDefaultInstance("1.2.3.4");

    private ArpTpaConvertor arpTpaConvertor;

    @Before
    public void setUp() {

        final NxmOfArpTpaBuilder nxmOfArpTpaBuilder = new NxmOfArpTpaBuilder()
                .setIpv4Address(IPV4_ADDRESS);
        final NxAugMatchRpcAddFlowBuilder nxAugMatchRpcAddFlowBuilder = new NxAugMatchRpcAddFlowBuilder();
        nxAugMatchRpcAddFlowBuilder.setNxmOfArpTpa(nxmOfArpTpaBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchRpcAddFlowBuilder.build();
        when(extension.augmentation(ArgumentMatchers.any()))
            .thenReturn(extensionAugmentation);

        arpTpaConvertor = new ArpTpaConvertor();
    }

    @Test
    public void testConvertFromOFJava() {
        final MatchEntry converted = arpTpaConvertor.convert(extension);
        Assert.assertEquals(IpConverter.ipv4AddressToLong(IPV4_ADDRESS),
                ((ArpTpaCaseValue)converted.getMatchEntryValue()).getArpTpaValues().getValue().longValue());
    }

    @Test
    public void testConvertToOFJava() {
        final ArpTpaValuesBuilder arpTpaValuesBuilder = new ArpTpaValuesBuilder()
                .setValue(Uint32.valueOf(IpConverter.ipv4AddressToLong(IPV4_ADDRESS)));
        final ArpTpaCaseValueBuilder arpTpaCaseValueBuilder = new ArpTpaCaseValueBuilder()
                .setArpTpaValues(arpTpaValuesBuilder.build());

        final ArpTpaCaseValue arpTpaCaseValue = arpTpaCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(arpTpaCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = arpTpaConvertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(IPV4_ADDRESS,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmOfArpTpa().getIpv4Address());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfArpTpaKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = arpTpaConvertor
                .convert(matchEntry, MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(IPV4_ADDRESS, ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                .getNxmOfArpTpa().getIpv4Address());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfArpTpaKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = arpTpaConvertor
                .convert(matchEntry, MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(IPV4_ADDRESS, ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject())
                .getNxmOfArpTpa().getIpv4Address());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfArpTpaKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = arpTpaConvertor
                .convert(matchEntry, MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(IPV4_ADDRESS, ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject())
                .getNxmOfArpTpa().getIpv4Address());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfArpTpaKey.class);
    }
}
