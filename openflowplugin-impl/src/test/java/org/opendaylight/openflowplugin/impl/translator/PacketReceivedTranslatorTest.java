/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import com.google.common.collect.Lists;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Created by tkubas on 4/1/15.
 */

@RunWith(MockitoJUnitRunner.class)
public class PacketReceivedTranslatorTest {

    @Mock
    ConnectionContext connectionContext;
    @Mock
    FeaturesReply featuresReply;
    @Mock
    GetFeaturesOutput getFeaturesOutput;
    @Mock
    DeviceState deviceState;
    @Mock
    DataBroker dataBroker;
    @Mock
    DeviceContext deviceContext;
    @Mock
    DeviceInfo deviceInfo;
    @Mock
    List<PhyPort> phyPorts;
    @Mock
    PhyPort phyPort;

    ConvertorManager convertorManager;

    static final Long PORT_NO = 5l;
    static final Long PORT_NO_DS = 6l;
    static final String DATA = "Test_Data";
    static final Long PORT_NUM_VALUE = 11l;

    @Before
    public void setUp() throws Exception {
        final List<PhyPort> phyPorts = Arrays.asList(phyPort);
        convertorManager = ConvertorManagerFactory.createDefaultManager();

        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(connectionContext.getFeatures()).thenReturn(featuresReply);
        Mockito.when(featuresReply.getDatapathId()).thenReturn(BigInteger.TEN);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(deviceInfo.getDatapathId()).thenReturn(BigInteger.TEN);
        Mockito.when(getFeaturesOutput.getDatapathId()).thenReturn(BigInteger.TEN);
        Mockito.when(getFeaturesOutput.getPhyPort()).thenReturn(phyPorts);
        Mockito.when(phyPort.getPortNo()).thenReturn(PORT_NO_DS);
    }

    @Test
    public void testTranslate() throws Exception {
        final KeyedInstanceIdentifier<Node, NodeKey> nodePath = KeyedInstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("openflow:10")));
        final PacketReceivedTranslator packetReceivedTranslator = new PacketReceivedTranslator(convertorManager);
        final PacketInMessage packetInMessage = createPacketInMessage(DATA.getBytes(), PORT_NO);
        Mockito.when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodePath);

        final PacketReceived packetReceived = packetReceivedTranslator.translate(packetInMessage, deviceInfo, null);

        Assert.assertArrayEquals(packetInMessage.getData(), packetReceived.getPayload());
        Assert.assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.SendToController",
                packetReceived.getPacketInReason().getName());
        Assert.assertEquals("openflow:10:" + PORT_NO,
                packetReceived.getIngress().getValue().firstKeyOf(NodeConnector.class, NodeConnectorKey.class)
                        .getId().getValue());
        Assert.assertEquals(0L, packetReceived.getFlowCookie().getValue().longValue());
        Assert.assertEquals(42L, packetReceived.getTableId().getValue().longValue());
    }

    private static PacketInMessage createPacketInMessage(final byte[] data,
                                                         final long port) {
        final PacketInReason reason = PacketInReason.OFPRACTION;

        MatchEntryBuilder matchEntryBuilder = assembleMatchEntryBld(port);
        MatchBuilder packetInMatchBld = new MatchBuilder()
                .setMatchEntry(Lists.newArrayList(matchEntryBuilder.build()));

        return new PacketInMessageBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_0)
                .setData(data).setReason(reason)
                .setMatch(packetInMatchBld.build())
                .setVersion(OFConstants.OFP_VERSION_1_3)
                .setCookie(BigInteger.ZERO)
                .setTableId(new TableId(42L))
                .build();

    }

    @Test
    public void testGetPacketInMatch() throws Exception {
        MatchEntryBuilder matchEntryBuilder = assembleMatchEntryBld(PORT_NUM_VALUE);
        MatchBuilder packetInMatchBld = new MatchBuilder()
                .setMatchEntry(Lists.newArrayList(matchEntryBuilder.build()));
        PacketInMessageBuilder inputBld = new PacketInMessageBuilder()
                .setMatch(packetInMatchBld.build())
                .setVersion(OFConstants.OFP_VERSION_1_3);
        BigInteger dpid = BigInteger.TEN;

        final PacketReceivedTranslator packetReceivedTranslator = new PacketReceivedTranslator(convertorManager);
        final Match packetInMatch = packetReceivedTranslator.getPacketInMatch(inputBld.build(), dpid);

        Assert.assertNotNull(packetInMatch.getInPort());
        Assert.assertEquals("openflow:10:" + PORT_NUM_VALUE, packetInMatch.getInPort().getValue());
    }

    private static MatchEntryBuilder assembleMatchEntryBld(long portNumValue) {
        MatchEntryBuilder matchEntryBuilder = prepareHeader(InPort.class, false);
        InPortBuilder inPortBld = new InPortBuilder().setPortNumber(new PortNumber(portNumValue));
        InPortCaseBuilder caseBuilder = new InPortCaseBuilder();
        caseBuilder.setInPort(inPortBld.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder;
    }

    private static MatchEntryBuilder prepareHeader(Class<? extends MatchField> oxmMatchField, boolean hasMask) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(oxmMatchField);
        builder.setHasMask(hasMask);
        return builder;
    }
}
