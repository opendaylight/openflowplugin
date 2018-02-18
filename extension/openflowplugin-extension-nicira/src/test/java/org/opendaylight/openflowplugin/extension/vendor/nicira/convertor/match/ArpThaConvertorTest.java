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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.arp.tha.grouping.ArpThaValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpThaCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpThaCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpThaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.arp.tha.grouping.NxmNxArpThaBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link ArpThaConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArpThaConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private static final MacAddress MAC_ADDRESS =  MacAddress.getDefaultInstance("01:23:45:67:89:AB");

    private ArpThaConvertor arpThaConvertor;

    @Before
    public void setUp() throws Exception {

        final NxmNxArpThaBuilder nxArpThaBuilder = new NxmNxArpThaBuilder()
                .setMacAddress(MAC_ADDRESS);
        final NxAugMatchRpcAddFlowBuilder nxAugMatchRpcAddFlowBuilder = new NxAugMatchRpcAddFlowBuilder()
                .setNxmNxArpTha(nxArpThaBuilder.build());
        final Augmentation<Extension> extensionAugmentation = nxAugMatchRpcAddFlowBuilder.build();

        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        arpThaConvertor = new ArpThaConvertor();
    }

    @Test
    public void testConvertToOFJava() throws Exception {
        final MatchEntry matchEntry = arpThaConvertor.convert(extension);
        Assert.assertEquals(MAC_ADDRESS.getValue(), ((ArpThaCaseValue)matchEntry.getMatchEntryValue()).getArpThaValues().getMacAddress().getValue());
    }

    @Test
    public void testConvertFromOFJava() throws Exception {
        final ArpThaValuesBuilder arpThaValuesBuilder = new ArpThaValuesBuilder()
                .setMacAddress(MAC_ADDRESS);
        final ArpThaCaseValueBuilder arpThaCaseValueBuilder = new ArpThaCaseValueBuilder()
                .setArpThaValues(arpThaValuesBuilder.build());
        final ArpThaCaseValue arpThaCaseValue = arpThaCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(arpThaCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = arpThaConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(arpThaCaseValue.getArpThaValues().getMacAddress(), ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxArpTha().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxArpThaKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = arpThaConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(arpThaCaseValue.getArpThaValues().getMacAddress(), ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxArpTha().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxArpThaKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = arpThaConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(arpThaCaseValue.getArpThaValues().getMacAddress(), ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxArpTha().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxArpThaKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = arpThaConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(arpThaCaseValue.getArpThaValues().getMacAddress(), ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxArpTha().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxArpThaKey.class);

    }

}
