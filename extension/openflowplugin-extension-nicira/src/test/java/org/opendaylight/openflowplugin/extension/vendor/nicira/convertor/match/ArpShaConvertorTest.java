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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.arp.sha.grouping.ArpShaValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpShaCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpShaCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpShaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.arp.sha.grouping.NxmNxArpShaBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link ArpShaConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArpShaConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private ArpShaConvertor arpShaConvertor;

    private static final MacAddress MAC_ADDRESS =  MacAddress.getDefaultInstance("01:23:45:67:89:AB");

    private static final Logger LOG = LoggerFactory.getLogger(ArpShaConvertorTest.class);

    @Before
    public void setUp() throws Exception {
        final NxmNxArpShaBuilder nxmNxArpShaBuilder = new NxmNxArpShaBuilder()
                .setMacAddress(MAC_ADDRESS);
        final NxAugMatchRpcAddFlowBuilder nxAugMatchRpcAddFlowBuilder = new NxAugMatchRpcAddFlowBuilder()
                .setNxmNxArpSha(nxmNxArpShaBuilder.build());
        final Augmentation<Extension> extensionAugmentation = nxAugMatchRpcAddFlowBuilder.build();

        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        arpShaConvertor = new ArpShaConvertor();
    }

    @Test
    public void testConvertToOFJava() throws Exception {
        final MatchEntry matchEntry = arpShaConvertor.convert(extension);
        Assert.assertEquals(MAC_ADDRESS.getValue(), ((ArpShaCaseValue)matchEntry.getMatchEntryValue()).getArpShaValues().getMacAddress().getValue());
    }

    @Test
    public void testConvertFromOFJava() throws Exception {
        final ArpShaValuesBuilder arpShaValuesBuilder = new ArpShaValuesBuilder()
                .setMacAddress(MAC_ADDRESS);
        final ArpShaCaseValueBuilder arpShaCaseValueBuilder = new ArpShaCaseValueBuilder()
                .setArpShaValues(arpShaValuesBuilder.build());
        final ArpShaCaseValue arpShaCaseValue = arpShaCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(arpShaCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = arpShaConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(arpShaCaseValue.getArpShaValues().getMacAddress(), ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxArpSha().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxArpShaKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = arpShaConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(arpShaCaseValue.getArpShaValues().getMacAddress(), ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxArpSha().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxArpShaKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = arpShaConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(arpShaCaseValue.getArpShaValues().getMacAddress(), ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxArpSha().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxArpShaKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = arpShaConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(arpShaCaseValue.getArpShaValues().getMacAddress(), ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxArpSha().getMacAddress());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxArpShaKey.class);
    }

}
