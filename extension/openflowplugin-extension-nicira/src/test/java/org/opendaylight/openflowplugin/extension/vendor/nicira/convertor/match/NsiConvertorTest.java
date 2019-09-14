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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NsiCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNsiKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsi.grouping.NxmNxNsi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsi.grouping.NxmNxNsiBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link NsiConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NsiConvertorTest {
    @Mock
    private Extension extension;

    private NsiConvertor nsiConvertor;

    private static final Uint8 NSI_VALUE = Uint8.MAX_VALUE;

    @Before
    public void setUp() {
        NxmNxNsi nxmNxNsi = new NxmNxNsiBuilder().setNsi(NSI_VALUE).build();
        NxAugMatchNodesNodeTableFlow nxAugMatchNotifUpdateFlowStats = new NxAugMatchNodesNodeTableFlowBuilder()
                .setNxmNxNsi(nxmNxNsi)
                .build();
        when(extension.augmentation(ArgumentMatchers.any()))
                .thenReturn(nxAugMatchNotifUpdateFlowStats);

        nsiConvertor = new NsiConvertor();
    }

    @Test
    public void testConvertToOFJava() {
        final MatchEntry converted = nsiConvertor.convert(extension);
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) converted.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NsiCaseValue nsiCaseValue = (NsiCaseValue) ofjAugNxExpMatch.getNxExpMatchEntryValue();
        Assert.assertEquals(NiciraConstants.NX_NSH_VENDOR_ID,
                experimenterIdCase.getExperimenter().getExperimenter().getValue());
        Assert.assertEquals(NSI_VALUE, nsiCaseValue.getNsiValues().getNsi());
    }

    @Test
    public void testConvertToOFSal() {
        MatchEntry matchEntry = NsiConvertor.buildMatchEntry(NSI_VALUE, null);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nsiConvertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(NSI_VALUE,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxNsi().getNsi());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNsiKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nsiConvertor.convert(matchEntry,
                MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(NSI_VALUE,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject()).getNxmNxNsi().getNsi());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNsiKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nsiConvertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(NSI_VALUE,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxNsi().getNsi());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNsiKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nsiConvertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(NSI_VALUE,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxNsi().getNsi());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNsiKey.class);
    }
}

