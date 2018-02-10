/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDirectStatisticsServiceTest {
    protected static final Long PORT_NO = 1L;
    protected static final BigInteger DATAPATH_ID = BigInteger.TEN;
    protected static final short OF_VERSION = OFConstants.OFP_VERSION_1_3;
    protected static final String NODE_ID = "openflow:1";

    @Mock
    protected RequestContextStack requestContextStack;
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
    @Mock
    protected DeviceGroupRegistry deviceGroupRegistry;
    @Mock
    protected DeviceFlowRegistry deviceFlowRegistry;
    @Mock
    protected DeviceMeterRegistry deviceMeterRegistry;

    protected NodeConnectorId nodeConnectorId;
    protected KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    protected ConvertorManager convertorManager;
    protected MultipartWriterProvider multipartWriterProvider;
    protected static NodeRef createNodeRef(String nodeIdValue) {
        InstanceIdentifier<Node> nodePath = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(nodeIdValue)));

        return new NodeRef(nodePath);
    }

    @Before
    public void init() throws Exception {
        nodeConnectorId = InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                DATAPATH_ID, PORT_NO, OpenflowVersion.get(OF_VERSION));

        nodeInstanceIdentifier = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(NODE_ID)));

        convertorManager = ConvertorManagerFactory.createDefaultManager();
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        when(deviceContext.getDeviceFlowRegistry()).thenReturn(deviceFlowRegistry);
        when(deviceContext.getDeviceGroupRegistry()).thenReturn(deviceGroupRegistry);
        when(deviceContext.getDeviceMeterRegistry()).thenReturn(deviceMeterRegistry);
        when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        when(deviceContext.getMultiMsgCollector(any())).thenReturn(multiMsgCollector);
        when(deviceContext.oook()).thenReturn(translatorLibrary);
        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);
        when(deviceInfo.getNodeId()).thenReturn(new NodeId(NODE_ID));
        when(deviceInfo.getVersion()).thenReturn(OF_VERSION);
        when(deviceInfo.getDatapathId()).thenReturn(DATAPATH_ID);
        when(getFeaturesOutput.getVersion()).thenReturn(OF_VERSION);
        when(getFeaturesOutput.getDatapathId()).thenReturn(DATAPATH_ID);
        when(connectionContext.getFeatures()).thenReturn(features);
        when(connectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        when(features.getVersion()).thenReturn(OF_VERSION);
        when(features.getDatapathId()).thenReturn(DATAPATH_ID);
        multipartWriterProvider = MultipartWriterProviderFactory.createDefaultProvider(deviceContext);
        setUp();
    }

    protected abstract void setUp() throws Exception;

    @Test
    public abstract void testBuildRequestBody() throws Exception;

    @Test
    public abstract void testBuildReply() throws Exception;

    @Test
    public abstract void testStoreStatistics() throws Exception;
}
