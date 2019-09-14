/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshFlagsCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshFlagsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsh.flags.grouping.NxmNxNshFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsh.flags.grouping.NxmNxNshFlagsBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(MockitoJUnitRunner.class)
public class NshFlagsConvertorTest {
    @Mock
    private Extension extension;

    private NshFlagsConvertor convertor;
    private static final Uint8 FLAGS_VALUE = Uint8.valueOf(0x7B);
    private static final Uint8 MASK_VALUE = Uint8.valueOf(0xFF);

    @Before
    public void setUp() {
        NxmNxNshFlags nxmNxNshFlags = new NxmNxNshFlagsBuilder()
                .setNshFlags(FLAGS_VALUE)
                .setMask(MASK_VALUE)
                .build();
        NxAugMatchNodesNodeTableFlow nxAugMatchNotifUpdateFlowStats = new NxAugMatchNodesNodeTableFlowBuilder()
                .setNxmNxNshFlags(nxmNxNshFlags)
                .build();
        when(extension.augmentation(ArgumentMatchers.any()))
                .thenReturn(nxAugMatchNotifUpdateFlowStats);

        convertor = new NshFlagsConvertor();
    }

    @Test
    public void testConvertToOFJava() {

        final MatchEntry converted = convertor.convert(extension);

        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) converted.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NshFlagsCaseValue nshFlagsCaseValue = (NshFlagsCaseValue) ofjAugNxExpMatch.getNxExpMatchEntryValue();
        assertEquals(NiciraConstants.NX_NSH_VENDOR_ID,
                experimenterIdCase.getExperimenter().getExperimenter().getValue());
        assertEquals(FLAGS_VALUE, nshFlagsCaseValue.getNshFlagsValues().getNshFlags());
        assertEquals(MASK_VALUE, nshFlagsCaseValue.getNshFlagsValues().getMask());
    }

    @Test
    public void testConvertToOFSal() {
        MatchEntry matchEntry = NshFlagsConvertor.buildMatchEntry(FLAGS_VALUE, MASK_VALUE);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = convertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        assertEquals(FLAGS_VALUE, ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject())
                .getNxmNxNshFlags().getNshFlags());
        assertEquals(MASK_VALUE, ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject())
                .getNxmNxNshFlags().getMask());
        assertEquals(extensionAugment.getKey(), NxmNxNshFlagsKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = convertor.convert(matchEntry,
                MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        assertEquals(FLAGS_VALUE, ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                .getNxmNxNshFlags().getNshFlags());
        assertEquals(MASK_VALUE, ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                .getNxmNxNshFlags().getMask());
        assertEquals(extensionAugment.getKey(), NxmNxNshFlagsKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        assertEquals(FLAGS_VALUE, ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject())
                .getNxmNxNshFlags().getNshFlags());
        assertEquals(MASK_VALUE, ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject())
                .getNxmNxNshFlags().getMask());
        assertEquals(extensionAugment.getKey(), NxmNxNshFlagsKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        assertEquals(FLAGS_VALUE, ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject())
                .getNxmNxNshFlags().getNshFlags());
        assertEquals(MASK_VALUE, ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject())
                .getNxmNxNshFlags().getMask());
        assertEquals(extensionAugment.getKey(), NxmNxNshFlagsKey.class);
    }
}