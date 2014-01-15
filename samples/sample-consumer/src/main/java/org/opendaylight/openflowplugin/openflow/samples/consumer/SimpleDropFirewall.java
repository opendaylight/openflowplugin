/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.samples.consumer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opendaylight.controller.md.sal.common.api.data.DataChangeEvent;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.controller.sal.binding.api.data.DataChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SimpleDropFirewall {

    private ConsumerContext context;
    private SalFlowService flowService;
    private DataChangeListener listener = new NodeListener();
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
    
    private class NodeListener implements DataChangeListener {
        
        
        @Override
        public void onDataChanged(
                DataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
            
            Map<InstanceIdentifier<?>, DataObject> updated = change.getUpdatedConfigurationData();
            Set<Entry<InstanceIdentifier<?>, DataObject>> set = updated.entrySet();
            for (Entry<InstanceIdentifier<?>, DataObject> entry : set) {
                Class<?> changedObjectType = entry.getKey().getTargetType();
                if(Node.class.equals(changedObjectType)) {
                    // We now that this is node updated
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
