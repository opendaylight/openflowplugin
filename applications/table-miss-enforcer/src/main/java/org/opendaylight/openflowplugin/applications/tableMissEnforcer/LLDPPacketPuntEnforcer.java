/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.tableMissEnforcer;

import static org.opendaylight.openflowplugin.applications.tableMissEnforcer.TableMissUtils.isAdd;
import static org.opendaylight.openflowplugin.applications.tableMissEnforcer.TableMissUtils.isDelete;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLDPPacketPuntEnforcer implements AutoCloseable, ClusteredDataTreeChangeListener<FlowCapableNode>, BindingAwareProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPPacketPuntEnforcer.class);
    private final SalFlowService flowService;
    private final DataBroker dataBroker;
    private final ClusterSingletonServiceProvider clusterSingletonService;

    private TableMissEnforcerManager tableMissEnforcerManager;
    private ListenerRegistration<LLDPPacketPuntEnforcer> dataTreeChangeRegistration;

    public LLDPPacketPuntEnforcer(final BindingAwareBroker bindingAwareBroker,
                                  final DataBroker dataBroker,
                                  final ClusterSingletonServiceProvider clusterSingletonService,
                                  final SalFlowService flowService) {
        this.flowService = flowService;
        this.dataBroker = dataBroker;
        this.clusterSingletonService = clusterSingletonService;

        if (Objects.nonNull(bindingAwareBroker.registerProvider(this))) {
            LOG.debug("Table miss enforcer binding aware listener successfully registered.");
        }
    }

    @Override
    public void onSessionInitiated(@Nonnull final ProviderContext providerContext) {
        tableMissEnforcerManager = new TableMissEnforcerManager(clusterSingletonService, flowService);

        dataTreeChangeRegistration = dataBroker.registerDataTreeChangeListener(
                new DataTreeIdentifier<>(
                        LogicalDatastoreType.OPERATIONAL,
                        InstanceIdentifier
                                .create(Nodes.class)
                                .child(Node.class)
                                .augmentation(FlowCapableNode.class)),
                this);

        if (Objects.nonNull(dataTreeChangeRegistration)) {
            LOG.debug("Table miss enforcer data tree change listener successfully registered.");
        }
    }

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<FlowCapableNode>> modifications) {
        for (DataTreeModification modification : modifications) {
            final NodeId nodeId = TableMissUtils.retreiveNodeId(modification.getRootPath().getRootIdentifier());
            LOG.debug("Table miss enforcer data tree change for node: {}", nodeId.getValue());

            if (isAdd(modification)) {
                tableMissEnforcerManager.onDeviceConnected(modification.getRootPath().getRootIdentifier());
            } else if (isDelete(modification)) {
                tableMissEnforcerManager.onDeviceDisconnected(modification.getRootPath().getRootIdentifier());
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (Objects.nonNull(dataTreeChangeRegistration)) {
            dataTreeChangeRegistration.close();
            dataTreeChangeRegistration = null;
        }

        if (Objects.nonNull(tableMissEnforcerManager)) {
            tableMissEnforcerManager.close();
            tableMissEnforcerManager = null;
        }

        LOG.debug("Table miss enforcer successfully closed.");
    }

    @VisibleForTesting
    protected TableMissEnforcerManager getTableMissEnforcerManager() {
        return tableMissEnforcerManager;
    }

}
