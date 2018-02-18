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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsi.grouping.NsiValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.NsiCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.NsiCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNsiKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsi.grouping.NxmNxNsiBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link NsiConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NsiConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private NsiConvertor nsiConvertor;

    @Before
    public void setUp() throws Exception {
        final NxmNxNsiBuilder nxmNxNsiBuilder = new NxmNxNsiBuilder()
                .setNsi((short)1);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmNxNsi(nxmNxNsiBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        nsiConvertor = new NsiConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = nsiConvertor.convert(extension);
        Assert.assertEquals(1, ((NsiCaseValue)matchEntry.getMatchEntryValue()).getNsiValues().getNsi().intValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final NsiValuesBuilder nsiValuesBuilder = new NsiValuesBuilder()
                .setNsi(Short.valueOf((short)1));
        final NsiCaseValueBuilder nsiCaseValueBuilder = new NsiCaseValueBuilder()
                .setNsiValues(nsiValuesBuilder.build());

        final NsiCaseValue nsiCaseValue = nsiCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(nsiCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nsiConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxNsi().getNsi().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNsiKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nsiConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxNsi().getNsi().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNsiKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nsiConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(1, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxNsi().getNsi().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNsiKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nsiConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(1, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxNsi().getNsi().intValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNsiKey.class);
    }

}
