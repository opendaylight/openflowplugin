/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device.impl;

import org.opendaylight.util.cache.ReadOnlyIterator;
import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.net.device.DefaultDeviceEvent;
import org.opendaylight.net.device.DeviceEvent;
import org.opendaylight.net.device.DeviceFilter;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.net.BigPortNumber;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.opendaylight.net.device.DeviceEvent.Type.*;

/**
 * Auxiliary facility for tracking and searching through device inventory.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
class DeviceCache {

    private final Map<DeviceId, DefaultDevice> devices = new ConcurrentHashMap<>();
    private final Map<DeviceId, Map<InterfaceId, Interface>> interfaces = new HashMap<>();

    private final Map<URI, Device> devicesByURI = new ConcurrentHashMap<>();
    private final Map<DataPathId, Device> devicesByDpid = new ConcurrentHashMap<>();

    // Comparator for sorting lists of interfaces
    private static final Comparator<Interface> IF_COMPARATOR = new Comparator<Interface>() {
        @Override
        public int compare(Interface a, Interface b) {
            return a.id().port().compareTo(b.id().port());
        }
    };

    /**
     * Returns safe iterator over all devices in the inventory.
     *
     * @return device iterator
     */
    Iterator<Device> getDevices() {
        synchronized (this) {
            return new ReadOnlyIterator<Device, DefaultDevice>(devices.values());
        }
    }

    /**
     * Returns safe iterator over all devices that match the supplied filter.
     *
     * @param filter device filter
     * @return device iterator
     */
    Iterator<Device> getDevices(DeviceFilter filter) {
        List<Device> results = new ArrayList<>();
        synchronized (this) {
            for (Device device : devices.values())
                if (filter.matches(device))
                    results.add(device);
        }
        return results.iterator();
    }

    /**
     * Return device with the supplied id or null if one is not found.
     *
     * @param id device id
     * @return device with the supplied id or null
     */
    Device getDevice(DeviceId id) {
        return devices.get(id);
    }

    /**
     * Returns the device with the specified datapath id.
     *
     * @param dpid datapath id
     * @return device representing the datapath or null
     */
    Device getDevice(DataPathId dpid) {
        return devicesByDpid.get(dpid);
    }

    /**
     * Returns the device with the specified control/management URI.
     *
     * @param uri device URI
     * @return device with matching URI or null
     */
    Device getDevice(URI uri) {
        return devicesByURI.get(uri);
    }

    /**
     * Returns the list of interfaces on the given device.
     *
     * @param device network device
     * @return list of interfaces ordered by interface index
     */
    List<Interface> getInterfaces(Device device) {
        Map<InterfaceId, Interface> difs = interfaces.get(device.id());

        Interface[] a = new Interface[difs.size()];
        int i = 0;
        for (Interface iface : difs.values())
            a[i++] = iface;

        Arrays.sort(a, IF_COMPARATOR);
        return Arrays.asList(a);
    }

    /**
     * Returns the interface with the specified id and on the given device.
     *
     * @param deviceId    device id
     * @param interfaceId interface id
     * @return matching interface or null
     */
    Interface getInterface(DeviceId deviceId, InterfaceId interfaceId) {
        Map<InterfaceId, Interface> difs = interfaces.get(deviceId);
        return difs != null ? difs.get(interfaceId) : null;
    }

    /**
     * Creates or updates network device descriptor and returns the appropriate
     * event.
     *
     * @param supplierId supplier that is creating this device
     * @param deviceId   device id
     * @param uris       control URI
     * @param info       device info descriptor
     * @return event describing the change to device inventory
     */
    DeviceEvent createOrUpdateDevice(SupplierId supplierId, DeviceId deviceId,
                                     Set<URI> uris, DeviceInfo info) {
        DefaultDevice device = devices.get(deviceId);
        if (device == null) {
            return createAndStoreDevice(supplierId, deviceId, uris, info);
        } else {
            return updateDevice(device, uris, info);
        }
    }

    // Adds device to the inventory and indexes and returns a prepared event.
    private DeviceEvent createAndStoreDevice(SupplierId supplierId, DeviceId deviceId,
                                             Set<URI> uris, DeviceInfo info) {
        DefaultDevice device = new DefaultDevice(supplierId, deviceId, uris, info);
        synchronized (this) {
            devices.put(device.id(), device);
            updateIndexes(device);
            return new DefaultDeviceEvent(DEVICE_ADDED, device, null);
        }
    }

    // Updates the device in the inventory and returns a prepared event.
    private DeviceEvent updateDevice(DefaultDevice device, Set<URI> uris,
                                     DeviceInfo info) {
        // Update the set of device URIs
        for (URI uri : uris)
            device.addManagementURI(uri);
        updateIndexes(device);
        // TODO: Consider how to treat info: replace, merge or what?
        return new DefaultDeviceEvent(DEVICE_UPDATED, device, null);
    }

    // Updates lookup indexes for the supplied device
    private void updateIndexes(DefaultDevice device) {
        if (device.dpid() != null)
            devicesByDpid.put(device.dpid(), device);
        for (URI uri : device.managementURIs())
            devicesByURI.put(uri, device);
    }

    /**
     * Removes the specified device from inventory.
     *
     * @param deviceId device id
     * @return list of prepared events describing the change
     */
    List<DeviceEvent> removeDevice(DeviceId deviceId) {
        List<DeviceEvent> events = new ArrayList<>();
        synchronized (this) {
            Device device = devices.remove(deviceId);
            if (device != null) {
                // Remove the device itself from the secondary indexes
                devicesByDpid.remove(device.dpid());
                for (URI uri : device.managementURIs())
                    devicesByURI.remove(uri);
                removeDeviceInterfaces(deviceId, events, device);
            }
            return events;
        }
    }

    // Remove all of device interfaces
    private void removeDeviceInterfaces(DeviceId deviceId,
                                        List<DeviceEvent> events, Device device) {
        // Remove the device interfaces
        Map<InterfaceId, Interface> ifs = interfaces.remove(deviceId);

        // And add events to represent removal of all device interfaces
        Iterator<Interface> it = ifs != null ? ifs.values().iterator() : null;
        while (it != null && it.hasNext())
            events.add(new DefaultDeviceEvent(INTERFACE_REMOVED, device, it.next()));
        events.add(new DefaultDeviceEvent(DEVICE_REMOVED, device, null));
    }

    /**
     * Marks the specified device as online or offline.
     *
     * @param deviceId device id
     * @return prepared event describing the change
     */
    DeviceEvent setOnline(DeviceId deviceId, boolean online) {
        synchronized (this) {
            DefaultDevice device = devices.get(deviceId);
            if (device != null) {
                boolean oldAvailability = device.isOnline();
                device.setOnline(online);
                if (oldAvailability != online)
                    return new DefaultDeviceEvent(DEVICE_AVAILABILITY_CHANGED, device, null);
            }
        }
        return null;
    }

    /**
     * Creates or updates device interfaces.
     *
     * @param deviceId device id
     * @param infos    list of network interfaces
     * @return list of device events describing the change
     */
    List<DeviceEvent> updateInterfaces(DeviceId deviceId,
                                       List<InterfaceInfo> infos) {
        List<DeviceEvent> events = new ArrayList<>();
        synchronized (this) {
            Device device = devices.get(deviceId);
            if (device != null) {
                // Lookup the device interfaces map; if not present, add one
                Map<InterfaceId, Interface> deviceInterfaces = interfaces.get(deviceId);
                if (deviceInterfaces == null) {
                    deviceInterfaces = new HashMap<>();
                    interfaces.put(deviceId, deviceInterfaces);
                }
                Set<BigPortNumber> processedInterfaces = new HashSet<>();

                // First sweep through the new interface info and appropriately
                // add or update the affected interfaces.
                for (InterfaceInfo ii : infos) {
                    DefaultInterface netInterface =
                            (DefaultInterface) deviceInterfaces.get(InterfaceId.valueOf(ii.id()));
                    DeviceEvent event;
                    if (netInterface == null)
                        event = createInterface(device, deviceInterfaces, ii);
                    else
                        event = updateInterface(device, ii, netInterface);

                    // Record the event and mark the interface as processed.
                    if (event != null)
                        events.add(event);
                    processedInterfaces.add(ii.id());
                }

                // Then sweep through the old list of interfaces and remove
                // any that do not have a corresponding interface info in the
                // new list.
                removeOtherInterfaces(device, deviceInterfaces,
                                      processedInterfaces, events);
            }
        }
        return events;
    }

    // Creates and adds a new interface on the specified device
    private DeviceEvent createInterface(Device device,
                                        Map<InterfaceId, Interface> deviceInterfaces,
                                        InterfaceInfo interfaceInfo) {
        InterfaceId id = InterfaceId.valueOf(interfaceInfo.id());
        DefaultInterface netInterface = new DefaultInterface(id, interfaceInfo);
        deviceInterfaces.put(id, netInterface);
        return new DefaultDeviceEvent(INTERFACE_ADDED, device, netInterface);
    }

    // Update an existing interface on the given device.
    private DeviceEvent updateInterface(Device device, InterfaceInfo ii,
                                        DefaultInterface netInterface) {
        DeviceEvent.Type eventType = netInterface.setInfo(ii);
        return eventType != null ?
                new DefaultDeviceEvent(eventType, device, netInterface) : null;
    }

    // Removes any interfaces of a device, which do not appear among the
    // processed interfaces set
    private void removeOtherInterfaces(Device device,
                                       Map<InterfaceId, Interface> deviceInterfaces,
                                       Set<BigPortNumber> processedInterfaces,
                                       List<DeviceEvent> events) {
        Iterator<InterfaceId> it = deviceInterfaces.keySet().iterator();
        while (it.hasNext()) {
            InterfaceId interfaceId = it.next();
            if (!processedInterfaces.contains(interfaceId.port())) {
                events.add(new DefaultDeviceEvent(INTERFACE_REMOVED, device,
                                                  deviceInterfaces.get(interfaceId)));
                it.remove();
            }
        }
    }

}
