/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import static org.mockito.Mockito.when;

import java.math.BigInteger;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.tun.id.grouping.TunIdValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIdCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIdCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIdKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.tun.id.grouping.NxmNxTunIdBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link TunIdConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TunIdConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private TunIdConvertor tunIdConvertor;

    @Before
    public void setUp() throws Exception {
        final NxmNxTunIdBuilder nxmNxTunIdBuilder = new NxmNxTunIdBuilder()
                .setValue(BigInteger.ONE);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmNxTunId(nxmNxTunIdBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        tunIdConvertor = new TunIdConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = tunIdConvertor.convert(extension);
        Assert.assertEquals(BigInteger.ONE, ((TunIdCaseValue)matchEntry.getMatchEntryValue()).getTunIdValues().getValue());
    }

    @Test
    public void testConvert1() throws Exception {
        final TunIdValuesBuilder tunIdValuesBuilder = new TunIdValuesBuilder()
                .setValue(BigInteger.TEN);
        final TunIdCaseValueBuilder tunIdCaseValueBuilder = new TunIdCaseValueBuilder()
                .setTunIdValues(tunIdValuesBuilder.build());

        final TunIdCaseValue tunIdCaseValue = tunIdCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(tunIdCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = tunIdConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(BigInteger.TEN, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxTunId().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIdKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = tunIdConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(BigInteger.TEN, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxTunId().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIdKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = tunIdConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(BigInteger.TEN, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxTunId().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIdKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = tunIdConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(BigInteger.TEN, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxTunId().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIdKey.class);
    }

}
