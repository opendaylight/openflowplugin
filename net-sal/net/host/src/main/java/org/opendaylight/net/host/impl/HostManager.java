/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host.impl;

import org.apache.felix.scr.annotations.*;
import org.opendaylight.net.host.*;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.AbstractSupplierService;
import org.opendaylight.net.supplier.AbstractSuppliersBroker;
import org.opendaylight.util.event.AbstractEventSink;
import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Base implementation of the network host service.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
@Component(immediate = true)
@Service
public class HostManager
        extends AbstractSuppliersBroker<HostSupplier, HostSupplierService>
        implements HostService, HostSuppliersBroker {

    private final Logger log = LoggerFactory.getLogger(HostManager.class);

    private static final String MSG_STARTED = "HostManager started";
    private static final String MSG_STOPPED = "HostManager stopped";
    private static final String E_UNHANDLED_ERROR = "Unhandled error";

    private final HostCache cache = new HostCache();

    private final ListenerManager listenerManager = new ListenerManager();

    @Reference(name = "EventDispatchService", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EventDispatchService dispatchService;

    @Activate
    public void activate() {
        dispatchService.addSink(HostEvent.class, listenerManager);
        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        dispatchService.removeSink(HostEvent.class);
        log.info(MSG_STOPPED);
    }

    @Override
    public String toString() {
        return "HostManager{" +
                "cacheSize=" + cache.size() +
                ", listenerCount=" + getListeners().size() +
                '}';
    }

    @Override
    public Iterator<Host> getHosts() {
        return cache.getHosts();
    }

    @Override
    public Host getHost(HostId id) {
        notNull(id);
        return cache.getHost(id);
    }

    @Override
    public Iterator<Host> getHosts(SegmentId segmentId) {
        notNull(segmentId);
        return cache.getHosts(segmentId);
    }

    @Override
    public Set<Host> getHosts(IpAddress ip) {
        notNull(ip);
        return cache.getHosts(ip);
    }

    @Override
    public Set<Host> getHosts(MacAddress mac, SegmentId segmentId) {
        notNull(mac, segmentId);
        return cache.getHosts(mac, segmentId);
    }

    @Override
    public Set<Host> getHosts(ConnectionPoint cp) {
        notNull(cp);
        return cache.getHosts(cp);
    }

    @Override
    public Set<Host> getHosts(DeviceId device) {
        notNull(device);
        return cache.getHosts(device);
    }

    @Override
    public Iterator<Host> getHosts(HostFilter filter) {
        notNull(filter);
        return cache.getHosts(filter);
    }

    @Override
    public void addListener(HostListener listener) {
        listenerManager.addListener(listener);
    }

    @Override
    public void removeListener(HostListener listener) {
        listenerManager.removeListener(listener);
    }

    @Override
    public Set<HostListener> getListeners() {
        return listenerManager.getListeners();
    }

    @Override
    protected HostSupplierService createSupplierService(HostSupplier supplier) {
        return new InnerHostSupplierService(supplier);
    }

    // Safely posts an event only if the event is not null.
    private void postEvent(HostEvent event) {
        if (event != null)
            dispatchService.post(event);
    }

    // Mechanism for suppliers to submit information about hosts in the network.
    private class InnerHostSupplierService extends AbstractSupplierService
            implements HostSupplierService {

        private final HostSupplier supplier;

        private InnerHostSupplierService(HostSupplier supplier) {
            this.supplier = supplier;
        }

        @Override
        public Host createOrUpdateHost(HostId id, HostInfo info) {
            validate();
            notNull(id, info);
            HostEvent event = cache.createOrUpdateHost(supplier.supplierId(), id, info);
            postEvent(event);
            return event.subject();
        }

        @Override
        public void removeHost(HostId id) {
            validate();
            notNull(id);
            postEvent(cache.removeHost(id));
        }
    }

    // Mechanism for tracking host listeners and dispatching events to them.
    private class ListenerManager
            extends AbstractEventSink<HostEvent, HostListener> {

        @Override
        protected void dispatch(HostEvent event, HostListener listener) {
            listener.event(event);
        }

        @Override
        protected void reportError(HostEvent event, HostListener listener,
                                   Throwable error) {
            log.warn(E_UNHANDLED_ERROR, error);
        }
    }

}
