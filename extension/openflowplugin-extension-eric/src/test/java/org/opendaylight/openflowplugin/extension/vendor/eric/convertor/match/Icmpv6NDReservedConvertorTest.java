/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.eric.convertor.match;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.GroupingLooseResolver;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.icmpv6.nd.reserved.grouping.Icmpv6NdReservedValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.oxm.container.match.entry.value.Icmpv6NdReservedCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.oxm.container.match.entry.value.Icmpv6NdReservedCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.Icmpv6NdReservedKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.eric.of.icmpv6.nd.reserved.grouping.EricOfIcmpv6NdReservedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.ExtensionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link Icmpv6NDReservedConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class Icmpv6NDReservedConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private Icmpv6NDReservedConvertor icmpv6NDReservedConvertor;

    @Before
    public void setUp()  {
        final EricOfIcmpv6NdReservedBuilder ericOfIcmpv6NdReservedBuilder = new EricOfIcmpv6NdReservedBuilder()
                .setIcmpv6NdReserved(Uint32.ONE);
        final EricAugMatchNodesNodeTableFlowBuilder ericAugMatchNotifUpdateFlowStatsBuilder =
                new EricAugMatchNodesNodeTableFlowBuilder();
        ericAugMatchNotifUpdateFlowStatsBuilder.setEricOfIcmpv6NdReserved(ericOfIcmpv6NdReservedBuilder.build());

        final Augmentation<Extension> extensionAugmentation = ericAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.augmentation(any()))
            .thenReturn(extensionAugmentation);

        icmpv6NDReservedConvertor = new Icmpv6NDReservedConvertor();
    }

    @Test
    public void testConvert()  {
        final MatchEntry converted = icmpv6NDReservedConvertor.convert(extension);
        assertEquals(1, ((Icmpv6NdReservedCaseValue)converted.getMatchEntryValue())
                .getIcmpv6NdReservedValues().getIcmpv6NdReserved().intValue());
    }

    @Test
    public void testConvert1()  {
        final Icmpv6NdReservedValuesBuilder icmpv6NdReservedValuesBuilder = new Icmpv6NdReservedValuesBuilder()
                .setIcmpv6NdReserved(Uint32.TEN);
        final Icmpv6NdReservedCaseValueBuilder icmpv6NdReservedCaseValueBuilder = new Icmpv6NdReservedCaseValueBuilder()
                .setIcmpv6NdReservedValues(icmpv6NdReservedValuesBuilder.build());

        final Icmpv6NdReservedCaseValue icmpv6NdReservedCaseValue = icmpv6NdReservedCaseValueBuilder.build();
        when(matchEntry.getMatchEntryValue()).thenReturn(icmpv6NdReservedCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment
                = icmpv6NDReservedConvertor.convert(matchEntry, MatchPath.PACKET_RECEIVED_MATCH);
        assertEquals(10, ((EricAugMatchNotifPacketIn) extensionAugment.getAugmentationObject())
                .getEricOfIcmpv6NdReserved().getIcmpv6NdReserved().intValue());
        assertEquals(extensionAugment.getKey(), Icmpv6NdReservedKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1
                = icmpv6NDReservedConvertor.convert(matchEntry, MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        assertEquals(10, ((EricAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                .getEricOfIcmpv6NdReserved().getIcmpv6NdReserved().intValue());
        assertEquals(extensionAugment.getKey(), Icmpv6NdReservedKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2
                = icmpv6NDReservedConvertor.convert(matchEntry, MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        assertEquals(10, ((EricAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject())
                .getEricOfIcmpv6NdReserved().getIcmpv6NdReserved().intValue());
        assertEquals(extensionAugment.getKey(), Icmpv6NdReservedKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3
               = icmpv6NDReservedConvertor.convert(matchEntry, MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        assertEquals(10, ((EricAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject())
                .getEricOfIcmpv6NdReserved().getIcmpv6NdReserved().intValue());
        assertEquals(extensionAugment.getKey(), Icmpv6NdReservedKey.class);
    }

    @Test
    public void testSetFieldExtension()  {
        GroupingLooseResolver<GeneralExtensionListGrouping> eqGroup =
                new GroupingLooseResolver<>(GeneralExtensionListGrouping.class);
        eqGroup.add(GeneralAugMatchNodesNodeTableFlowWriteActionsSetField.class);

        ExtensionAugment<? extends Augmentation<Extension>> extensionMatch =
            new ExtensionAugment<>(EricAugMatchNodesNodeTableFlow.class,
                new EricAugMatchNodesNodeTableFlowBuilder().setEricOfIcmpv6NdReserved(
                    new EricOfIcmpv6NdReservedBuilder().setIcmpv6NdReserved(Uint32.ONE).build()).build(),
                Icmpv6NdReservedKey.class);

        ExtensionListBuilder extListBld = null;
        ExtensionBuilder extBld = new ExtensionBuilder();
        extBld.addAugmentation(extensionMatch.getAugmentationObject());

        extListBld = new ExtensionListBuilder();
        extListBld.setExtension(extBld.build());
        extListBld.setExtensionKey(extensionMatch.getKey());

        SetField setField = new SetFieldBuilder()
                .addAugmentation(new GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder()
                    .setExtensionList(Collections.singletonList(extListBld.build()))
                    .build())
                .build();

        assertEquals(Icmpv6NdReservedKey.class, eqGroup.getExtension(setField).get().nonnullExtensionList()
                .values().iterator().next().getExtensionKey());
    }

}
