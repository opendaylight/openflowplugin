/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.OperStatus;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Tests for @{LLDPSpeaker} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class LLDPSpeakerTest {
    static InstanceIdentifier<NodeConnector> id;
    static FlowCapableNodeConnector fcnc;
    static TransmitPacketInput packet;

    static {
        MacAddress mac = new MacAddress("01:23:45:67:89:AB");
        id = TestUtils.createNodeConnectorId("openflow:1", "openflow:1:1");
        fcnc = TestUtils.createFlowCapableNodeConnector(mac, 1L).build();
        byte[] lldpFrame = LLDPUtil.buildLldpFrame(new NodeId("openflow:1"),
                new NodeConnectorId("openflow:1:1"), mac, 1L);
        packet = new TransmitPacketInputBuilder()
                .setEgress(new NodeConnectorRef(id))
                .setNode(new NodeRef(id.firstIdentifierOf(Node.class)))
                .setPayload(lldpFrame).build();
    }

    @Mock
    PacketProcessingService packetProcessingService;
    @Mock
    ScheduledExecutorService scheduledExecutorService;
    @Mock
    ScheduledFuture scheduledSpeakerTask;

    MacAddress destinationMACAddress = null;
    LLDPSpeaker lldpSpeaker;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        when(
                scheduledExecutorService.scheduleAtFixedRate(
                        any(Runnable.class), anyLong(), anyLong(),
                        any(TimeUnit.class))).thenReturn(scheduledSpeakerTask);
        lldpSpeaker = new LLDPSpeaker(packetProcessingService,
                scheduledExecutorService, destinationMACAddress);
        lldpSpeaker.setOperationalStatus(OperStatus.RUN);
    }

    /**
     * Test that speaker does nothing when in {@link OperStatus.STANDBY} mode.
     */
    @Test
    public void testStandBy() {
        lldpSpeaker.setOperationalStatus(OperStatus.STANDBY);
        // Add node connector - LLDP packet should be transmitted through
        // packetProcessingService
        lldpSpeaker.nodeConnectorAdded(id, fcnc);

        // Execute one iteration of periodic task - LLDP packet should be
        // transmitted second time
        lldpSpeaker.run();

        // Check packet transmission
        verify(packetProcessingService, times(0)).transmitPacket(packet);
        verifyNoMoreInteractions(packetProcessingService);
    }

    /**
     * Test that LLDP frame is transmitted after port appeared in inventory and
     * periodically after that.
     */
    @Test
    public void testNodeConnectorAdd() {
        // Add node connector - LLDP packet should be transmitted through
        // packetProcessingService
        lldpSpeaker.setOperationalStatus(OperStatus.RUN);
        lldpSpeaker.nodeConnectorAdded(id, fcnc);

        // Execute one iteration of periodic task - LLDP packet should be
        // transmitted second time
        lldpSpeaker.run();

        // Check packet transmission
        verify(packetProcessingService, times(2)).transmitPacket(packet);
        verifyNoMoreInteractions(packetProcessingService);
    }

    /**
     * Test that LLDP frame stop to periodically transmit after port disappeared
     * from inventory.
     */
    @Test
    public void testNodeConnectorRemoval() {
        // Prepare for test - add node connector first
        lldpSpeaker.setOperationalStatus(OperStatus.RUN);
        lldpSpeaker.nodeConnectorAdded(id, fcnc);

        // Trigger removal of packet
        lldpSpeaker.nodeConnectorRemoved(id);

        // Run one iteration of LLDP flood
        lldpSpeaker.run();

        // Verify that LLDP frame sent only once (by nodeConnectorAdded),
        // e.g. no flood after removal
        verify(packetProcessingService, times(1)).transmitPacket(packet);
        verifyNoMoreInteractions(packetProcessingService);
    }

    /**
     * Test that when @{LLDPSpeaker#nodeConnectorAdded} is called multiple times
     * with same arguments, only the first one have effect.
     */
    @Test
    public void testMultipleSameNodeConnectorAddEvents() {
        // Add node connector - LLDP packet should be transmitted through
        // packetProcessingService
        lldpSpeaker.setOperationalStatus(OperStatus.RUN);
        for (int i = 0; i < 10; i++) {
            lldpSpeaker.nodeConnectorAdded(id, fcnc);
        }

        // Check packet transmission
        verify(packetProcessingService, times(1)).transmitPacket(packet);
        verifyNoMoreInteractions(packetProcessingService);
    }

    /**
     * Test that lldpSpeaker cancels periodic LLDP flood task and stops
     * 
     * @{ScheduledExecutorService .
     * @throws Exception
     */
    @Test
    public void testCleanup() throws Exception {
        lldpSpeaker.close();
        verify(scheduledSpeakerTask, times(1)).cancel(true);
        verify(scheduledExecutorService, times(1)).shutdown();
    }

    /**
     * Test that checks if LLDPSpeaker working fine with local ports.
     */
    @Test
    public void testLocalNodeConnectorCreation() {
        // Call nodeConnectorAdded with local port
        FlowCapableNodeConnector fcnc = TestUtils
                .createFlowCapableNodeConnector()
                .setPortNumber(new PortNumberUni("LOCAL")).build();
        lldpSpeaker.nodeConnectorAdded(id, fcnc);

        // Verify that nothing happened for local port
        verify(packetProcessingService, never()).transmitPacket(
                any(TransmitPacketInput.class));
    }
}
