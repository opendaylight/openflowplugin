/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test of {@link PortUpdateTranslator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PortUpdateTranslatorTest {

    private PortUpdateTranslator portUpdateTranslator;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private DeviceInfo deviceInfo;

    private org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig portConfig;
    private State portState;
    private PortStatusMessageBuilder portBld;

    @Before
    public void setUp() {
        portUpdateTranslator = new PortUpdateTranslator();

        portState = new StateBuilder().setLive(true).setBlocked(false).setLinkDown(false).build();
        portConfig = org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig
                .getDefaultInstance("nOFWD");
        portBld = assemblePortStatusMessage(21L, 84L);

    }

    @Test
    public void testTranslate_13() {
        Mockito.when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures portFeatures =
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures
                        .getDefaultInstance("hundredGbFd");


        final FlowCapableNodeConnector nodeConnector =
                portUpdateTranslator.translate(portBld.build(), deviceInfo, null);

        commonCheck(nodeConnector);

        Assert.assertEquals(portConfig, nodeConnector.getConfiguration());
        Assert.assertEquals(portFeatures, nodeConnector.getCurrentFeature());
        Assert.assertEquals(portFeatures, nodeConnector.getAdvertisedFeatures());
        Assert.assertEquals(portFeatures, nodeConnector.getPeerFeatures());
        Assert.assertEquals(portFeatures, nodeConnector.getSupported());
        Assert.assertEquals(portState, nodeConnector.getState());
        Assert.assertNull(nodeConnector.getQueue());
    }

    private static void commonCheck(final FlowCapableNodeConnector nodeConnector) {
        Assert.assertEquals(84L, nodeConnector.getCurrentSpeed().longValue());
        Assert.assertEquals(84L * 2, nodeConnector.getMaximumSpeed().longValue());
        Assert.assertEquals("utPortName:21", nodeConnector.getName());
        Assert.assertEquals("01:02:03:04:05:06", nodeConnector.getHardwareAddress().getValue());
        Assert.assertEquals(21L, nodeConnector.getPortNumber().getUint32().longValue());
    }

    @Test
    public void testTranslate_10() {
        Mockito.when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures portFeatures =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures(
                        false, false, false, false, false, true,  false, false,
                        false, false, false, false, false, false, false, false
                );

        final FlowCapableNodeConnector nodeConnector =
                portUpdateTranslator.translate(portBld.build(), deviceInfo, null);

        commonCheck(nodeConnector);

        Assert.assertEquals(portConfig, nodeConnector.getConfiguration());
        Assert.assertEquals(portFeatures, nodeConnector.getCurrentFeature());
        Assert.assertEquals(portFeatures, nodeConnector.getAdvertisedFeatures());
        Assert.assertEquals(portFeatures, nodeConnector.getPeerFeatures());
        Assert.assertEquals(portFeatures, nodeConnector.getSupported());
        Assert.assertEquals(portState, nodeConnector.getState());
        Assert.assertNull(nodeConnector.getQueue());
    }

    private static PortStatusMessageBuilder assemblePortStatusMessage(final long portNoValue, final long speed) {
        final PortFeatures portFeatures13 = PortFeatures.getDefaultInstance("_100gbFd");
        final PortFeaturesV10 portFeatures10 = PortFeaturesV10.getDefaultInstance("_100mbFd");
        final PortConfig portConfig13 = PortConfig.getDefaultInstance("noFwd");
        final PortConfigV10 portConfig10 = PortConfigV10.getDefaultInstance("noFwd");
        final PortState portState13 = PortState.getDefaultInstance("live");
        final PortStateV10 portState10 = PortStateV10.getDefaultInstance("live");

        return new PortStatusMessageBuilder()
                .setPortNo(Uint32.valueOf(portNoValue))
                .setReason(PortReason.OFPPRADD)
                .setAdvertisedFeatures(portFeatures13)
                .setAdvertisedFeaturesV10(portFeatures10)
                .setConfig(portConfig13)
                .setConfigV10(portConfig10)
                .setCurrentFeatures(portFeatures13)
                .setCurrentFeaturesV10(portFeatures10)
                .setPeerFeatures(portFeatures13)
                .setPeerFeaturesV10(portFeatures10)
                .setState(portState13)
                .setStateV10(portState10)
                .setSupportedFeatures(portFeatures13)
                .setSupportedFeaturesV10(portFeatures10)
                .setCurrSpeed(Uint32.valueOf(speed))
                .setHwAddr(new MacAddress("01:02:03:04:05:06"))
                .setMaxSpeed(Uint32.valueOf(2 * speed))
                .setName("utPortName:" + portNoValue);
    }
}
