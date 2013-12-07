/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration
import org.opendaylight.controller.sal.binding.api.NotificationProviderService
import org.opendaylight.controller.sal.binding.api.data.DataProviderService
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier
import org.slf4j.LoggerFactory

class OpenflowpluginTestServiceProvider implements AutoCloseable, SalFlowService {


    static val LOG = LoggerFactory.getLogger(OpenflowpluginTestServiceProvider);

    @Property
    DataProviderService dataService;
    
    @Property
    RoutedRpcRegistration<SalFlowService> flowRegistration;
        

    @Property
    NotificationProviderService notificationService;


    def void start() {
        LOG.info("SalFlowServiceProvider Started.");
        
    }

    override close() {
       LOG.info("SalFlowServiceProvide stopped.");
        flowRegistration.close;
    }
    
    override addFlow(AddFlowInput input) {
        LOG.info("addFlow - " + input);
        return null;
        
    }
    
    override removeFlow(RemoveFlowInput input) {
        LOG.info("removeFlow - " + input);
        return null;
    }
    
    override updateFlow(UpdateFlowInput input) {
        LOG.info("updateFlow - " + input);
        return null;
    }
    
    
    def CompositeObjectRegistration<OpenflowpluginTestServiceProvider> register(ProviderContext ctx) {
        val builder = CompositeObjectRegistration
                .<OpenflowpluginTestServiceProvider> builderFor(this);

        flowRegistration = ctx.addRoutedRpcImplementation(SalFlowService, this);
        val nodeIndentifier = InstanceIdentifier.builder(Nodes).child(Node, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID)));
        flowRegistration.registerPath(NodeContext, nodeIndentifier.toInstance());
        builder.add(flowRegistration);

        return builder.toInstance();
    }
    
}


