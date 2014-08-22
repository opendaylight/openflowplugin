/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * This class defines unit tests for {@link IfType}.
 * 
 * @author Steve Britt
 * @author Simon Hunt
 */
public class IfTypeTest {
    
    private static final int ETHERNET_IF_TYPE = 6;
    private static final int FIBRE_CHANNEL_IF_TYPE = 56;
    private static final int INVALID_IF_TYPE = -1;
    private static final int ISO8_802_5_CRFP_IF_TYPE = 98;
    private static final int UNKNOWN_IF_TYPE = 0;
    private static final int X29_PAD_IF_TYPE = 90;
    private static final String ILU = "Incorrect lookup for IfType value: ";
    private static final String IVFI = "Incorrect value for ifType: ";
    private static final String S_F = "IfType should be considered a physical port type with fiber connector: ";
    private static final String S_LA = "IfType should be considered a link aggregate type: ";
    private static final String S_M = "IfType should be considered a virtual multiplexor type: ";
    private static final String S_P = "IfType should be considered a physical port type: ";
    private static final String S_VI = "IfType should be considered a virtual interface type: ";
    private static final String SN_F = "IfType should NOT be considered a physical port type with fiber connector: ";
    private static final String SN_LA = "IfType should NOT be considered a link aggregate type: ";
    private static final String SN_M = "IfType should NOT be considered a virtual multiplexor type: ";
    private static final String SN_P = "IfType should NOT be considered a physical port type: ";
    private static final String SN_VI = "IfType should NOT be considered a virtual interface type: ";

    
    @Test
    public void testEnumToValueMappings() {
        assertEquals(IVFI + IfType.ISO8_802_5_CRFP, ISO8_802_5_CRFP_IF_TYPE, IfType.ISO8_802_5_CRFP.getValue());
        assertEquals(IVFI + IfType.UNKNOWN, UNKNOWN_IF_TYPE, IfType.UNKNOWN.getValue());
        assertEquals(IVFI + IfType.FIBRE_CHANNEL, FIBRE_CHANNEL_IF_TYPE, IfType.FIBRE_CHANNEL.getValue());
    }
    

    @Test
    public void testIsFiberConnector() {
        assertFalse(SN_F + IfType.ETHERNET_CSMACD, IfType.ETHERNET_CSMACD.isFiberConnector());
        assertFalse(SN_F + IfType.IEEE_802_11, IfType.IEEE_802_11.isFiberConnector());
        assertFalse(SN_F + IfType.SOFTWARE_LOOPBACK, IfType.SOFTWARE_LOOPBACK.isFiberConnector());
        assertTrue(S_F + IfType.FAST_ETHERNET_FX, IfType.FAST_ETHERNET_FX.isFiberConnector());
        assertTrue(S_F + IfType.GIGABIT_ETHERNET_LX, IfType.GIGABIT_ETHERNET_LX.isFiberConnector());
        assertTrue(S_F + IfType.TEN_GIGABIT_ETHERNET_ER, IfType.TEN_GIGABIT_ETHERNET_ER.isFiberConnector());
    }
    

    @Test
    public void testIsLinkAggregateInterface() {
        assertFalse(SN_LA + IfType.L3_IPX_VLAN, IfType.L3_IPX_VLAN.isLinkAggregateInterface());
        assertTrue(S_LA + IfType.IEEE_802_3AD_LINK_AGGREGATE,
                   IfType.IEEE_802_3AD_LINK_AGGREGATE.isLinkAggregateInterface());
    }
    

    @Test
    public void testIsMultiplexorInterface() {
        assertFalse(SN_M + IfType.REGULAR_1822, IfType.REGULAR_1822.isMultiplexorInterface());
        assertTrue(S_M + IfType.PROPRIETARY_MULTIPLEXOR, IfType.PROPRIETARY_MULTIPLEXOR.isMultiplexorInterface());
    }
    

    @Test
    public void testIsPhysicalPort() {
        assertFalse(SN_P + IfType.IP_FORWARDING, IfType.IP_FORWARDING.isPhysicalPort());
        assertFalse(SN_P + IfType.L2VLAN, IfType.L2VLAN.isPhysicalPort());
        assertFalse(SN_P + IfType.PROPRIETARY_VIRTUAL, IfType.PROPRIETARY_VIRTUAL.isPhysicalPort());
        assertTrue(S_P + IfType.ETHERNET_CSMACD, IfType.ETHERNET_CSMACD.isPhysicalPort());
        assertTrue(S_P + IfType.GIGABIT_ETHERNET, IfType.GIGABIT_ETHERNET.isPhysicalPort());
        assertTrue(S_P + IfType.IEEE_802_11, IfType.IEEE_802_11.isPhysicalPort());
    }
    

    @Test
    public void testIsVirtualInterface() {
        assertFalse(SN_VI + IfType.ETHERNET_CSMACD, IfType.ETHERNET_CSMACD.isVirtualInterface());
        assertFalse(SN_VI + IfType.FAST_ETHERNET, IfType.FAST_ETHERNET.isVirtualInterface());
        assertFalse(SN_VI + IfType.STAR_LAN, IfType.STAR_LAN.isVirtualInterface());
        assertTrue(S_VI + IfType.IP_FORWARDING, IfType.IP_FORWARDING.isVirtualInterface());
        assertTrue(S_VI + IfType.L2VLAN, IfType.L2VLAN.isVirtualInterface());
        assertTrue(S_VI + IfType.PROPRIETARY_VIRTUAL, IfType.PROPRIETARY_VIRTUAL.isVirtualInterface());
    }
    

    @Test
    public void testValueToEnumMappings() {
        assertEquals(ILU + ETHERNET_IF_TYPE, IfType.ETHERNET_CSMACD, IfType.valueOf(ETHERNET_IF_TYPE).get(0));
        assertEquals(ILU + INVALID_IF_TYPE, IfType.UNKNOWN, IfType.valueOf(INVALID_IF_TYPE).get(0));
        assertEquals(ILU + X29_PAD_IF_TYPE, IfType.X29_PAD, IfType.valueOf(X29_PAD_IF_TYPE).get(0));
    }
}