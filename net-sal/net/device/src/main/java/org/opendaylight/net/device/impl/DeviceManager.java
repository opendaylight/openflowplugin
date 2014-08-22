/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device.impl;

import org.opendaylight.util.driver.DeviceIdentity;
import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.util.driver.Facet;
import org.opendaylight.util.event.AbstractEventSink;
import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.net.IpAddress;
import org.apache.felix.scr.annotations.*;
import org.opendaylight.net.device.*;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.AbstractSupplierService;
import org.opendaylight.net.supplier.AbstractSuppliersBroker;
import org.opendaylight.of.lib.dt.DataPathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Base implementation of the network device service.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
@Component(immediate = true)
@Service
public class DeviceManager
        extends AbstractSuppliersBroker<DeviceSupplier, DeviceSupplierService>
        implements DeviceService, DeviceSuppliersBroker {

    private final Logger log = LoggerFactory.getLogger(DeviceManager.class);

    private static final String MSG_STARTED = "DeviceManager started";
    private static final String MSG_STOPPED = "DeviceManager stopped";
    private static final String E_UNHANDLED_ERROR = "Unhandled error";
    private static final String E_DEVICE_FOREIGN = "Device is foreign";

    private final DeviceCache cache = new DeviceCache();

    private final ListenerManager listenerManager = new ListenerManager();

    @Reference(name = "EventDispatchService", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EventDispatchService dispatchService;


    @Activate
    public void activate() {
        dispatchService.addSink(DeviceEvent.class, listenerManager);
        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        dispatchService.removeSink(DeviceEvent.class);
        log.info(MSG_STOPPED);
    }

    @Override
    public Iterator<Device> getDevices() {
        return cache.getDevices();
    }

    @Override
    public Iterator<Device> getDevices(DeviceFilter filter) {
        notNull(filter);
        return cache.getDevices(filter);
    }

    @Override
    public Device getDevice(DeviceId id) {
        notNull(id);
        return cache.getDevice(id);
    }

    @Override
    public Set<Device> getDevices(IpAddress deviceIp) {
        notNull(deviceIp);
        Set<Device> set = new HashSet<>();
        Iterator<Device> it = getDevices(new DeviceByIpFilter(deviceIp));
        while (it.hasNext())
            set.add(it.next());
        return set;
    }

    @Override
    public Device getDevice(DataPathId dpid) {
        notNull(dpid);
        return cache.getDevice(dpid);
    }

    @Override
    public Device getDevice(URI uri) {
        notNull(uri);
        return cache.getDevice(uri);
    }

    @Override
    public Iterator<Device> getDevices(Class<? extends Facet> facetClass) {
        notNull(facetClass);
        return getDevices(new DeviceByFacetFilter(facetClass));
    }

    @Override
    public List<Interface> getInterfaces(Device device) {
        notNull(device);
        return cache.getInterfaces(device);
    }

    @Override
    public Interface getInterface(DeviceId deviceId, InterfaceId interfaceId) {
        notNull(deviceId, interfaceId);
        return cache.getInterface(deviceId, interfaceId);
    }

    @Override
    public void setName(Device device, String name) {
        notNull(device);
        if (!(device instanceof DefaultDevice))
            throw new IllegalArgumentException(E_DEVICE_FOREIGN);
        DefaultDevice dd = (DefaultDevice) device;
        dd.setName(name);
    }


    @Override
    public void addListener(DeviceListener listener) {
        listenerManager.addListener(listener);
    }

    @Override
    public void removeListener(DeviceListener listener) {
        listenerManager.removeListener(listener);
    }

    @Override
    public Set<DeviceListener> getListeners() {
        return listenerManager.getListeners();
    }


    // Auxiliary filter to find devices by IP address
    private class DeviceByIpFilter implements DeviceFilter {
        private final IpAddress ip;

        public DeviceByIpFilter(IpAddress ip) {
            this.ip = ip;
        }

        @Override
        public boolean matches(Device device) {
            DeviceIdentity identity = device.info().getFacet(DeviceIdentity.class);
            return identity != null && identity.getIpAddress().equals(ip);
        }
    }

    // Auxiliary filter to find devices by facet class
    private class DeviceByFacetFilter implements DeviceFilter {
        private final Class<? extends Facet> facetClass;

        public DeviceByFacetFilter(Class<? extends Facet> facetClass) {
            this.facetClass = facetClass;
        }

        @Override
        public boolean matches(Device device) {
            return device.info().isSupported(facetClass);
        }
    }

    @Override
    protected DeviceSupplierService createSupplierService(DeviceSupplier supplier) {
        return new InnerDeviceSupplierService(supplier);
    }

    // Safely posts an event only if the event is not null.
    private void postEvent(DeviceEvent event) {
        if (event != null)
            dispatchService.post(event);
    }

    // Mechanism for suppliers to submit information about devices in the network.
    private class InnerDeviceSupplierService extends AbstractSupplierService
            implements DeviceSupplierService {

        private final DeviceSupplier supplier;

        private InnerDeviceSupplierService(DeviceSupplier supplier) {
            this.supplier = supplier;
        }

        @Override
        public Device createOrUpdateDevice(DeviceId deviceId, Set<URI> uris, DeviceInfo info) {
            validate();
            notNull(deviceId, uris, info);
            DeviceEvent event = cache.createOrUpdateDevice(supplier.supplierId(),
                                                           deviceId, uris, info);
            postEvent(event);
            return event.subject();
        }

        @Override
        public void removeDevice(DeviceId deviceId) {
            validate();
            notNull(deviceId);
            for (DeviceEvent event : cache.removeDevice(deviceId))
                postEvent(event);
        }

        @Override
        public void setOnline(DeviceId deviceId, boolean online) {
            validate();
            notNull(deviceId);
            postEvent(cache.setOnline(deviceId, online));
        }

        @Override
        public void updateInterfaces(DeviceId deviceId, List<InterfaceInfo> infos) {
            validate();
            notNull(deviceId);
            for (DeviceEvent event : cache.updateInterfaces(deviceId, infos))
                postEvent(event);
        }

    }

    // Mechanism for tracking device listeners and dispatching events to them.
    private class ListenerManager
            extends AbstractEventSink<DeviceEvent, DeviceListener> {

        @Override
        protected void dispatch(DeviceEvent event, DeviceListener listener) {
            listener.event(event);
        }

        @Override
        protected void reportError(DeviceEvent event, DeviceListener listener,
                                   Throwable error) {
            log.warn(E_UNHANDLED_ERROR, error);
        }
    }

}
