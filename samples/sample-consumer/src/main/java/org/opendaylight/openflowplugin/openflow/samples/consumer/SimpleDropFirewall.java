/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.samples.consumer;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SimpleDropFirewall {

    private ConsumerContext context;
    private SalFlowService flowService;
    private DataTreeChangeListener listener = new NodeListener();
    private DataBrokerService data;

    public void setContext(ConsumerContext session) {
        this.context = session;
    }

    public void setFlowService(SalFlowService flowService) {
        this.flowService = flowService;
    }

    public void start() {
        NotificationService notificationService = context.getSALService(NotificationService.class);
    }

    public boolean addFlow(AddFlowInput flow) throws InterruptedException,
            ExecutionException, TimeoutException {
        Future<RpcResult<AddFlowOutput>> result = flowService.addFlow(flow);

        return result.get(5, TimeUnit.SECONDS).isSuccessful();
    }

    private class NodeListener implements DataTreeChangeListener<Node> {
        @Override
        public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Node>> modifications) {
            for (DataTreeModification modification : modifications) {
                if (modification.getRootNode().getModificationType() == ModificationType.SUBTREE_MODIFIED) {
                    // node updated
                }
            }
        }
    }


    private class InventoryListener implements OpendaylightInventoryListener {

        @Override
        public void onNodeConnectorRemoved(NodeConnectorRemoved notification) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onNodeConnectorUpdated(NodeConnectorUpdated notification) {
            
        }

        @Override
        public void onNodeRemoved(NodeRemoved notification) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onNodeUpdated(NodeUpdated notification) {
            // TODO Auto-generated method stub
            
        }
        
    }
}
