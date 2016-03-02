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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 *
 * test of {@link DeviceStateImpl} - lightweight version, using basic ways (TDD)
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * updated: 3/3/16 by <a href="mailto:jbacigal@cisco.com">Jozef Bacigal</a>
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

    @Test
    public void testGetSetIsValid(){
        Assert.assertFalse(deviceState.isValid());
        deviceState.setValid(true);
        Assert.assertTrue(deviceState.isValid());
    }

    @Test
    public void testGetSetDeviceSynchronized(){
        Assert.assertFalse(deviceState.deviceSynchronized());
        deviceState.setDeviceSynchronized(true);
        Assert.assertTrue(deviceState.deviceSynchronized());
    }

    @Test
    public void testGetSetStatPollEnabled(){
        Assert.assertFalse(deviceState.isStatisticsPollingEnabled());
        deviceState.setStatisticsPollingEnabledProp(true);
        Assert.assertTrue(deviceState.isStatisticsPollingEnabled());
    }

    @Test
    public void testGetSetRole(){
        Assert.assertFalse(deviceState.getRole().equals(OfpRole.BECOMEMASTER));
        Assert.assertFalse(deviceState.getRole().equals(OfpRole.NOCHANGE));
        deviceState.setRole(OfpRole.BECOMEMASTER);
        Assert.assertFalse(deviceState.getRole().equals(OfpRole.BECOMESLAVE));
    }

    @Test
    public void testFalseStatisticsAfterInit(){
        Assert.assertFalse(deviceState.isFlowStatisticsAvailable());
        Assert.assertFalse(deviceState.isPortStatisticsAvailable());
        Assert.assertFalse(deviceState.isQueueStatisticsAvailable());
        Assert.assertFalse(deviceState.isTableStatisticsAvailable());
        deviceState.setFlowStatisticsAvailable(true);
        deviceState.setPortStatisticsAvailable(true);
        deviceState.setQueueStatisticsAvailable(true);
        deviceState.setTableStatisticsAvailable(true);
        Assert.assertTrue(deviceState.isFlowStatisticsAvailable());
        Assert.assertTrue(deviceState.isPortStatisticsAvailable());
        Assert.assertTrue(deviceState.isQueueStatisticsAvailable());
        Assert.assertTrue(deviceState.isTableStatisticsAvailable());
    }

    @Test
    public void testFalseMeterAndGroupAvaiable(){
        Assert.assertFalse(deviceState.isGroupAvailable());
        Assert.assertFalse(deviceState.isMetersAvailable());
        deviceState.setGroupAvailable(true);
        deviceState.setMeterAvailable(true);
        Assert.assertTrue(deviceState.isGroupAvailable());
        Assert.assertTrue(deviceState.isMetersAvailable());
    }

}
