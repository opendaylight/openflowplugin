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
 * Provides information about infrastructure devices known to the system.
 *
 * @author Thomas Vachuska
 * @author Shaun Humphress
 * @author Julie Britt
 * @author Steve Dean
 */
public interface DeviceService {

    /**
     * Returns set of all devices known to the system.
     *
     * @return set of all devices
     */
    Iterator<Device> getDevices();

    /**
     * Returns set of all devices that match the supplied filter.
     *
     * @param filter device filter
     * @return set of all devices
     */
    Iterator<Device> getDevices(DeviceFilter filter);

    /**
     * Returns the device with the specified id.
     *
     * @param id device id
     * @return device, null if not found
     */
    Device getDevice(DeviceId id);

    /**
     * Returns the device with the specified management IP address.
     *
     * @param deviceIp management IP
     * @return devices, empty set if none found
     */
    Set<Device> getDevices(IpAddress deviceIp);

    /**
     * Returns the device with the specified management Data Path Id (dpid).
     *
     * @param dpid management Data Path Id
     * @return device, null if not found
     */
    Device getDevice(DataPathId dpid);

    /**
     * Returns the device with the specified URI.
     *
     * @param uri URI identifier
     * @return device, null if not found
     */
    Device getDevice(URI uri);

    /**
     * Returns a list of all devices that support the specified device driver
     * facet.
     *
     * @param facetClass facet class to filter by
     * @return device iterator, empty iterator if none found
     */
    Iterator<Device> getDevices(Class<? extends Facet> facetClass);

    /**
     * Returns the list of interfaces hosted on this device.
     *
     * @param device device descriptor
     * @return list of interfaces/ports
     */
    List<Interface> getInterfaces(Device device);

    /**
     * Returns the interface requested with the device and interface ids.
     *
     * @param deviceId device id
     * @param interfaceId interface id
     * @return requested interface, null if not found
     */
    Interface getInterface(DeviceId deviceId, InterfaceId interfaceId);

    /**
     * Add the specified listener...
     *
     * @param listener listener to be added
     */
    void addListener(DeviceListener listener);

    /**
     * Remove the specified listener...
     *
     * @param listener listener to be removed
     */
    void removeListener(DeviceListener listener);

    /**
     * Returns the set of all device update listeners.
     *
     * @return all device listeners
     */
    Set<DeviceListener> getListeners();

    /**
     * Assign a user provided friendly name to the device. This does not
     * involve any device interactions. Instead, it simply labels the device
     * in the inventory for better readability.
     *
     * @param device to modify
     * @param name user provided friendly name
     * @throws org.opendaylight.util.api.NotFoundException if device not found
     */
    void setName(Device device, String name);

}

