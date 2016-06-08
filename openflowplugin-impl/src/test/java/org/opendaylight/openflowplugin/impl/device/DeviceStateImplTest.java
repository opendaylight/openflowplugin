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

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 *
 * test of {@link DeviceStateImpl} - lightweight version, using basic ways (TDD)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceStateImplTest {

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
        Mockito.when(featuresReply.getVersion()).thenReturn(version);
        Mockito.when(featuresReply.getPhyPort()).thenReturn(pPort);
        deviceState = new DeviceStateImpl();
    }

    @Test
    public void testIsValid_initialValue(){
        Assert.assertFalse(deviceState.isValid());
    }

    @Test
    public void testDeviceSynchronized_initialValue(){
        Assert.assertFalse(deviceState.deviceSynchronized());
    }

    @Test
    public void testStatPollEnabled_initialValue(){
        Assert.assertFalse(deviceState.isStatisticsPollingEnabled());
    }

    @Test
    public void testStatistics_initialValue(){
        Assert.assertFalse(deviceState.isFlowStatisticsAvailable());
        Assert.assertFalse(deviceState.isPortStatisticsAvailable());
        Assert.assertFalse(deviceState.isQueueStatisticsAvailable());
        Assert.assertFalse(deviceState.isTableStatisticsAvailable());
    }

    @Test
    public void testMeterAndGroupAvailable_initialValue(){
        Assert.assertFalse(deviceState.isGroupAvailable());
        Assert.assertFalse(deviceState.isMetersAvailable());
    }

}
