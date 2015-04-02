/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPortBuilder;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 *
 * test of {@link DeviceStateImpl} - lightweight version, using basic ways (TDD)
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Mar 29, 2015
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceStateImplTest {

    private NodeId nodeId;
    @Mock
    private FeaturesReply featuresReply;
    private DeviceStateImpl deviceState;

    private final short version = 13;
    private final long portNr = 10L;
    private final Long portBandwidth = 1024L;
    private final List<PhyPort> pPort = Arrays.asList(new PhyPortBuilder()
                    .setPortNo(portNr).setMaxSpeed(portBandwidth).build());

    @Before
    public void initialization() {
        nodeId = new NodeId("test-node-id");
        Mockito.when(featuresReply.getVersion()).thenReturn(version);
        Mockito.when(featuresReply.getPhyPort()).thenReturn(pPort);
        deviceState = new DeviceStateImpl(featuresReply, nodeId);
    }

    /**
     * Test method for {@link DeviceStateImpl#DeviceStateImpl(FeaturesReply, NodeId)}.
     */
    @Test(expected=NullPointerException.class)
    public void testDeviceStateImplNullNodeId(){
        new DeviceStateImpl(featuresReply, null);
    }

    /**
     * Test method for {@link DeviceStateImpl#DeviceStateImpl(FeaturesReply, NodeId)}.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testDeviceStateImplNullFeaturesReply(){
        new DeviceStateImpl(null, nodeId);
    }

    /**
     * Test method for {@link DeviceStateImpl#DeviceStateImpl(FeaturesReply, NodeId)}.
     */
    @Test(expected=IllegalArgumentException.class)
    @Ignore // Available for OF1.0 only
    public void testDeviceStateImplNullPhyPort(){
        final FeaturesReply emptyFeaturesReply = Mockito.mock(FeaturesReply.class);
        Mockito.when(emptyFeaturesReply.getPhyPort()).thenReturn(null);
        new DeviceStateImpl(emptyFeaturesReply, nodeId);
    }

    /**
     * Test method for {@link DeviceStateImpl#getNodeId()}.
     */
    @Test
    public void testGetNodeId(){
        final NodeId getNodeId = deviceState.getNodeId();
        Assert.assertNotNull(getNodeId);
        Assert.assertEquals(nodeId, getNodeId);
    }

    /**
     * Test method for {@link DeviceStateImpl#getFeatures()}.
     */
    @Test
    public void testGetFeatures(){
        final GetFeaturesOutputBuilder expetedResult = new GetFeaturesOutputBuilder(featuresReply);
        final GetFeaturesOutput getFeatures = deviceState.getFeatures();
        Assert.assertNotNull(getFeatures);
        Assert.assertEquals(expetedResult.getVersion(), getFeatures.getVersion());
        Assert.assertEquals(expetedResult.getPhyPort(), getFeatures.getPhyPort());
    }

    /**
     * Test method for {@link DeviceStateImpl#getPhysicalPorts()}.
     */
    @Test
    @Ignore // Available for OF1.0 only
    public void testGetPhysicalPorts(){
        final Map<Long, PortGrouping> getPhysPort = deviceState.getPhysicalPorts();
        Assert.assertNotNull(getPhysPort);
        Assert.assertTrue(getPhysPort.values().contains(pPort.get(0)));
    }

    /**
     * Test method for {@link DeviceStateImpl#getPortsBandwidth()}.
     */
    @Test
    @Ignore // Available for OF1.0 only
    public void testGetPortsBandwidth(){
        final Map<Long, Long> portBandwidth = deviceState.getPortsBandwidth();
        Assert.assertNotNull(portBandwidth);
        Assert.assertTrue(portBandwidth.containsKey(portNr));
        Assert.assertEquals(this.portBandwidth, portBandwidth.get(portNr));
    }

    /**
     * Test method for {@link DeviceStateImpl#getPorts()}.
     */
    @Test
    @Ignore // Available for OF1.0 only
    public void testGetPorts(){
        final Set<Long> portNrs = deviceState.getPorts();
        Assert.assertTrue(portNrs.contains(portNr));
    }

    /**
     * Test method for {@link DeviceStateImpl#getPhysicalPort(java.lang.Long)}.
     */
    @Test
    @Ignore // Available for OF1.0 only
    public void testGetPhysicalPort(){
        Assert.assertEquals(pPort.get(0), deviceState.getPhysicalPort(portNr));
    }

    /**
     * Test method for {@link DeviceStateImpl#getPortBandwidth(java.lang.Long)}.
     */
    @Test
    @Ignore // Available for OF1.0 only
    public void testGetPortBandwidth(){
        Assert.assertEquals(portBandwidth, deviceState.getPortBandwidth((portNr)));
    }

    /**
     * Test method for {@link DeviceStateImpl#isPortEnabled(long)}.
     */
    @Test
    @Ignore // Available for OF1.0 only
    public void testIsPortEnabledLong(){
        Assert.assertTrue(deviceState.isPortEnabled(portNr));
    }

    /**
     * Test method for {@link DeviceStateImpl#isPortEnabled(PortGrouping)}.
     */
    @Test
    @Ignore // Available for OF1.0 only
    public void testIsPortEnabledPortGrouping(){
        Assert.assertTrue(deviceState.isPortEnabled(pPort.get(0)));
    }

    /**
     * Test method for {@link DeviceStateImpl#getEnabledPorts()}.
     */
    @Test
    @Ignore // Available for OF1.0 only
    public void testGetEnabledPorts(){
        final List<PortGrouping> getEnabledPort = deviceState.getEnabledPorts();
        Assert.assertNotNull(getEnabledPort);
        Assert.assertTrue(getEnabledPort.contains(pPort.get(0)));
    }

}
