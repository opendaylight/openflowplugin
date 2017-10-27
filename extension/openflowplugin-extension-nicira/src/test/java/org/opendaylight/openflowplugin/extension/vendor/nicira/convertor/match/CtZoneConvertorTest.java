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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.ct.zone.grouping.CtZoneValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.CtZoneCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.CtZoneCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtZoneKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.ct.zone.grouping.NxmNxCtZoneBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link CtZoneConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CtZoneConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private CtZoneConvertor ctZoneConvertor;

    @Before
    public void setUp() throws Exception {

        final NxmNxCtZoneBuilder nxmNxCtZoneBuilder = new NxmNxCtZoneBuilder()
                .setCtZone(1);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmNxCtZone(nxmNxCtZoneBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        ctZoneConvertor = new CtZoneConvertor();

    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = ctZoneConvertor.convert(extension);
        Assert.assertEquals(1L, ((CtZoneCaseValue)matchEntry.getMatchEntryValue()).getCtZoneValues().getCtZone().longValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final CtZoneValuesBuilder ctZoneValuesBuilder = new CtZoneValuesBuilder()
                .setCtZone(2);
        final CtZoneCaseValueBuilder ctZoneCaseValueBuilder = new CtZoneCaseValueBuilder()
                .setCtZoneValues(ctZoneValuesBuilder.build());

        final CtZoneCaseValue ctZoneCaseValue = ctZoneCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(ctZoneCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = ctZoneConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(2L, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxCtZone().getCtZone().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxCtZoneKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = ctZoneConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(2L, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxCtZone().getCtZone().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxCtZoneKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = ctZoneConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(2L, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxCtZone().getCtZone().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxCtZoneKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = ctZoneConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(2L, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxCtZone().getCtZone().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxCtZoneKey.class);

    }

}
