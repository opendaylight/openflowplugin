/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.driver.base.impl;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.net.driver.DeviceDriverSuppliersBroker;
import org.opendaylight.net.driver.impl.DeviceDriverManager;
import org.opendaylight.net.facet.FlowModAdjuster;
import org.opendaylight.util.driver.DeviceHandler;
import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.util.net.IpAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.util.CommonUtils.itemSet;

/**
 * Set of tests for the base driver provider.
 *
 * @author Uyen Chau
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public class BaseDriverProviderTest {

    public static final String SWITCH_OPENFLOW = "switch.openflow";
    private static final IpAddress IP = IpAddress.LOOPBACK_IPv4;
    private final DeviceDriverSuppliersBroker broker = new DeviceDriverManager();
    private BaseDriverProvider driver;

    @Before
    public void setUp() {
        driver = new BaseDriverProvider();
        driver.broker = broker;
        driver.activate();
    }

    @Test
    public void basic() {
        System.out.println(driver);
        assertEquals("incorrect device types", itemSet(SWITCH_OPENFLOW),
                     broker.getDeviceTypeNames());

        DeviceInfo info = broker.create(SWITCH_OPENFLOW);
        assertEquals("incorrect facet count", 2, info.getFacetClasses().size());

        DeviceHandler handler = broker.create(SWITCH_OPENFLOW, IP);
        assertEquals("incorrect handler facet count", 1, handler.getFacetClasses().size());

        assertTrue("should support flow mod facet", info.isSupported(FlowModAdjuster.class));
    }

}
