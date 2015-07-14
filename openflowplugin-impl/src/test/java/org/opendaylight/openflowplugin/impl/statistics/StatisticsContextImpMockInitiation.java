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
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringOnTheFlyService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;


public class StatisticsContextImpMockInitiation {
    protected boolean isTable = false;
    protected boolean isFlow = false;
    protected boolean isGroup = false;
    protected boolean isMeter = false;
    protected boolean isPort = false;
    protected boolean isQueue = false;

    protected DeviceContext mockedDeviceContext;
    protected StatisticsGatheringService mockedStatisticsGatheringService;
    protected StatisticsGatheringOnTheFlyService mockedStatisticsOnFlyGatheringService;
    protected ConnectionContext mockedConnectionContext;
    protected FeaturesReply mockedFeatures;
    protected DeviceState mockedDeviceState;
    protected MessageSpy mockedMessageSpy;
    protected OutboundQueue mockedOutboundQueue;

    @Before
    public void initialize() {
        mockedDeviceContext = mock(DeviceContext.class);
        mockedStatisticsGatheringService = mock(StatisticsGatheringService.class);
        mockedStatisticsOnFlyGatheringService = mock(StatisticsGatheringOnTheFlyService.class);
        mockedConnectionContext = mock(ConnectionContext.class);
        mockedFeatures = mock(FeaturesReply.class);
        mockedDeviceState = mock(DeviceState.class);
        mockedMessageSpy = mock(MessageSpy.class);
        mockedOutboundQueue = mock(OutboundQueue.class);

        when(mockedDeviceState.isTableStatisticsAvailable()).thenReturn(isTable);
        when(mockedDeviceState.isFlowStatisticsAvailable()).thenReturn(isFlow);
        when(mockedDeviceState.isGroupAvailable()).thenReturn(isGroup);
        when(mockedDeviceState.isMetersAvailable()).thenReturn(isMeter);
        when(mockedDeviceState.isPortStatisticsAvailable()).thenReturn(isPort);
        when(mockedDeviceState.isQueueStatisticsAvailable()).thenReturn(isQueue);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedConnectionContext);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessageSpy);

        when(mockedConnectionContext.getNodeId()).thenReturn(new NodeId("dummyNodeId"));
        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedConnectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        when(mockedConnectionContext.getOutboundQueueProvider()).thenReturn(mockedOutboundQueue);

    }
}
