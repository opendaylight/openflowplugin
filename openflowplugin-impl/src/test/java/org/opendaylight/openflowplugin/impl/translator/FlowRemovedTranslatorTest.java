/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.translator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Test of {@link AggregatedFlowStatisticsTranslator}
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowRemovedTranslatorTest {

    private FlowRemovedTranslator translator;

    private FlowRemovedV10Translator translatorV10;

    @Mock
    private DeviceContext deviceContext;

    @Mock
    private DeviceState deviceState;

    @Mock
    private DeviceInfo deviceInfo;

    @Mock
    private GetFeaturesOutput features;

    @Mock
    private FlowWildcardsV10 flowWildcards;

    private KeyedInstanceIdentifier<Node, NodeKey> nodeId;

    @Before
    public void setUp() throws Exception {
        nodeId = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("dummyNodeId")));

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        translator = new FlowRemovedTranslator(convertorManager);
        translatorV10 = new FlowRemovedV10Translator(convertorManager);

        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeId);
        when(features.getDatapathId()).thenReturn(BigInteger.TEN);
    }

    @Test
    public void testTranslate() throws Exception {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved flowRemovedMessage = buildMessage(false);
        final FlowRemoved flowRemoved = translator.translate(flowRemovedMessage, deviceInfo, null);

        assertEquals(flowRemovedMessage.getCookie(), flowRemoved.getCookie().getValue());
        assertEquals(flowRemovedMessage.getPriority(), flowRemoved.getPriority());
        assertEquals((long)flowRemovedMessage.getTableId().getValue(), (long)flowRemoved.getTableId());
    }

    @Test
    public void testTranslateV10() throws Exception {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved flowRemovedMessage = buildMessage(true);
        final FlowRemoved flowRemoved = translatorV10.translate(flowRemovedMessage, deviceInfo, null);

        assertEquals(flowRemovedMessage.getCookie(), flowRemoved.getCookie().getValue());
        assertEquals(flowRemovedMessage.getPriority(), flowRemoved.getPriority());
        assertEquals((short)0, flowRemoved.getTableId().shortValue());
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved buildMessage(boolean isV10) {
        FlowRemovedMessageBuilder builder = new FlowRemovedMessageBuilder()
                .setCookie(BigInteger.ONE)
                .setPriority(1);

        if (isV10) {
            builder.setMatchV10(new MatchV10Builder().setWildcards(flowWildcards).build());
        } else {
            builder.setMatch(new MatchBuilder().setMatchEntry(Collections.<MatchEntry>emptyList()).build())
                .setTableId(new TableId(42L));
        }

        return builder.build();
    }
}
