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

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LLDPPacketPuntEnforcer implements AutoCloseable, DataTreeChangeListener<FlowCapableNode> {
    private TableMissEnforcerManager tableMissEnforcerManager;

    private ListenerRegistration<DataTreeChangeListener> listenerRegistration;

    public LLDPPacketPuntEnforcer(final SalFlowService flowService,
                                  final DataBroker dataBroker,
                                  final ClusterSingletonServiceProvider clusterSingletonService) {
        tableMissEnforcerManager = new TableMissEnforcerManager(clusterSingletonService, flowService);

        dataBroker.registerDataTreeChangeListener(
                new DataTreeIdentifier<>(
                        LogicalDatastoreType.OPERATIONAL,
                        InstanceIdentifier
                                .create(Nodes.class)
                                .child(Node.class)
                                .augmentation(FlowCapableNode.class)),
                this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<FlowCapableNode>> modifications) {
        for (DataTreeModification modification : modifications) {
            final DataObjectModification<Node> nodeModification = modification.getRootNode();

            if (isAdd(nodeModification)) {
                tableMissEnforcerManager.onDeviceConnected(modification);
            } else if (isDelete(nodeModification)) {
                tableMissEnforcerManager.onDeviceDisconnected(modification);
            }
        }
    }

    @Override
    public void close() throws Exception {
        tableMissEnforcerManager.close();
    }

}
