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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshTtlCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshTtlKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsh.ttl.grouping.NxmNxNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsh.ttl.grouping.NxmNxNshTtlBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(MockitoJUnitRunner.class)
public class NshTtlConvertorTest {
    @Mock
    private Extension extension;

    private NshTtlConvertor convertor;
    private static final Uint8 TTL_VALUE = Uint8.valueOf(0x13);

    @Before
    public void setUp() {
        NxmNxNshTtl nxmNxNshTtl = new NxmNxNshTtlBuilder()
                .setNshTtl(TTL_VALUE)
                .build();
        NxAugMatchNodesNodeTableFlow nxAugMatchNotifUpdateFlowStats = new NxAugMatchNodesNodeTableFlowBuilder()
                .setNxmNxNshTtl(nxmNxNshTtl)
                .build();
        when(extension.augmentation(ArgumentMatchers.any()))
                .thenReturn(nxAugMatchNotifUpdateFlowStats);

        convertor = new NshTtlConvertor();
    }

    @Test
    public void testConvertToOFJava() {

        final MatchEntry converted = convertor.convert(extension);

        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) converted.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NshTtlCaseValue nshTtlCaseValue = (NshTtlCaseValue) ofjAugNxExpMatch.getNxExpMatchEntryValue();
        assertEquals(NiciraConstants.NX_NSH_VENDOR_ID,
                experimenterIdCase.getExperimenter().getExperimenter().getValue());
        assertEquals(TTL_VALUE, nshTtlCaseValue.getNshTtlValues().getNshTtl());
    }

    @Test
    public void testConvertToOFSal() {
        MatchEntry matchEntry = NshTtlConvertor.buildMatchEntry(TTL_VALUE, null);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = convertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        assertEquals(TTL_VALUE, ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject())
                .getNxmNxNshTtl().getNshTtl());
        assertEquals(extensionAugment.getKey(), NxmNxNshTtlKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = convertor.convert(matchEntry,
                MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        assertEquals(TTL_VALUE, ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                .getNxmNxNshTtl().getNshTtl());
        assertEquals(extensionAugment.getKey(), NxmNxNshTtlKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        assertEquals(TTL_VALUE, ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject())
                .getNxmNxNshTtl().getNshTtl());
        assertEquals(extensionAugment.getKey(), NxmNxNshTtlKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = convertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        assertEquals(TTL_VALUE, ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject())
                .getNxmNxNshTtl().getNshTtl());
        assertEquals(extensionAugment.getKey(), NxmNxNshTtlKey.class);
    }
}