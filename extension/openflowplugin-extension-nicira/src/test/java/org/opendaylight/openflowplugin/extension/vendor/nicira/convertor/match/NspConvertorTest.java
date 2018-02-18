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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsp.grouping.NspValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.NspCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.NspCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNspKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsp.grouping.NxmNxNspBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link NspConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NspConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private NspConvertor nspConvertor;

    @Before
    public void setUp() throws Exception {
        final NxmNxNspBuilder nxmNxNspBuilder = new NxmNxNspBuilder()
                .setValue(Long.valueOf(1L));
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmNxNsp(nxmNxNspBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        nspConvertor = new NspConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = nspConvertor.convert(extension);
        Assert.assertEquals(1, ((NspCaseValue)matchEntry.getMatchEntryValue()).getNspValues().getNsp().intValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final NspValuesBuilder nspValuesBuilder = new NspValuesBuilder()
                .setNsp(Long.valueOf(2L));
        final NspCaseValueBuilder nspCaseValueBuilder = new NspCaseValueBuilder()
                .setNspValues(nspValuesBuilder.build());

        final NspCaseValue nspCaseValue = nspCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(nspCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nspConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(2, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxNsp().getValue().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNspKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nspConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(2, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxNsp().getValue().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNspKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nspConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(2, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxNsp().getValue().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNspKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nspConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(2, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxNsp().getValue().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNspKey.class);
    }
}
