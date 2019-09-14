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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc3Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._3.grouping.NxmNxNshc3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._3.grouping.NxmNxNshc3Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link Nshc3Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class Nshc3ConvertorTest {
    @Mock
    private Extension extension;

    private Nshc3Convertor nshc3Convertor;

    private static final Uint32 NSHC3_VALUE = Uint32.valueOf(0xFFFFFFFFL);
    private static final Uint32 MASK_VALUE = Uint32.valueOf(0xFFFFFFFFL);

    @Before
    public void setUp() {
        NxmNxNshc3 nxmNxNshc3 = new NxmNxNshc3Builder().setValue(NSHC3_VALUE).setMask(MASK_VALUE).build();
        NxAugMatchNodesNodeTableFlow nxAugMatchNotifUpdateFlowStats = new NxAugMatchNodesNodeTableFlowBuilder()
                .setNxmNxNshc3(nxmNxNshc3)
                .build();
        when(extension.augmentation(ArgumentMatchers.any()))
                .thenReturn(nxAugMatchNotifUpdateFlowStats);

        nshc3Convertor = new Nshc3Convertor();
    }

    @Test
    public void testConvertToOFJava() {
        final MatchEntry converted = nshc3Convertor.convert(extension);
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) converted.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NshcCaseValue nshcCaseValue = (NshcCaseValue) ofjAugNxExpMatch.getNxExpMatchEntryValue();
        Assert.assertEquals(NiciraConstants.NX_NSH_VENDOR_ID,
                experimenterIdCase.getExperimenter().getExperimenter().getValue());
        Assert.assertEquals(NSHC3_VALUE, nshcCaseValue.getNshc());
        Assert.assertEquals(MASK_VALUE, nshcCaseValue.getMask());
    }

    @Test
    public void testConvertToOFSal() {
        MatchEntry matchEntry = Nshc3Convertor.buildMatchEntry(NSHC3_VALUE, MASK_VALUE);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nshc3Convertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(NSHC3_VALUE,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxNshc3().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxNshc3().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc3Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nshc3Convertor.convert(matchEntry,
                MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(NSHC3_VALUE,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                        .getNxmNxNshc3().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                        .getNxmNxNshc3().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc3Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nshc3Convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(NSHC3_VALUE,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxNshc3().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxNshc3().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc3Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nshc3Convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(NSHC3_VALUE,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxNshc3().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxNshc3().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc3Key.class);
    }
}
