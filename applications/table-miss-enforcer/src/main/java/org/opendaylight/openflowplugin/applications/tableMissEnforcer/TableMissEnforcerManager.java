/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.tableMissEnforcer;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link TableMissEnforcer}.
 */
public class TableMissEnforcerManager implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(TableMissEnforcerManager.class);
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final SalFlowService salFlowService;
    private final ConcurrentHashMap<NodeId, TableMissEnforcer> tableMissEnforcers = new ConcurrentHashMap();

    public TableMissEnforcerManager(final ClusterSingletonServiceProvider clusterSingletonService,
                                    final SalFlowService salFlowService) {
        this.clusterSingletonService = clusterSingletonService;
        this.salFlowService = salFlowService;
    }

    public void onDeviceConnected(final DataTreeModification modification) {
        final NodeId nodeId = TableMissUtils.retreiveNodeId(modification);
        if (!tableMissEnforcers.contains(nodeId)) {
            final TableMissEnforcer tableMissEnforcer = new TableMissEnforcer(nodeId, clusterSingletonService, salFlowService);
            tableMissEnforcers.put(nodeId, tableMissEnforcer);
            LOG.debug("Table miss enforcer service registered for node: {}", nodeId.getValue());
        }
    }

    public void onDeviceDisconnected(final DataTreeModification modification) {
        final NodeId nodeId = TableMissUtils.retreiveNodeId(modification);
        Optional.ofNullable(tableMissEnforcers.remove(nodeId)).ifPresent(TableMissEnforcer::close);
        LOG.debug("Table miss enforcer service unregistered for node: {}", nodeId.getValue());
    }

    @Override
    public void close() throws Exception {
        tableMissEnforcers.values().forEach(TableMissEnforcer::close);
        tableMissEnforcers.clear();
    }
}
