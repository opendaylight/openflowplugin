/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDirectStatisticsServiceTest {
    protected static final Uint32 PORT_NO = Uint32.ONE;
    protected static final Uint64 DATAPATH_ID = Uint64.valueOf(10);
    protected static final Uint8 OF_VERSION = Uint8.valueOf(OFConstants.OFP_VERSION_1_3);
    protected static final String NODE_ID = "openflow:10";

    @Mock
    protected RequestContextStack requestContextStack;
    @Mock
    protected DeviceFlowRegistry deviceFlowRegistry;
    @Mock
    protected DeviceGroupRegistry deviceGroupRegistry;
    @Mock
    protected DeviceMeterRegistry deviceMeterRegistry;
    @Mock
    protected DeviceContext deviceContext;
    @Mock
    protected ConnectionContext connectionContext;
    @Mock
    protected FeaturesReply features;
    @Mock
    protected MessageSpy messageSpy;
    @Mock
    protected OutboundQueue outboundQueueProvider;
    @Mock
    protected MultiMsgCollector multiMsgCollector;
    @Mock
    protected TranslatorLibrary translatorLibrary;
    @Mock
    protected DeviceState deviceState;
    @Mock
    protected DeviceInfo deviceInfo;
    @Mock
    protected GetFeaturesOutput getFeaturesOutput;

    protected NodeConnectorId nodeConnectorId;
    protected KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    protected ConvertorManager convertorManager;
    protected MultipartWriterProvider multipartWriterProvider;

    protected static NodeRef createNodeRef(final String nodeIdValue) {
        InstanceIdentifier<Node> nodePath = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(nodeIdValue)));

        return new NodeRef(nodePath);
    }

    @Before
    public void init() {
        nodeConnectorId = InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                DATAPATH_ID, PORT_NO, OpenflowVersion.get(OF_VERSION));

        nodeInstanceIdentifier = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(NODE_ID)));

        convertorManager = ConvertorManagerFactory.createDefaultManager();
        lenient().when(deviceContext.getDeviceFlowRegistry()).thenReturn(deviceFlowRegistry);
        lenient().when(deviceContext.getDeviceGroupRegistry()).thenReturn(deviceGroupRegistry);
        lenient().when(deviceContext.getDeviceMeterRegistry()).thenReturn(deviceMeterRegistry);
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        lenient().when(deviceContext.getMultiMsgCollector(any())).thenReturn(multiMsgCollector);
        lenient().when(deviceContext.oook()).thenReturn(translatorLibrary);
        lenient().when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);
        lenient().when(deviceInfo.getNodeId()).thenReturn(new NodeId(NODE_ID));
        when(deviceInfo.getVersion()).thenReturn(OF_VERSION.toJava());
        when(deviceInfo.getDatapathId()).thenReturn(DATAPATH_ID);
        lenient().when(getFeaturesOutput.getVersion()).thenReturn(OF_VERSION);
        lenient().when(getFeaturesOutput.getDatapathId()).thenReturn(DATAPATH_ID);
        when(connectionContext.getFeatures()).thenReturn(features);
        lenient().when(connectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        lenient().when(features.getVersion()).thenReturn(OF_VERSION);
        lenient().when(features.getDatapathId()).thenReturn(DATAPATH_ID);
        multipartWriterProvider = MultipartWriterProviderFactory.createDefaultProvider(deviceContext);
        setUp();
    }

    protected abstract void setUp();

    @Test
    public abstract void testBuildRequestBody();

    @Test
    public abstract void testBuildReply();

    @Test
    public abstract void testStoreStatistics();
}
