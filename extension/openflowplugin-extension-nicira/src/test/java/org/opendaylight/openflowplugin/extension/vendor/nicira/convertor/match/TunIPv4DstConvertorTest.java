/**
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
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.IpConverter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.tun.ipv4.dst.grouping.TunIpv4DstValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4DstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4DstCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4DstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.tun.ipv4.dst.grouping.NxmNxTunIpv4DstBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link TunIPv4DstConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TunIPv4DstConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final Ipv4Address IPV4_ADDRESS = Ipv4Address.getDefaultInstance("1.2.3.4");

    private TunIPv4DstConvertor tunIPv4DstConvertor;

    @Before
    public void setUp() throws Exception {
        final NxmNxTunIpv4DstBuilder nxmNxTunIpv4DstBuilder = new NxmNxTunIpv4DstBuilder()
                .setIpv4Address(IPV4_ADDRESS);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder =
                new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmNxTunIpv4Dst(nxmNxTunIpv4DstBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.augmentation(ArgumentMatchers.<Class<Augmentation<Extension>>>any()))
            .thenReturn(extensionAugmentation);

        tunIPv4DstConvertor = new TunIPv4DstConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry converted = tunIPv4DstConvertor.convert(extension);
        Assert.assertEquals(IpConverter.ipv4AddressToLong(IPV4_ADDRESS),
                ((TunIpv4DstCaseValue)converted.getMatchEntryValue()).getTunIpv4DstValues().getValue().longValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final TunIpv4DstValuesBuilder tunIpv4DstValuesBuilder = new TunIpv4DstValuesBuilder()
                .setValue(IpConverter.ipv4AddressToLong(IPV4_ADDRESS));
        final TunIpv4DstCaseValueBuilder tunIpv4DstCaseValueBuilder = new TunIpv4DstCaseValueBuilder()
                .setTunIpv4DstValues(tunIpv4DstValuesBuilder.build());

        final TunIpv4DstCaseValue tunIpv4DstCaseValue = tunIpv4DstCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(tunIpv4DstCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = tunIPv4DstConvertor
                .convert(matchEntry, MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(IPV4_ADDRESS, ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject())
                .getNxmNxTunIpv4Dst().getIpv4Address());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIpv4DstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = tunIPv4DstConvertor
                .convert(matchEntry, MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(IPV4_ADDRESS, ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                .getNxmNxTunIpv4Dst().getIpv4Address());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIpv4DstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = tunIPv4DstConvertor
                .convert(matchEntry, MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(IPV4_ADDRESS, ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject())
                .getNxmNxTunIpv4Dst().getIpv4Address());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIpv4DstKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = tunIPv4DstConvertor
                .convert(matchEntry, MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(IPV4_ADDRESS, ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject())
                .getNxmNxTunIpv4Dst().getIpv4Address());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIpv4DstKey.class);
    }
}
