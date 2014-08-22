/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device;

import org.opendaylight.util.driver.DefaultDeviceInfo;
import org.opendaylight.util.driver.DefaultDeviceType;
import org.junit.Test;
import org.opendaylight.net.device.DeviceEvent.Type;
import org.opendaylight.net.model.DefaultDevice;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.supplier.SupplierId;

import java.net.URI;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DeviceTest {

    @Test
    public void event() {
        DefaultDevice device = new DefaultDevice(new SupplierId("foo"),
                                                 DeviceId.valueOf("foo"),
                                                 Collections.<URI>emptySet(),
                                                 new MockDeviceInfo());
        DefaultDeviceEvent event = new DefaultDeviceEvent(Type.DEVICE_ADDED,
                                                          device, null);
        assertNotNull(event);
        assertNull(event.netInterface());
    }
    
    private class MockDeviceInfo extends DefaultDeviceInfo {
        public MockDeviceInfo() {
            super(new MockDeviceType());
        }
    }
    private class MockDeviceType extends DefaultDeviceType {
        protected MockDeviceType() {
            super(null, null);
        }
    }
}
