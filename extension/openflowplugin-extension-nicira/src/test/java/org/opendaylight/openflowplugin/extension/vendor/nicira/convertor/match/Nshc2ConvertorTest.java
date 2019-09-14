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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc2Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._2.grouping.NxmNxNshc2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nshc._2.grouping.NxmNxNshc2Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link Nshc2Convertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class Nshc2ConvertorTest {
    @Mock
    private Extension extension;

    private Nshc2Convertor nshc2Convertor;

    private static final Uint32 NSHC2_VALUE = Uint32.valueOf(0xFFFFFFFFL).intern();
    private static final Uint32 MASK_VALUE = Uint32.valueOf(0xFFFFFFFFL).intern();

    @Before
    public void setUp() {
        NxmNxNshc2 nxmNxNshc2 = new NxmNxNshc2Builder().setValue(NSHC2_VALUE).setMask(MASK_VALUE).build();
        NxAugMatchNodesNodeTableFlow nxAugMatchNotifUpdateFlowStats = new NxAugMatchNodesNodeTableFlowBuilder()
                .setNxmNxNshc2(nxmNxNshc2)
                .build();
        when(extension.augmentation(ArgumentMatchers.any()))
                .thenReturn(nxAugMatchNotifUpdateFlowStats);

        nshc2Convertor = new Nshc2Convertor();
    }

    @Test
    public void testConvertToOFJava() {
        final MatchEntry converted = nshc2Convertor.convert(extension);
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) converted.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NshcCaseValue nshcCaseValue = (NshcCaseValue) ofjAugNxExpMatch.getNxExpMatchEntryValue();
        Assert.assertEquals(NiciraConstants.NX_NSH_VENDOR_ID,
                experimenterIdCase.getExperimenter().getExperimenter().getValue());
        Assert.assertEquals(NSHC2_VALUE, nshcCaseValue.getNshc());
        Assert.assertEquals(MASK_VALUE, nshcCaseValue.getMask());
    }

    @Test
    public void testConvertToOFSal() {
        MatchEntry matchEntry = Nshc2Convertor.buildMatchEntry(NSHC2_VALUE, MASK_VALUE);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nshc2Convertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(NSHC2_VALUE,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxNshc2().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxNshc2().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc2Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nshc2Convertor.convert(matchEntry,
                MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(NSHC2_VALUE,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                        .getNxmNxNshc2().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                        .getNxmNxNshc2().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc2Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nshc2Convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(NSHC2_VALUE,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxNshc2().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxNshc2().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc2Key.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nshc2Convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(NSHC2_VALUE,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxNshc2().getValue());
        Assert.assertEquals(MASK_VALUE,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxNshc2().getMask());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNshc2Key.class);
    }
}
