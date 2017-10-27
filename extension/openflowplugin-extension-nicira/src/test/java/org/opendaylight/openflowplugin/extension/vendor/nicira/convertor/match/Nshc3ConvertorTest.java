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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nshc._3.grouping.Nshc3ValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.Nshc3CaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.Nshc3CaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc3Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._3.grouping.NxmNxNshc3Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link Nshc3Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class Nshc3ConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private Nshc3Convertor nshc3Convertor;

    @Before
    public void setUp() throws Exception {
        final NxmNxNshc3Builder nxmNxNshc3Builder = new NxmNxNshc3Builder()
                .setValue(1L);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmNxNshc3(nxmNxNshc3Builder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        nshc3Convertor = new Nshc3Convertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = nshc3Convertor.convert(extension);
        Assert.assertEquals(1, ((Nshc3CaseValue)matchEntry.getMatchEntryValue()).getNshc3Values().getNshc().intValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final Nshc3ValuesBuilder nshc3ValuesBuilder = new Nshc3ValuesBuilder()
                .setNshc(Long.valueOf(1));
        final Nshc3CaseValueBuilder nshc3CaseValueBuilder = new Nshc3CaseValueBuilder()
                .setNshc3Values(nshc3ValuesBuilder.build());

        final Nshc3CaseValue nshc3CaseValue = nshc3CaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(nshc3CaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nshc3Convertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxNshc3().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc3Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nshc3Convertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxNshc3().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc3Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nshc3Convertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxNshc3().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc3Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nshc3Convertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(1, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxNshc3().getValue().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc3Key.class);
    }

}
