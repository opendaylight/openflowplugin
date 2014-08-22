/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.impl;

import org.opendaylight.util.api.ServiceNotFoundException;
import org.opendaylight.util.event.AbstractEventSink;
import org.opendaylight.util.event.EventDispatchService;
import org.apache.felix.scr.annotations.*;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.AbstractSupplierService;
import org.opendaylight.net.supplier.AbstractSuppliersBroker;
import org.opendaylight.net.topology.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Topology service in-memory implementation
 *
 * @author Thomas Vachuska
 */
@Component(immediate = true)
@Service
public class TopologyManager
        extends AbstractSuppliersBroker<TopologySupplier, TopologySupplierService>
        implements TopologyService, TopologySuppliersBroker {

    private final Logger log = LoggerFactory.getLogger(TopologyManager.class);

    private static final String E_NO_TOPO = "No topology data is available";
    private static final String MSG_STARTED = "TopologyManager started";
    private static final String MSG_STOPPED = "TopologyManager stopped";
    private static final String E_NEW_TOPO = "Received new topology: {} due to: {}";
    private static final String E_UNHANDLED_ERROR = "Unhandled error";

    private final ListenerManager listenerManager = new ListenerManager();

    private DefaultTopology topo;

    @Reference(name = "EventDispatchService", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EventDispatchService dispatchService;

    @Activate
    public void activate() {
        dispatchService.addSink(TopologyEvent.class, listenerManager);
        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        dispatchService.removeSink(TopologyEvent.class);
        log.info(MSG_STOPPED);
    }

    @Override
    public Topology getTopology() {
        assertTopologyData();
        return topo;
    }

    @Override
    public boolean isPathViable(DeviceId src, DeviceId dst) {
        notNull(src, dst);
        assertTopologyData();
        return topo.isPathViable(src, dst);
    }

    @Override
    public Set<Path> getPaths(DeviceId src, DeviceId dst) {
        notNull(src, dst);
        assertTopologyData();
        return topo.getPaths(src, dst);
    }

    @Override
    public Set<Path> getPaths(DeviceId src, DeviceId dst, LinkWeight weight) {
        notNull(src, dst, weight);
        assertTopologyData();
        return topo.getPaths(src, dst, weight);
    }

    @Override
    public boolean isInfrastructure(ConnectionPoint point) {
        notNull(point);
        assertTopologyData();
        return topo.isInfrastructure(point);
    }

    @Override
    public boolean isBroadcastAllowed(ConnectionPoint point) {
        notNull(point);
        assertTopologyData();
        return topo.isBroadcastAllowed(point);
    }

    @Override
    public Set<TopologyCluster> getClusters() {
        assertTopologyData();
        return topo.getClusters();
    }

    @Override
    public TopologyCluster getCluster(DeviceId deviceId) {
        notNull(deviceId);
        assertTopologyData();
        return topo.getCluster(deviceId);
    }

    @Override
    public Set<DeviceId> getClusterDevices(TopologyCluster cluster) {
        notNull(cluster);
        assertTopologyData();
        return topo.getClusterDevices(cluster);
    }

    @Override
    public void addListener(TopologyListener listener) {
        listenerManager.addListener(listener);
    }

    @Override
    public void removeListener(TopologyListener listener) {
        listenerManager.removeListener(listener);
    }

    @Override
    public Set<TopologyListener> getListeners() {
        return listenerManager.getListeners();
    }

    // Asserts that topology data is available.
    private void assertTopologyData() {
        if (topo == null)
            throw new ServiceNotFoundException(E_NO_TOPO);
    }

    @Override
    protected TopologySupplierService createSupplierService(TopologySupplier supplier) {
        return new InnerTopologySupplierService(supplier);
    }

    // Mechanism for suppliers to submit and activate a new topology.
    private class InnerTopologySupplierService extends AbstractSupplierService
            implements TopologySupplierService {

        private final TopologySupplier supplier;

        private InnerTopologySupplierService(TopologySupplier supplier) {
            this.supplier = supplier;
        }

        @Override
        public void submit(TopologyData topologyData, List<ModelEvent> reasons) {
            validate();
            notNull(topologyData);

            // Make sure that the submitted data is newer than the already
            // active topology data.
            if (topo != null && topo.data().ts() > topologyData.ts())
                return;

            topo = new DefaultTopology(supplier.supplierId(), topologyData,
                                       System.currentTimeMillis());
            log.info(E_NEW_TOPO, topo, reasons == null ? "initial" : reasons);
            dispatchService.post(new DefaultTopologyEvent(topo, reasons));
       }
    }

    // Mechanism for tracking topology listeners and dispatching topology
    // events to them.
    private class ListenerManager
            extends AbstractEventSink<TopologyEvent, TopologyListener> {

        @Override
        protected void dispatch(TopologyEvent event, TopologyListener listener) {
            listener.event(event);
        }

        @Override
        protected void reportError(TopologyEvent event, TopologyListener listener,
                                   Throwable error) {
            log.warn(E_UNHANDLED_ERROR, error);
        }
    }

}
