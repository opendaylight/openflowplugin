/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.driver;

import java.util.Set;

import org.opendaylight.util.driver.DeviceHandler;
import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.util.driver.DeviceLoader;
import org.opendaylight.util.driver.MutableDeviceInfo;
import org.opendaylight.util.net.IpAddress;

/**
 * Adapter implementation of the {@link DeviceDriverService}.
 * 
 * @author Sean Humphress
 */
public class DeviceDriverServiceAdapter implements DeviceDriverService {
    @Override public Set<String> getDeviceTypeNames() { return null; }
    @Override public DeviceInfo create(String typeName) { return null; }
    @Override public DeviceHandler create(String typeName, IpAddress ip) { return null; }
    @Override public DeviceHandler create(DeviceInfo info, IpAddress ip) { return null; }
    @Override public DeviceLoader create(String typeName, String uid) { return null; }
    @Override public DeviceLoader create(DeviceInfo info, String uid) { return null; }
    @Override public void switchType(MutableDeviceInfo mutableDeviceInfo, String newTypeName) { }
    @Override public String getTypeName(String mfg, String hw, String fw) { return null; }
}
