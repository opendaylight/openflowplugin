/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.eric.convertor.match;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.GroupingLooseResolver;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.icmpv6.nd.options.type.grouping.Icmpv6NdOptionsTypeValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.oxm.container.match.entry.value.Icmpv6NdOptionsTypeCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.oxm.container.match.entry.value.Icmpv6NdOptionsTypeCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.Icmpv6NdOptionsTypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.eric.of.icmpv6.nd.options.type.grouping.EricOfIcmpv6NdOptionsTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.ExtensionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Test for {@link Icmpv6NDOptionsTypeConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class Icmpv6NDOptionsTypeConvertorTest {

    @Mock
    private Extension extension;
    @Mock
    private MatchEntry matchEntry;

    private Icmpv6NDOptionsTypeConvertor icmpv6NDOptionsTypeConvertor;

    @Before
    public void setUp()  {
        final EricOfIcmpv6NdOptionsTypeBuilder ericOfIcmpv6NdOptionsTypeBuilder = new EricOfIcmpv6NdOptionsTypeBuilder()
                .setIcmpv6NdOptionsType((short)1);
        final EricAugMatchNodesNodeTableFlowBuilder ericAugMatchNotifUpdateFlowStatsBuilder =
                new EricAugMatchNodesNodeTableFlowBuilder();
        ericAugMatchNotifUpdateFlowStatsBuilder.setEricOfIcmpv6NdOptionsType(ericOfIcmpv6NdOptionsTypeBuilder.build());

        final Augmentation<Extension> extensionAugmentation = ericAugMatchNotifUpdateFlowStatsBuilder.build();
        when(extension.augmentation(Matchers.any()))
                .thenReturn(extensionAugmentation);

        icmpv6NDOptionsTypeConvertor = new Icmpv6NDOptionsTypeConvertor();
    }

    @Test
    public void testConvert()  {
        final MatchEntry converted = icmpv6NDOptionsTypeConvertor.convert(extension);
        assertEquals(1, ((Icmpv6NdOptionsTypeCaseValue)converted.getMatchEntryValue())
                .getIcmpv6NdOptionsTypeValues().getIcmpv6NdOptionsType().intValue());
    }

    @Test
    public void testConvert1()  {
        final Icmpv6NdOptionsTypeValuesBuilder icmpv6NdOptionsTypeValuesBuilder = new Icmpv6NdOptionsTypeValuesBuilder()
                .setIcmpv6NdOptionsType((short)10);
        final Icmpv6NdOptionsTypeCaseValueBuilder icmpv6NdOptionsTypeCaseValueBuilder
                = new Icmpv6NdOptionsTypeCaseValueBuilder()
                .setIcmpv6NdOptionsTypeValues(icmpv6NdOptionsTypeValuesBuilder.build());

        final Icmpv6NdOptionsTypeCaseValue icmpv6NdOptionsTypeCaseValue = icmpv6NdOptionsTypeCaseValueBuilder.build();
        when(matchEntry.getMatchEntryValue()).thenReturn(icmpv6NdOptionsTypeCaseValue);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment
                = icmpv6NDOptionsTypeConvertor.convert(matchEntry, MatchPath.PACKET_RECEIVED_MATCH);
        assertEquals(10, ((EricAugMatchNotifPacketIn) extensionAugment.getAugmentationObject())
                .getEricOfIcmpv6NdOptionsType().getIcmpv6NdOptionsType().intValue());
        assertEquals(extensionAugment.getKey(), Icmpv6NdOptionsTypeKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment1
                = icmpv6NDOptionsTypeConvertor.convert(matchEntry, MatchPath.SWITCH_FLOW_REMOVED_MATCH);
        assertEquals(10, ((EricAugMatchNotifSwitchFlowRemoved) extensionAugment1.getAugmentationObject())
                .getEricOfIcmpv6NdOptionsType() .getIcmpv6NdOptionsType().intValue());
        assertEquals(extensionAugment.getKey(), Icmpv6NdOptionsTypeKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment2
                = icmpv6NDOptionsTypeConvertor.convert(matchEntry, MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);
        assertEquals(10, ((EricAugMatchNodesNodeTableFlow) extensionAugment2.getAugmentationObject())
                .getEricOfIcmpv6NdOptionsType().getIcmpv6NdOptionsType().intValue());
        assertEquals(extensionAugment.getKey(), Icmpv6NdOptionsTypeKey.class);

        final ExtensionAugment<? extends Augmentation<Extension>> extensionAugment3
                = icmpv6NDOptionsTypeConvertor.convert(matchEntry, MatchPath.FLOWS_STATISTICS_RPC_MATCH);
        assertEquals(10, ((EricAugMatchRpcGetFlowStats) extensionAugment3.getAugmentationObject())
                .getEricOfIcmpv6NdOptionsType().getIcmpv6NdOptionsType().intValue());
        assertEquals(extensionAugment.getKey(), Icmpv6NdOptionsTypeKey.class);
    }

    @Test
    public void testSetFieldExtension()  {
        GroupingLooseResolver<GeneralExtensionListGrouping> eqGroup =
                new GroupingLooseResolver<>(GeneralExtensionListGrouping.class);
        eqGroup.add(GeneralAugMatchNodesNodeTableFlowWriteActionsSetField.class);

        ExtensionAugment<? extends Augmentation<Extension>> extensionMatch
                =  new ExtensionAugment<>(EricAugMatchNodesNodeTableFlow.class,
                new EricAugMatchNodesNodeTableFlowBuilder().setEricOfIcmpv6NdOptionsType(
                        new EricOfIcmpv6NdOptionsTypeBuilder().setIcmpv6NdOptionsType((short)1).build()).build(),
                Icmpv6NdOptionsTypeKey.class);

        ExtensionListBuilder extListBld = null;
        ExtensionBuilder extBld = new ExtensionBuilder();
        extBld.addAugmentation(extensionMatch.getAugmentationObject());

        extListBld = new ExtensionListBuilder();
        extListBld.setExtension(extBld.build());
        extListBld.setExtensionKey(extensionMatch.getKey());

        SetField setField = new SetFieldBuilder()
                .addAugmentation(new GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder()
                    .setExtensionList(Collections.singletonList(extListBld.build())).build())
                .build();

        Assert.assertEquals(Icmpv6NdOptionsTypeKey.class, eqGroup.getExtension(setField).get().nonnullExtensionList()
                 .values().iterator().next().getExtensionKey());
    }

}