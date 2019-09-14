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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NspCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNspKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsp.grouping.NxmNxNsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.nsp.grouping.NxmNxNspBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link NspConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NspConvertorTest {
    @Mock
    private Extension extension;

    private NspConvertor nspConvertor;

    private static final Uint32 NSP_VALUE = Uint32.valueOf(0xFFL);

    @Before
    public void setUp() {
        NxmNxNsp nxmNxNsp = new NxmNxNspBuilder().setValue(NSP_VALUE).build();
        NxAugMatchNodesNodeTableFlow nxAugMatchNotifUpdateFlowStats = new NxAugMatchNodesNodeTableFlowBuilder()
                .setNxmNxNsp(nxmNxNsp)
                .build();
        when(extension.augmentation(ArgumentMatchers.any()))
                .thenReturn(nxAugMatchNotifUpdateFlowStats);

        nspConvertor = new NspConvertor();
    }

    @Test
    public void testConvertToOFJava() {
        final MatchEntry converted = nspConvertor.convert(extension);
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) converted.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NspCaseValue nspCaseValue = (NspCaseValue) ofjAugNxExpMatch.getNxExpMatchEntryValue();
        Assert.assertEquals(NiciraConstants.NX_NSH_VENDOR_ID,
                experimenterIdCase.getExperimenter().getExperimenter().getValue());
        Assert.assertEquals(NSP_VALUE, nspCaseValue.getNspValues().getNsp());
    }

    @Test
    public void testConvertToOFSal() {
        MatchEntry matchEntry = NspConvertor.buildMatchEntry(NSP_VALUE, null);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment = nspConvertor.convert(matchEntry,
                MatchPath.PACKET_RECEIVED_MATCH);
        Assert.assertEquals(NSP_VALUE,
                ((NxAugMatchNotifPacketIn) extensionAugment.getAugmentationObject()).getNxmNxNsp().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNspKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1 = nspConvertor.convert(matchEntry,
                MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        Assert.assertEquals(NSP_VALUE,
                ((NxAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                        .getNxmNxNsp().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNspKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2 = nspConvertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        Assert.assertEquals(NSP_VALUE,
                ((NxAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject()).getNxmNxNsp().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNspKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3 = nspConvertor.convert(matchEntry,
                MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        Assert.assertEquals(NSP_VALUE,
                ((NxAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject()).getNxmNxNsp().getValue());
        Assert.assertEquals(extensionAugment.getKey(), NxmNxNspKey.class);
    }
}
