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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nshc._2.grouping.Nshc2ValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.Nshc2CaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.Nshc2CaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc2Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._2.grouping.NxmNxNshc2Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link Nshc2Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class Nshc2ConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private Nshc2Convertor nshc2Convertor;

    @Before
    public void setUp() throws Exception {
        final NxmNxNshc2Builder nxmNxNshc2Builder = new NxmNxNshc2Builder()
                .setValue(1L);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmNxNshc2(nxmNxNshc2Builder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        nshc2Convertor = new Nshc2Convertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = nshc2Convertor.convert(extension);
        Assert.assertEquals(1, ((Nshc2CaseValue)matchEntry.getMatchEntryValue()).getNshc2Values().getNshc().intValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final Nshc2ValuesBuilder nshc2ValuesBuilder = new Nshc2ValuesBuilder()
                .setNshc(Long.valueOf(1));
        final Nshc2CaseValueBuilder nshc2CaseValueBuilder = new Nshc2CaseValueBuilder()
                .setNshc2Values(nshc2ValuesBuilder.build());

        final Nshc2CaseValue nshc2CaseValue = nshc2CaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(nshc2CaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nshc2Convertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxNshc2().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc2Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nshc2Convertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxNshc2().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc2Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nshc2Convertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxNshc2().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc2Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nshc2Convertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(1, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxNshc2().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc2Key.class);
    }


}
