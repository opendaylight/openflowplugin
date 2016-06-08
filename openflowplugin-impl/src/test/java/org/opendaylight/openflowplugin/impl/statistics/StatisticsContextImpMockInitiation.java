/*
 *
 *  * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *
 */

package org.opendaylight.openflowplugin.impl.statistics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.Mockito;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringOnTheFlyService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;


class StatisticsContextImpMockInitiation {
    Boolean isTable = false;
    Boolean isFlow = false;
    Boolean isGroup = false;
    Boolean isMeter = false;
    Boolean isPort = false;
    Boolean isQueue = false;

    protected DeviceContext mockedDeviceContext;
    protected DeviceState mockedDeviceState;

    StatisticsGatheringService mockedStatisticsGatheringService;
    StatisticsGatheringOnTheFlyService mockedStatisticsOnFlyGatheringService;
    ConnectionContext mockedConnectionContext;
    DeviceInfo mockedDeviceInfo;

    static final KeyedInstanceIdentifier<Node, NodeKey> dummyNodeII = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("dummyNodeId")));

    LifecycleConductor mockConductor;

    @Before
    public void initialize() {
        mockedDeviceContext = mock(DeviceContext.class);
        mockedStatisticsGatheringService = mock(StatisticsGatheringService.class);
        mockedStatisticsOnFlyGatheringService = mock(StatisticsGatheringOnTheFlyService.class);
        mockedConnectionContext = mock(ConnectionContext.class);
        mockedDeviceState = mock(DeviceState.class);
        mockedDeviceInfo = mock(DeviceInfo.class);

        final FeaturesReply mockedFeatures = mock(FeaturesReply.class);
        final MessageSpy mockedMessageSpy = mock(MessageSpy.class);
        final OutboundQueue mockedOutboundQueue = mock(OutboundQueue.class);
        final DeviceManager mockedDeviceManager = mock(DeviceManager.class);
        final GetFeaturesOutput mockedFeaturesOutput = mock(GetFeaturesOutput.class);

        mockConductor = mock(LifecycleConductor.class);

        when(mockedDeviceState.isTableStatisticsAvailable()).thenReturn(isTable);
        when(mockedDeviceState.isFlowStatisticsAvailable()).thenReturn(isFlow);
        when(mockedDeviceState.isGroupAvailable()).thenReturn(isGroup);
        when(mockedDeviceState.isMetersAvailable()).thenReturn(isMeter);
        when(mockedDeviceState.isPortStatisticsAvailable()).thenReturn(isPort);
        when(mockedDeviceState.isQueueStatisticsAvailable()).thenReturn(isQueue);
        when(mockedDeviceState.getNodeInstanceIdentifier()).thenReturn(dummyNodeII);
        when(mockedDeviceState.getFeatures()).thenReturn(mockedFeaturesOutput);

        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedConnectionContext);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessageSpy);

        when(mockedConnectionContext.getNodeId()).thenReturn(dummyNodeII.getKey().getId());
        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedConnectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        when(mockedConnectionContext.getOutboundQueueProvider()).thenReturn(mockedOutboundQueue);

        when(mockedDeviceManager.getDeviceContextFromNodeId(Mockito.<NodeId>any())).thenReturn(mockedDeviceContext);
        mockConductor.setSafelyDeviceManager(mockedDeviceManager);
        when(mockConductor.getDeviceContext(Mockito.<NodeId>any())).thenReturn(mockedDeviceContext);

    }
}
