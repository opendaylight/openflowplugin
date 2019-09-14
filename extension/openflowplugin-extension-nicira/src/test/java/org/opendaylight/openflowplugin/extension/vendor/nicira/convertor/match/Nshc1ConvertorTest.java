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
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc1Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._1.grouping.NxmNxNshc1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._1.grouping.NxmNxNshc1Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link Nshc1Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class Nshc1ConvertorTest {
    @Mock
    private Extension extension;

    private Nshc1Convertor nshc1Convertor;

    private static final Uint32 NSHC1_VALUE = Uint32.valueOf(0xFFFFFFFFL).intern();
    private static final Uint32 MASK_VALUE = Uint32.valueOf(0xFFFFFFFFL).intern();

    @Before
    public void setUp() {
        NxmNxNshc1 nxmNxNshc1 = new NxmNxNshc1Builder().setValue(NSHC1_VALUE).setMask(MASK_VALUE).build();
        NxAugMatchNodesNodeTableFlow nxAugMatchNotifUpdateFlowStats = new NxAugMatchNodesNodeTableFlowBuilder()
                .setNxmNxNshc1(nxmNxNshc1)
                .build();
        when(extension.augmentation(ArgumentMatchers.any()))
                .thenReturn(nxAugMatchNotifUpdateFlowStats);

        nshc1Convertor = new Nshc1Convertor();
    }

    @Test
    public void testConvertToOFJava() {
        final MatchEntry converted = nshc1Convertor.convert(extension);
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) converted.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NshcCaseValue nshcCaseValue = (NshcCaseValue) ofjAugNxExpMatch.getNxExpMatchEntryValue();
        Assert.assertEquals(NiciraConstants.NX_NSH_VENDOR_ID,
                experimenterIdCase.getExperimenter().getExperimenter().getValue());
        Assert.assertEquals(NSHC1_VALUE, nshcCaseValue.getNshc());
        Assert.assertEquals(MASK_VALUE, nshcCaseValue.getMask());
    }

    @Test
    public void testConvertToOFSal() {
        MatchEntry matchEntry = Nshc1Convertor.buildMatchEntry(NSHC1_VALUE, MASK_VALUE);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nshc1Convertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(NSHC1_VALUE,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxNshc1().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxNshc1().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc1Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nshc1Convertor.convert(matchEntry,
                MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(NSHC1_VALUE,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                        .getNxmNxNshc1().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                        .getNxmNxNshc1().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc1Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nshc1Convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(NSHC1_VALUE,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxNshc1().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxNshc1().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc1Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nshc1Convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(NSHC1_VALUE,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxNshc1().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxNshc1().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc1Key.class);
    }
}
