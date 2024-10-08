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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpOpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.op._case.ArpOpBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.ArpOpCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpOpKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.arp.op.grouping.NxmOfArpOpBuilder;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Test for {@link ArpOpConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArpOpConvertorTest {
    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private ArpOpConvertor arpOpConvertor;

    @Before
    public void setUp() {
        arpOpConvertor = new ArpOpConvertor();
    }

    @Test
    public void testConvertToOFJava() {
        final NxmOfArpOpBuilder nxmOfArpOpBuilder = new NxmOfArpOpBuilder()
                .setValue(Uint16.TWO);
        final NxAugMatchRpcAddFlowBuilder nxAugMatchRpcAddFlowBuilder = new NxAugMatchRpcAddFlowBuilder();
        nxAugMatchRpcAddFlowBuilder.setNxmOfArpOp(nxmOfArpOpBuilder.build());

        final Augmentation<Extension> extensionAugmentation = nxAugMatchRpcAddFlowBuilder.build();
        when(extension.augmentation(ArgumentMatchers.any()))
                .thenReturn(extensionAugmentation);

        final MatchEntry converted = arpOpConvertor.convert(extension);
        Assert.assertEquals(nxAugMatchRpcAddFlowBuilder.getNxmOfArpOp().getValue(),
                ((ArpOpCaseValue) converted.getMatchEntryValue()).getArpOpValues().getValue());
    }

    @Test
    public void testConvertFromOFJava() {
        final ArpOpBuilder arpOpBuilder = new ArpOpBuilder()
                .setOpCode(Uint16.TWO);
        final ArpOpCaseBuilder arpOpCaseBuilder = new ArpOpCaseBuilder()
                .setArpOp(arpOpBuilder.build());
        final ArpOpCase arpOpCase = arpOpCaseBuilder.build();

        when(matchEntry.getMatchEntryValue()).thenReturn(arpOpCase);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = arpOpConvertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(arpOpCase.getArpOp().getOpCode(),
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmOfArpOp().getValue());
        Assert.assertEquals(NxmOfArpOpKey.VALUE, extensionAugment.getKey());

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = arpOpConvertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(arpOpCase.getArpOp().getOpCode(),
                ((NxAugMatchNodesNodeTableFlow) extensionAugment1.getAugmentationObject()).getNxmOfArpOp().getValue());
        Assert.assertEquals(NxmOfArpOpKey.VALUE, extensionAugment1.getKey());

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = arpOpConvertor.convert(matchEntry,
                MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(arpOpCase.getArpOp().getOpCode(),
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment2.getAugmentationObject()).getNxmOfArpOp()
                        .getValue());
        Assert.assertEquals(NxmOfArpOpKey.VALUE, extensionAugment2.getKey());

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = arpOpConvertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(arpOpCase.getArpOp().getOpCode(),
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmOfArpOp().getValue());
        Assert.assertEquals(NxmOfArpOpKey.VALUE, extensionAugment3.getKey());
    }
}
