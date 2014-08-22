/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.driver.DefaultDeviceInfo;
import org.opendaylight.util.driver.DefaultDeviceType;
import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.net.supplier.SupplierId;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.of.lib.dt.DataPathId;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;


public class DefaultDeviceTest {

    private DeviceId deviceId = DeviceId.valueOf("fingerprint");
    private DeviceInfo info;
    private Set<URI> uris = Collections.<URI>emptySet();
    private SupplierId supplierId = new SupplierId("supplier");
    private DefaultDevice device;
    private DataPathId dpid = DataPathId.valueOf("10:00:1c:c1:de:4d:49:80");

    @Before
    public void setup() {
        info = new MockDeviceInfo();
        device = new DefaultDevice(supplierId, deviceId, uris, info);
    }

    @Test
    public void nullParams() {
        try {
            new DefaultDevice(supplierId, null, uris, info);
            fail();
        } catch (NullPointerException e) {}

        try {
            new DefaultDevice(supplierId, deviceId, null, info);
            fail();
        } catch (NullPointerException e) {}

        try {
            new DefaultDevice(supplierId, deviceId, uris, null);
            fail();
        } catch (NullPointerException e) {}

    }

    @Test
    public void info() {
        assertEquals(device.info(), info);
    }

    @Test
    public void id() {
        assertEquals(device.id(), deviceId);
    }

    @Test
    public void supplier() {
        assertEquals(device.supplierId(), supplierId);
    }

    @Test
    public void online() {
        assertTrue(device.isOnline());
        device.setOnline(false);
        assertFalse(device.isOnline());
    }
    
    @Test
    public void uri() {
        assertEquals(device.managementURIs().size(), 0);
        assertNull(device.dpid());
        URI uri = createURI(dpid.toString());
        device.addManagementURI(uri);
        Set<URI> uris = device.managementURIs();
        assertEquals(uris.size(), 1);
        assertTrue(uris.contains(uri));

        assertEquals(device.dpid(), dpid);
    }

    private URI createURI(String dpid) {
        try {
            return new URI("of", dpid, null);
        } catch (URISyntaxException e) {
            fail();
        }
        return null;
    }
    
    // TODO: Not implemented yet
    @Test
    public void realizedBy() {
        assertNull(device.realizedBy());
    }
    @Test
    public void name() {
        assertNull(device.name());
        device.setName("foo");
        assertEquals("foo", device.name());
    }
    @Test
    public void type() {
        assertNull(device.type());
    }

    @Test
    public void equals() {
        DefaultDevice newDevice = new DefaultDevice(supplierId, deviceId, uris, info);
        assertTrue(device.equals(newDevice));

        newDevice.setOnline(false);
        assertFalse(device.equals(newDevice));
        newDevice.setOnline(true);

        newDevice.setName("name");
        assertFalse(device.equals(newDevice));
        assertFalse(newDevice.equals(device));
        newDevice.setName(null);

        newDevice.addManagementURI(createURI(dpid.toString()));
        assertFalse(device.equals(newDevice));
        device.addManagementURI(createURI("10:00:1c:c1:de:4d:49:81"));
        assertFalse(device.equals(newDevice));
    }

    @Test
    public void hash() {
        Set<Device> set = new HashSet<Device>();
        set.add(device);
        assertTrue(set.contains(device));
    }

    private class MockDeviceType extends DefaultDeviceType {
        protected MockDeviceType() {
            super(null, "testType");
        }
    }

    private class MockDeviceInfo extends DefaultDeviceInfo {
        public MockDeviceInfo() {
            super(new MockDeviceType());
        }
        @Override public String exportData()  { return "export"; }
        @Override public boolean importData(String data) { return false; }
    }

}
