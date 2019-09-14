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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc4Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._4.grouping.NxmNxNshc4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._4.grouping.NxmNxNshc4Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link Nshc4Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class Nshc4ConvertorTest {
    @Mock
    private Extension extension;

    private Nshc4Convertor nshc4Convertor;

    private static final Uint32 NSHC4_VALUE = Uint32.valueOf(0xFFFFFFFFL);
    private static final Uint32 MASK_VALUE = Uint32.valueOf(0xFFFFFFFFL);

    @Before
    public void setUp() {
        NxmNxNshc4 nxmNxNshc4 = new NxmNxNshc4Builder().setValue(NSHC4_VALUE).setMask(MASK_VALUE).build();
        NxAugMatchNodesNodeTableFlow nxAugMatchNotifUpdateFlowStats = new NxAugMatchNodesNodeTableFlowBuilder()
                .setNxmNxNshc4(nxmNxNshc4)
                .build();
        when(extension.augmentation(ArgumentMatchers.any()))
                .thenReturn(nxAugMatchNotifUpdateFlowStats);

        nshc4Convertor = new Nshc4Convertor();
    }

    @Test
    public void testConvertToOFJava() {
        final MatchEntry converted = nshc4Convertor.convert(extension);
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) converted.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NshcCaseValue nshcCaseValue = (NshcCaseValue) ofjAugNxExpMatch.getNxExpMatchEntryValue();
        Assert.assertEquals(NiciraConstants.NX_NSH_VENDOR_ID,
                experimenterIdCase.getExperimenter().getExperimenter().getValue());
        Assert.assertEquals(NSHC4_VALUE, nshcCaseValue.getNshc());
        Assert.assertEquals(MASK_VALUE, nshcCaseValue.getMask());
    }

    @Test
    public void testConvertToOFSal() {
        MatchEntry matchEntry = Nshc4Convertor.buildMatchEntry(NSHC4_VALUE, MASK_VALUE);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nshc4Convertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(NSHC4_VALUE,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxNshc4().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxNshc4().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc4Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nshc4Convertor.convert(matchEntry,
                MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(NSHC4_VALUE,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                        .getNxmNxNshc4().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                        .getNxmNxNshc4().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc4Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nshc4Convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(NSHC4_VALUE,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxNshc4().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxNshc4().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc4Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nshc4Convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(NSHC4_VALUE,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxNshc4().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxNshc4().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc4Key.class);
    }
}
