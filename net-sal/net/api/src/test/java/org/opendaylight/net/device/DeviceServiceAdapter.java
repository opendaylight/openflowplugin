/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device;

import org.opendaylight.util.driver.Facet;
import org.opendaylight.net.model.Device;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.Interface;
import org.opendaylight.net.model.InterfaceId;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.of.lib.dt.DataPathId;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An adapter for the {@link org.opendaylight.net.device.DeviceService} API,
 * provided specifically for unit tests and implementers to use, to
 * insulate from changes in the API.
 *
 * @author Shaun Wackerly
 */
public class DeviceServiceAdapter implements DeviceService {

    @Override public Iterator<Device> getDevices() { return null; }
    @Override public Iterator<Device> getDevices(DeviceFilter filter) { return null; }
    @Override public Device getDevice(DeviceId id) { return null; }
    @Override public Set<Device> getDevices(IpAddress deviceIp) { return null; }
    @Override public Device getDevice(DataPathId dpid) { return null; }
    @Override public Device getDevice(URI uri) { return null; }
    @Override public Iterator<Device> getDevices(Class<? extends Facet> facetClass) { return null; }
    @Override public List<Interface> getInterfaces(Device device) { return null; }
    @Override public Interface getInterface(DeviceId deviceId, InterfaceId interfaceId) { return null; }
    @Override public void addListener(DeviceListener listener) { }
    @Override public void removeListener(DeviceListener listener) { }
    @Override public Set<DeviceListener> getListeners() { return null; }
    @Override public void setName(Device device, String name) { }

}
