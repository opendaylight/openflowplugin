/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.compute.impl;

import org.opendaylight.util.event.AbstractEventBatcher;
import org.opendaylight.util.NamedThreadFactory;
import org.apache.felix.scr.annotations.*;
import org.opendaylight.net.device.DeviceEvent;
import org.opendaylight.net.device.DeviceListener;
import org.opendaylight.net.device.DeviceService;
import org.opendaylight.net.link.LinkEvent;
import org.opendaylight.net.link.LinkListener;
import org.opendaylight.net.link.LinkService;
import org.opendaylight.net.model.ModelEvent;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.net.topology.TopologySupplier;
import org.opendaylight.net.topology.TopologySupplierService;
import org.opendaylight.net.topology.TopologySuppliersBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Default implementation of a topology supplier.
 *
 * @author Thomas Vachuska
 */
@Component(immediate = true)
public class DefaultTopologySupplier implements TopologySupplier {

    private final Logger log = LoggerFactory.getLogger(DefaultTopologySupplier.class);

    private static final String MSG_STARTED = "DefaultTopologySupplier started";
    private static final String MSG_STOPPED = "DefaultTopologySupplier stopped";

    private final SupplierId SUPPLIER_ID =
            new SupplierId(getClass().getPackage().getName());

    private static final String THREAD_NAME = "net-topo";

    private volatile boolean started = false;

    // TODO: make these configurable
    // Maximum number of events before batch will be triggered for processing
    private int maxEvents = 128;

    // Maximum number of milliseconds since the first event in the batch,
    // before the batch will be triggered for processing
    private long maxMs = 250;

    // Maximum number of milliseconds since the last event before the batch
    // will be triggered for processing
    private long idleMs = 50;

    // Maximum number of topology processing threads
    private int maxThreads = 4;

    private final DeviceMonitor deviceMonitor = new DeviceMonitor();
    private final LinkMonitor linkMonitor = new LinkMonitor();
    private EventBatcher eventBatcher;
    private ExecutorService executor;

    @Override
    public SupplierId supplierId() {
        return SUPPLIER_ID;
    }

    @Reference(name = "DeviceService", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected volatile DeviceService deviceService;

    @Reference(name = "LinkService", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected volatile LinkService linkService;

    @Reference(name = "TopologySuppliersBroker", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected volatile TopologySuppliersBroker broker;

    private TopologySupplierService supplierService;

    @Activate
    public synchronized void activate() {
        executor = newFixedThreadPool(maxThreads, new NamedThreadFactory(THREAD_NAME));
        eventBatcher = new EventBatcher();

        supplierService = broker.registerSupplier(this);
        deviceService.addListener(deviceMonitor);
        linkService.addListener(linkMonitor);
        scheduleTopologyComputation(null);
        started = true;

        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        broker.unregisterSupplier(this);
        deviceService.removeListener(deviceMonitor);
        linkService.removeListener(linkMonitor);

        synchronized (this) {
            executor.shutdownNow();
            executor = null;
        }
        started = false;
        log.info(MSG_STOPPED);
    }

    /**
     * Schedules imminent topology computation.
     *
     * @param reasons list of device and link events
     */
    protected synchronized void scheduleTopologyComputation(List<ModelEvent> reasons) {
        executor.execute(new TopologyBuilder(reasons));
    }

    /**
     * Coordinates topology computation process.
     *
     * @param reasons list of device and link events that triggered this computation
     */
    protected void computeTopology(List<ModelEvent> reasons) {
        if (!started)
            return;

        // Build the graph using the links from the link service
        DefaultTopologyData data = new DefaultTopologyData();
        data.build(deviceService.getDevices(), linkService.getLinks());
        supplierService.submit(data, reasons);
    }

    // Device listener to submit device events for batch processing
    private class DeviceMonitor implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            DeviceEvent.Type type = event.type();
            if (type == DeviceEvent.Type.DEVICE_ADDED ||
                    type == DeviceEvent.Type.DEVICE_REMOVED ||
                    type == DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED)
                eventBatcher.submit(event);
        }
    }

    // Link listener to submit link events for batch processing
    private class LinkMonitor implements LinkListener {
        @Override
        public void event(LinkEvent event) {
            eventBatcher.submit(event);
        }
    }


    // Device and link event batcher & processor
    private class EventBatcher extends AbstractEventBatcher<ModelEvent> {
        private EventBatcher() {
            super(maxEvents, maxMs, idleMs, new Timer());
        }

        @Override
        protected void processBatch(List<ModelEvent> reasons) {
            scheduleTopologyComputation(reasons);
        }
    }

    // Runnable wrapper to host topology computation.
    private class TopologyBuilder implements Runnable {

        private List<ModelEvent> reasons;

        TopologyBuilder(List<ModelEvent> reasons) {
            this.reasons = reasons;
        }

        @Override
        public void run() {
            computeTopology(reasons);
        }
    }

}
