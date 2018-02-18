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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nshc._1.grouping.Nshc1ValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.Nshc1CaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.Nshc1CaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc1Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._1.grouping.NxmNxNshc1Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link Nshc1Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class Nshc1ConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private Nshc1Convertor nshc1Convertor;

    @Before
    public void setUp() throws Exception {
        final NxmNxNshc1Builder nxmNxNshc1Builder = new NxmNxNshc1Builder()
                .setValue(1L);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmNxNshc1(nxmNxNshc1Builder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        nshc1Convertor = new Nshc1Convertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = nshc1Convertor.convert(extension);
        Assert.assertEquals(1, ((Nshc1CaseValue)matchEntry.getMatchEntryValue()).getNshc1Values().getNshc().intValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final Nshc1ValuesBuilder nshc1ValuesBuilder = new Nshc1ValuesBuilder()
                .setNshc(Long.valueOf(1));
        final Nshc1CaseValueBuilder nshc1CaseValueBuilder = new Nshc1CaseValueBuilder()
                .setNshc1Values(nshc1ValuesBuilder.build());

        final Nshc1CaseValue nshc1CaseValue = nshc1CaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(nshc1CaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nshc1Convertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxNshc1().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc1Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nshc1Convertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxNshc1().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc1Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nshc1Convertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxNshc1().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc1Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nshc1Convertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(1, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxNshc1().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc1Key.class);
    }

}
