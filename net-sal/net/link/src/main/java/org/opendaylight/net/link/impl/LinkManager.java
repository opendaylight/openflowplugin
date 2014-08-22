/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link.impl;

import org.opendaylight.util.event.AbstractEventSink;
import org.opendaylight.util.event.EventDispatchService;
import org.apache.felix.scr.annotations.*;
import org.opendaylight.net.link.*;
import org.opendaylight.net.model.ConnectionPoint;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.Link;
import org.opendaylight.net.model.LinkInfo;
import org.opendaylight.net.supplier.AbstractSupplierService;
import org.opendaylight.net.supplier.AbstractSuppliersBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Provides the implementation of the {@link LinkService}.
 * <p/>
 * Manages the cache of link information. Informs registered listeners of
 * updates to the link cache.
 *
 * @author Marjorie Krueger
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
@Component(immediate = true)
@Service
public class LinkManager
        extends AbstractSuppliersBroker<LinkSupplier, LinkSupplierService>
        implements LinkService, LinkSuppliersBroker {

    private final Logger log = LoggerFactory.getLogger(LinkManager.class);

    private static final String MSG_STARTED = "LinkManager started";
    private static final String MSG_STOPPED = "LinkManager stopped";
    private static final String E_UNHANDLED_ERROR = "Unhandled error";

    private final ListenerManager listenerManager = new ListenerManager();

    private final LinkCache cache = new LinkCache();

    @Reference(name = "EventDispatchService", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EventDispatchService dispatchService;


    @Activate
    public void activate() {
        dispatchService.addSink(LinkEvent.class, listenerManager);
        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        dispatchService.removeSink(LinkEvent.class);
        log.info(MSG_STOPPED);
    }

    @Override
    public Iterator<Link> getLinks() {
        return cache.getLinks();
    }

    @Override
    public Set<Link> getLinks(DeviceId deviceId) {
        notNull(deviceId);
        return cache.getLinks(deviceId);
    }

    @Override
    public Set<Link> getLinks(DeviceId deviceA, DeviceId deviceB) {
        notNull(deviceA, deviceB);
        return cache.getLinks(deviceA, deviceB);
    }

    @Override
    public Set<Link> getLinksFrom(DeviceId deviceId) {
        notNull(deviceId);
        return cache.getLinksFrom(deviceId);
    }

    @Override
    public Set<Link> getLinksTo(DeviceId deviceId) {
        notNull(deviceId);
        return cache.getLinksTo(deviceId);
    }

    @Override
    public Set<Link> getLinks(ConnectionPoint cp) {
        notNull(cp);
        return cache.getLinks(cp);
    }

    @Override
    public Set<Link> getLinksFrom(ConnectionPoint src) {
        notNull(src);
        return cache.getLinksFrom(src);
    }

    @Override
    public Set<Link> getLinksTo(ConnectionPoint dst) {
        notNull(dst);
        return cache.getLinksTo(dst);
    }

    @Override
    public void addListener(LinkListener listener) {
        listenerManager.addListener(listener);
    }

    @Override
    public void removeListener(LinkListener listener) {
        listenerManager.removeListener(listener);
    }

    @Override
    public Set<LinkListener> getListeners() {
        return listenerManager.getListeners();
    }

    @Override
    protected LinkSupplierService createSupplierService(LinkSupplier supplier) {
        return new InnerLinkSupplierService(supplier);
    }

    // Safely posts an event only if the event is not null and is a valid event
    private void postEvent(LinkEvent event) {
        if (event != null && !(event instanceof NoOpLinkEvent))
            dispatchService.post(event);
    }

    // Mechanism for suppliers to manipulate link inventory.
    private class InnerLinkSupplierService
            extends AbstractSupplierService implements LinkSupplierService {
        private final LinkSupplier supplier;

        public InnerLinkSupplierService(LinkSupplier supplier) {
            this.supplier = supplier;
        }

        @Override
        public Link createOrUpdateLink(ConnectionPoint src, ConnectionPoint dst,
                                       LinkInfo linkInfo) {
            validate();
            notNull(src, dst, linkInfo);
            LinkEvent event = cache.createOrUpdateLink(supplier.supplierId(),
                                                       src, dst, linkInfo);
            postEvent(event);
            return event.subject();
        }

        @Override
        public void removeLink(ConnectionPoint src, ConnectionPoint dst) {
            validate();
            notNull(src, dst);
            postEvent(cache.removeLink(src, dst));
        }

        @Override
        public void removeAllLinks(DeviceId deviceId) {
            validate();
            notNull(deviceId);
            for (Link link : getLinks(deviceId))
                postEvent(cache.removeLink(link.src(), link.dst()));
        }

        @Override
        public void removeAllLinks(ConnectionPoint cp) {
            validate();
            notNull(cp);
            for (Link link : getLinks(cp))
                postEvent(cache.removeLink(link.src(), link.dst()));
        }
    }


    // Mechanism for tracking Link listeners and dispatching events to them.
    private class ListenerManager
            extends AbstractEventSink<LinkEvent, LinkListener> {

        @Override
        protected void dispatch(LinkEvent event, LinkListener listener) {
            listener.event(event);
        }

        @Override
        protected void reportError(LinkEvent event, LinkListener listener,
                                   Throwable error) {
            log.warn(E_UNHANDLED_ERROR, error);
        }
    }

}
