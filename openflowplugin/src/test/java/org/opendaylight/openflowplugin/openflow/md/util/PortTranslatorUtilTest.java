/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;

/**
 * Created by Martin Bobak mbobak@cisco.com on 7/29/14.
 */
public class PortTranslatorUtilTest {

    private static final String MAC_ADDRESS = "00:01:02:03:04:05";
    private static final String NAME = "PortTranslatorTest";
    private final Boolean[] pfBls = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    private final boolean[] pfV10Bls = {false, false, false, false, false, false, false, false, false, false, false, false};
    private final boolean[] portCfgBools = {false, false, false, false};
    private final boolean[] portCfgV10bools = {false, false, false, false, false, false, false};
    private final boolean[] portStateBools = {false, false, false, false};
    private final Long currentSpeed = Long.decode("4294967295");
    private static final Long maxSpeed = Long.decode("4294967295");

    /**
     * Test  method for {@link PortTranslatorUtil#translatePortFeatures(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures)}
     */
    @Test
    public void testTranslatePortFeatures() {


        for (int i = 0; i < pfBls.length; i++) {
            pfBls[i] = true;
            final PortFeatures apf = getPortFeatures();
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures npf = PortTranslatorUtil.translatePortFeatures(apf);
            assertEqualsPortFeatures(apf, npf);
            pfBls[i] = false;
        }

    }

    private PortFeatures getPortFeatures() {
        return new PortFeatures(pfBls[0], pfBls[1], pfBls[2], pfBls[3], pfBls[4], pfBls[5], pfBls[6], pfBls[7], pfBls[8],
                pfBls[9], pfBls[10], pfBls[11], pfBls[12], pfBls[13], pfBls[14], pfBls[15]);
    }

    /**
     * Test  method for {@link PortTranslatorUtil#translatePortFeatures(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10)}
     */
    @Test
    public void testTranslatePortFeaturesV10() {


        for (int i = 0; i < pfV10Bls.length; i++) {

            pfV10Bls[i] = true;
            final PortFeaturesV10 apfV10 = getPortFeaturesV10();
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures npf = PortTranslatorUtil.translatePortFeatures(apfV10);
            assertEqualsPortFeaturesV10(apfV10, npf);
            pfV10Bls[i] = false;

        }

    }

    private PortFeaturesV10 getPortFeaturesV10() {
        return new PortFeaturesV10(pfV10Bls[0], pfV10Bls[1], pfV10Bls[2], pfV10Bls[3], pfV10Bls[4], pfV10Bls[5], pfV10Bls[6],
                pfV10Bls[7], pfV10Bls[8], pfV10Bls[9], pfV10Bls[10], pfV10Bls[11]);
    }

    /**
     * Test  method for {@link PortTranslatorUtil#translatePort(Short, java.math.BigInteger, Long, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping)} ()}
     */
    @Test
    public void testTranslatePort() {

        Short version = OpenflowVersion.OF10.getVersion();

        BigInteger dataPathId = BigInteger.ONE;
        Long portNumber = Long.MAX_VALUE;
        PortGrouping portGrouping = mockPortGrouping();

        NodeConnectorUpdated nodeConnectorUpdated = PortTranslatorUtil.translatePort(version, dataPathId, portNumber, portGrouping);
        assertNotNull(nodeConnectorUpdated);
        version = OpenflowVersion.OF13.getVersion();
        nodeConnectorUpdated = PortTranslatorUtil.translatePort(version, dataPathId, portNumber, portGrouping);
        assertNotNull(nodeConnectorUpdated);

        version = OpenflowVersion.UNSUPPORTED.getVersion();
        nodeConnectorUpdated = PortTranslatorUtil.translatePort(version, dataPathId, portNumber, portGrouping);
        assertNotNull(nodeConnectorUpdated);


    }

    private PortGrouping mockPortGrouping() {
        PortGrouping portGrouping = mock(PortGrouping.class);
        when(portGrouping.getAdvertisedFeatures()).thenReturn(getPortFeatures());
        when(portGrouping.getAdvertisedFeaturesV10()).thenReturn(getPortFeaturesV10());
        when(portGrouping.getConfig()).thenReturn(getPortConfig());
        when(portGrouping.getConfigV10()).thenReturn(getPortConfigV10());
        when(portGrouping.getCurrentFeatures()).thenReturn(getPortFeatures());
        when(portGrouping.getCurrentFeaturesV10()).thenReturn(getPortFeaturesV10());
        when(portGrouping.getCurrSpeed()).thenReturn(currentSpeed);
        when(portGrouping.getHwAddr()).thenReturn(getMacAddress());
        when(portGrouping.getName()).thenReturn(NAME);
        when(portGrouping.getMaxSpeed()).thenReturn(maxSpeed);
        when(portGrouping.getPeerFeatures()).thenReturn(getPortFeatures());
        when(portGrouping.getPeerFeaturesV10()).thenReturn(getPortFeaturesV10());
        when(portGrouping.getPortNo()).thenReturn(Long.MAX_VALUE);
        when(portGrouping.getState()).thenReturn(getPortState());
        when(portGrouping.getSupportedFeatures()).thenReturn(getPortFeatures());
        when(portGrouping.getSupportedFeaturesV10()).thenReturn(getPortFeaturesV10());
        return portGrouping;
    }

    private PortState getPortState() {
        PortState portState = new PortState(portStateBools[0], portStateBools[1], portStateBools[2]);
        return portState;
    }

    private static MacAddress getMacAddress() {
        return new MacAddress(MAC_ADDRESS);
    }

    private PortConfigV10 getPortConfigV10() {
        return new PortConfigV10(portCfgV10bools[0], portCfgV10bools[1], portCfgV10bools[2], portCfgV10bools[3], portCfgV10bools[4], portCfgV10bools[5], portCfgV10bools[6]);
    }

    private PortConfig getPortConfig() {
        return new PortConfig(portCfgBools[0], portCfgBools[1], portCfgBools[2], portCfgBools[3]);
    }

    private static void assertEqualsPortFeaturesV10(final PortFeaturesV10 apfV10, final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures npf) {
        assertEquals(apfV10.is_100mbFd(), npf.isHundredMbFd());
        assertEquals(apfV10.is_100mbHd(), npf.isHundredMbHd());

        assertEquals(apfV10.is_10gbFd(), npf.isTenGbFd());
        assertEquals(apfV10.is_10mbFd(), npf.isTenMbFd());
        assertEquals(apfV10.is_10mbHd(), npf.isTenMbHd());

        assertEquals(apfV10.is_1gbFd(), npf.isOneGbFd());
        assertEquals(apfV10.is_1gbHd(), npf.isOneGbHd());

        assertEquals(apfV10.isAutoneg(), npf.isAutoeng());
        assertEquals(apfV10.isCopper(), npf.isCopper());
        assertEquals(apfV10.isFiber(), npf.isFiber());
        assertEquals(apfV10.isPause(), npf.isPause());
        assertEquals(apfV10.isPauseAsym(), npf.isPauseAsym());
    }

    private static void assertEqualsPortFeatures(final PortFeatures apf, final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures npf) {
        assertEquals(apf.is_100gbFd(), npf.isHundredGbFd());
        assertEquals(apf.is_100mbFd(), npf.isHundredMbFd());
        assertEquals(apf.is_100mbHd(), npf.isHundredMbHd());

        assertEquals(apf.is_10gbFd(), npf.isTenGbFd());
        assertEquals(apf.is_10mbFd(), npf.isTenMbFd());
        assertEquals(apf.is_10mbHd(), npf.isTenMbHd());

        assertEquals(apf.is_1gbFd(), npf.isOneGbFd());
        assertEquals(apf.is_1gbHd(), npf.isOneGbHd());
        assertEquals(apf.is_1tbFd(), npf.isOneTbFd());

        assertEquals(apf.is_40gbFd(), npf.isFortyGbFd());

        assertEquals(apf.isAutoneg(), npf.isAutoeng());
        assertEquals(apf.isCopper(), npf.isCopper());
        assertEquals(apf.isFiber(), npf.isFiber());
        assertEquals(apf.isOther(), npf.isOther());
        assertEquals(apf.isPause(), npf.isPause());
        assertEquals(apf.isPauseAsym(), npf.isPauseAsym());
    }

}
