/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class DeviceStateUtilTest {
    @Mock
    private DeviceState mockedDeviceState;

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(mockedDeviceState);
    }

    @Test
    public void setDeviceStateBasedOnV13CapabilitiesTest() {
        final Capabilities dummyCapabilities =  new Capabilities(false,false,false,false,false,false,false);

        DeviceStateUtil.setDeviceStateBasedOnV13Capabilities(mockedDeviceState, dummyCapabilities);

        verify(mockedDeviceState).setFlowStatisticsAvailable(false);
        verify(mockedDeviceState).setTableStatisticsAvailable(false);
        verify(mockedDeviceState).setPortStatisticsAvailable(false);
        verify(mockedDeviceState).setQueueStatisticsAvailable(false);
        verify(mockedDeviceState).setGroupAvailable(false);
    }

    @Test
    public void setDeviceStateBasedOnV10CapabilitiesTest() {
        CapabilitiesV10 dummyCapabilitiesV10 =
                new CapabilitiesV10(false, false, false, false, false, false, false, false);

        DeviceStateUtil.setDeviceStateBasedOnV10Capabilities(mockedDeviceState, dummyCapabilitiesV10);
        verify(mockedDeviceState).setFlowStatisticsAvailable(false);
        verify(mockedDeviceState).setTableStatisticsAvailable(false);
        verify(mockedDeviceState).setPortStatisticsAvailable(false);
        verify(mockedDeviceState).setQueueStatisticsAvailable(false);
    }

    @Test
    public void createNodeInstanceIdentifierTest() {
        final var nodeId = new NodeId("dummyId");
        assertEquals(DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(nodeId)).build(),
            DeviceStateUtil.createNodeInstanceIdentifier(nodeId));
    }
}
