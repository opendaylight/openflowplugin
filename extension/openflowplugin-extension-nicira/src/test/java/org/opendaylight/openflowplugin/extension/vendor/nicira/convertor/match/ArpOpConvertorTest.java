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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpOpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.op._case.ArpOpBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpOpCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpOpKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.arp.op.grouping.NxmOfArpOpBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link ArpOpConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArpOpConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(ArpOpConvertorTest.class);

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private ArpOpConvertor arpOpConvertor;

    @Before
    public void setUp() throws Exception {
        arpOpConvertor = new ArpOpConvertor();
    }

    @Test
    public void testConvertToOFJava() throws Exception {
        final NxmOfArpOpBuilder nxmOfArpOpBuilder = new NxmOfArpOpBuilder()
                .setValue(2);
        final NxAugMatchRpcAddFlowBuilder nxAugMatchRpcAddFlowBuilder = new NxAugMatchRpcAddFlowBuilder();
        nxAugMatchRpcAddFlowBuilder.setNxmOfArpOp(nxmOfArpOpBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchRpcAddFlowBuilder.build();
        when(extension.getAugmentation(Matchers.<Class<Augmentation<Extension>>>any())).thenReturn(extensionAugmentation);

        final MatchEntry matchEntry = arpOpConvertor.convert(extension);
        Assert.assertEquals(nxAugMatchRpcAddFlowBuilder.getNxmOfArpOp().getValue(), ((ArpOpCaseValue)matchEntry.getMatchEntryValue()).getArpOpValues().getValue());
    }

    @Test
    public void testConvertFromOFJava() throws Exception {
        final ArpOpBuilder arpOpBuilder = new ArpOpBuilder()
                .setOpCode(2);
        final ArpOpCaseBuilder arpOpCaseBuilder = new ArpOpCaseBuilder()
                .setArpOp(arpOpBuilder.build());
        final ArpOpCase arpOpCase = arpOpCaseBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(arpOpCase);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = arpOpConvertor.convert(matchEntry, MatchPath.PACKETRECEIVED_MATCH);
        Assert.assertEquals(arpOpCase.getArpOp().getOpCode(), ((NxAugMatchNotifPacketIn)extensionAugment.getAugmentationObject()).getNxmOfArpOp().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmOfArpOpKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = arpOpConvertor.convert(matchEntry, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(arpOpCase.getArpOp().getOpCode(), ((NxAugMatchNodesNodeTableFlow)extensionAugment1.getAugmentationObject()).getNxmOfArpOp().getValue());
        Assert.assertEquals(extensionAugment1.getKey(), NxmOfArpOpKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = arpOpConvertor.convert(matchEntry, MatchPath.SWITCHFLOWREMOVED_MATCH);
        Assert.assertEquals(arpOpCase.getArpOp().getOpCode(), ((NxAugMatchNotifSwitchFlowRemoved)extensionAugment2.getAugmentationObject()).getNxmOfArpOp().getValue());
        Assert.assertEquals(extensionAugment2.getKey(), NxmOfArpOpKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = arpOpConvertor.convert(matchEntry, MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
        Assert.assertEquals(arpOpCase.getArpOp().getOpCode(), ((NxAugMatchRpcGetFlowStats)extensionAugment3.getAugmentationObject()).getNxmOfArpOp().getValue());
        Assert.assertEquals(extensionAugment3.getKey(), NxmOfArpOpKey.class);

    }
}
