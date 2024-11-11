/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringOnTheFlyService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class StatisticsContextImpMockInitiation {
    Boolean isTable = false;
    Boolean isFlow = false;
    Boolean isGroup = false;
    Boolean isMeter = false;
    Boolean isPort = false;
    Boolean isQueue = false;

    protected DeviceContext mockedDeviceContext;
    protected DeviceState mockedDeviceState;

    StatisticsGatheringService<MultipartReply> mockedStatisticsGatheringService;
    StatisticsGatheringOnTheFlyService<MultipartReply> mockedStatisticsOnFlyGatheringService;
    ConnectionContext mockedConnectionContext;
    DeviceInfo mockedDeviceInfo;
    StatisticsManager mockedStatisticsManager;
    ContextChainMastershipWatcher mockedMastershipWatcher;

    static final DataObjectIdentifier.WithKey<Node, NodeKey> DUMMY_NODE_ID = DataObjectIdentifier.builder(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("dummyNodeId")))
            .build();

    @Before
    public void initialize() {
        mockedDeviceContext = mock(DeviceContext.class);
        mockedStatisticsGatheringService = mock(StatisticsGatheringService.class);
        mockedStatisticsOnFlyGatheringService = mock(StatisticsGatheringOnTheFlyService.class);
        mockedConnectionContext = mock(ConnectionContext.class);
        mockedDeviceState = mock(DeviceState.class);
        mockedDeviceInfo = mock(DeviceInfo.class);
        mockedStatisticsManager = mock(StatisticsManager.class);
        mockedMastershipWatcher = mock(ContextChainMastershipWatcher.class);

        final FeaturesReply mockedFeatures = mock(FeaturesReply.class);
        final MessageSpy mockedMessageSpy = mock(MessageSpy.class);
        final OutboundQueue mockedOutboundQueue = mock(OutboundQueue.class);
        final DeviceManager mockedDeviceManager = mock(DeviceManager.class);

        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getDeviceInfo()).thenReturn(mockedDeviceInfo);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedConnectionContext);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessageSpy);

        when(mockedDeviceInfo.getVersion()).thenReturn(Uint8.ONE);
        when(mockedDeviceInfo.getNodeInstanceIdentifier()).thenReturn(DUMMY_NODE_ID);
        when(mockedDeviceInfo.getDatapathId()).thenReturn(Uint64.TEN);

        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getDeviceInfo()).thenReturn(mockedDeviceInfo);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedConnectionContext);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessageSpy);
        when(mockedDeviceInfo.getNodeId()).thenReturn(DUMMY_NODE_ID.getKey().getId());

        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedConnectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);

    }
}
