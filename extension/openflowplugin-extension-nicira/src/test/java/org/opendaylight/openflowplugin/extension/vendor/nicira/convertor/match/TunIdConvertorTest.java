/*
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
import org.mockito.junit.MockitoJUnitRunner;
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
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Test for {@link TunIdConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TunIdConvertorTest {
    private static final Uint64 U64_TEN = Uint64.valueOf(10).intern();

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private TunIdConvertor tunIdConvertor;

    @Before
    public void setUp() {
        final NxmNxTunIdBuilder nxmNxTunIdBuilder = new NxmNxTunIdBuilder()
                .setValue(Uint64.ONE);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder =
                new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmNxTunId(nxmNxTunIdBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.augmentation(ArgumentMatchers.any()))
            .thenReturn(extensionAugmentation);

        tunIdConvertor = new TunIdConvertor();
    }

    @Test
    public void testConvert() {
        final MatchEntry converted = tunIdConvertor.convert(extension);
        Assert.assertEquals(Uint64.ONE, ((TunIdCaseValue)converted.getMatchEntryValue())
                .getTunIdValues().getValue());
    }

    @Test
    public void testConvert1() {
        final TunIdValuesBuilder tunIdValuesBuilder = new TunIdValuesBuilder()
                .setValue(U64_TEN);
        final TunIdCaseValueBuilder tunIdCaseValueBuilder = new TunIdCaseValueBuilder()
                .setTunIdValues(tunIdValuesBuilder.build());

        final TunIdCaseValue tunIdCaseValue = tunIdCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(tunIdCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = tunIdConvertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(U64_TEN,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxTunId().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIdKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = tunIdConvertor.convert(matchEntry,
                MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(U64_TEN,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject()).getNxmNxTunId()
                        .getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIdKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = tunIdConvertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(U64_TEN,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxTunId().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIdKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = tunIdConvertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(U64_TEN,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxTunId().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxTunIdKey.class);
    }
}
