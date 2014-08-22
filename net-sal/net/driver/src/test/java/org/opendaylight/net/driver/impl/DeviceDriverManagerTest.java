/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.driver.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeviceDriverManagerTest {

    // FIXME: please!
    DeviceDriverManager manager;

    private static final String MFG = "Hewlett-Packard";
    private static final String HW  = "3800";
    private static final String FW  = "K.15.04";
    private static final String NAME = "3800";

    @Before
    public void setUp() throws Exception {
        manager = new DeviceDriverManager();
        manager.addTypeName("foo", "box", "wind", "bar");
    }
    
    @Test
    public void constructors() {
        assertNotNull(new DeviceDriverManager());
    }

    @Test
    public void start() {
        manager.activate();
    }

    @Test
    public void stop() {
        manager.deactivate();
    }

    @Test
    public void findTypeName() {
        assertEquals("whatever", "bar", manager.getTypeName("foo", "box", "wind"));
    }

}
