/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.table.miss.enforcer;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link TableMissEnforcer}.
 */
public class TableMissEnforcerManager implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(TableMissEnforcerManager.class);
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final SalFlowService salFlowService;
    private final ConcurrentHashMap<InstanceIdentifier<FlowCapableNode>,
            TableMissEnforcer> tableMissEnforcers = new ConcurrentHashMap();

    public TableMissEnforcerManager(final ClusterSingletonServiceProvider clusterSingletonService,
                                    final SalFlowService salFlowService) {
        this.clusterSingletonService = clusterSingletonService;
        this.salFlowService = salFlowService;
    }

    public void onDeviceConnected(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        final NodeId nodeId = TableMissUtils.retreiveNodeId(nodeIdent);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Table miss enforcer connected node: {}", nodeId.getValue());
        }
        if (!tableMissEnforcers.contains(nodeId)) {
            final TableMissEnforcer tableMissEnforcer = new TableMissEnforcer(
                    nodeIdent,
                    clusterSingletonService,
                    salFlowService);

            tableMissEnforcers.put(nodeIdent, tableMissEnforcer);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Table miss enforcer service registered for node: {}", nodeId.getValue());
            }
        }
    }

    public void onDeviceDisconnected(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        final NodeId nodeId = TableMissUtils.retreiveNodeId(nodeIdent);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Table miss enforcer disconnected node: {}", nodeId.getValue());
        }
        Optional.ofNullable(tableMissEnforcers.remove(nodeIdent)).ifPresent(TableMissEnforcer::close);
    }

    @Override
    public void close() throws Exception {
        tableMissEnforcers.values().forEach(TableMissEnforcer::close);
        tableMissEnforcers.clear();
    }

    @VisibleForTesting
    ConcurrentHashMap<InstanceIdentifier<FlowCapableNode>, TableMissEnforcer> getTableMissEnforcers() {
        return tableMissEnforcers;
    }
}
