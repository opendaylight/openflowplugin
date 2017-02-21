/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.tableMissEnforcer;

import static org.opendaylight.openflowplugin.applications.tableMissEnforcer.TableMissUtils.DEFAULT_FLOW_ID;
import static org.opendaylight.openflowplugin.applications.tableMissEnforcer.TableMissUtils.TABLE_ID;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService} clusterSingletonServiceRegistration per connected device.
 */
public class TableMissEnforcer implements ClusterSingletonService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(TableMissEnforcer.class);

    private final NodeId nodeId;
    private final ServiceGroupIdentifier identifier;
    private final ClusterSingletonServiceRegistration clusterSingletonServiceRegistration;
    private final InstanceIdentifier<FlowCapableNode> instanceIdentifier;
    private final SalFlowService flowService;

    public TableMissEnforcer(final InstanceIdentifier<FlowCapableNode> instanceIdentifier,
                             final ClusterSingletonServiceProvider clusterSingletonService,
                             final SalFlowService flowService) {
        this.nodeId = TableMissUtils.retreiveNodeId(instanceIdentifier);
        this.identifier = ServiceGroupIdentifier.create(nodeId.getValue());
        this.instanceIdentifier = instanceIdentifier;
        this.flowService = flowService;
        clusterSingletonServiceRegistration = clusterSingletonService.registerClusterSingletonService(this);
    }

    @Override
    public void instantiateServiceInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Table miss flow instantiateServiceInstance: {}", nodeId.getValue());
        }
        final AddFlowInput addFlowInput = new AddFlowInputBuilder(TableMissUtils.createFlow())
                .setNode(new NodeRef(instanceIdentifier.firstIdentifierOf(Node.class)))
                .setFlowRef(new FlowRef(
                        instanceIdentifier
                                .child(Table.class, new TableKey(TABLE_ID))
                                .child(Flow.class, new FlowKey(new FlowId(DEFAULT_FLOW_ID)))))
                .build();

        Futures.lazyTransform(flowService.addFlow(addFlowInput), result -> {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Table miss flow added for node: {}", nodeId.getValue());
            }
            return result;
        });
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        return Futures.immediateFuture(null);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    @SuppressWarnings("checkstyle:illegalcatch")
    public void close() {
        if (clusterSingletonServiceRegistration != null) {
            try {
                clusterSingletonServiceRegistration.close();
            } catch (Exception ex) {
                LOG.warn("Table miss enforcer cluster service close fail: {} {}", nodeId.getValue(), ex);
            }
        }
    }
}
