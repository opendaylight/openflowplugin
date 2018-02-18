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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.ct.state.grouping.CtStateValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.CtStateCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.CtStateCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxCtStateKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.ct.state.grouping.NxmNxCtStateBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link CtStateConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CtStateConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private CtStateConvertor ctStateConvertor;

    @Before
    public void setUp() throws Exception {

        final NxmNxCtStateBuilder nxmNxCtStateBuilder = new NxmNxCtStateBuilder()
                .setCtState(1L)
                .setMask(2L);
        final NxAugMatchNodesNodeTableFlowBuilder nxAugMatchNotifUpdateFlowStatsBuilder = new NxAugMatchNodesNodeTableFlowBuilder();
        nxAugMatchNotifUpdateFlowStatsBuilder.setNxmNxCtState(nxmNxCtStateBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        ctStateConvertor = new CtStateConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final MatchEntry matchEntry = ctStateConvertor.convert(extension);
        Assert.assertEquals(1L, ((CtStateCaseValue)matchEntry.getMatchEntryValue()).getCtStateValues().getCtState().longValue());
        Assert.assertEquals(2L, ((CtStateCaseValue)matchEntry.getMatchEntryValue()).getCtStateValues().getMask().longValue());
    }

    @Test
    public void testConvert1() throws Exception {

        final CtStateValuesBuilder ctStateValuesBuilder = new CtStateValuesBuilder()
                .setCtState(3L)
                .setMask(4L);
        final CtStateCaseValueBuilder ctStateCaseValueBuilder = new CtStateCaseValueBuilder()
                .setCtStateValues(ctStateValuesBuilder.build());

        final CtStateCaseValue ctStateCaseValue = ctStateCaseValueBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(ctStateCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = ctStateConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(3L, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxCtState().getCtState().longValue());
        Assert.assertEquals(4L, ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmNxCtState().getMask().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxCtStateKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = ctStateConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(3L, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxCtState().getCtState().longValue());
        Assert.assertEquals(4L, ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment1.getAugmentationObject()).getNxmNxCtState().getMask().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxCtStateKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = ctStateConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(3L, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxCtState().getCtState().longValue());
        Assert.assertEquals(4L, ((NxAugMatchNodesNodeTableFlow)extensionAugment2.getAugmentationObject()).getNxmNxCtState().getMask().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxCtStateKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = ctStateConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(3L, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxCtState().getCtState().longValue());
        Assert.assertEquals(4L, ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmNxCtState().getMask().longValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxCtStateKey.class);

    }

}
