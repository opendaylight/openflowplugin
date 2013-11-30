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
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration
import org.opendaylight.yangtools.concepts.Registration
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier
import org.opendaylight.yangtools.yang.binding.NotificationListener
import org.slf4j.LoggerFactory
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput

class OpenflowpluginTestServiceProvider implements AutoCloseable, SalGroupService {


    static val LOG = LoggerFactory.getLogger(OpenflowpluginTestServiceProvider);

    @Property
    DataProviderService dataService;
    
    @Property
    RoutedRpcRegistration<SalGroupService> groupRegistration;
        

    @Property
    NotificationProviderService notificationService;


    def void start() {
        LOG.info("SalGroupServiceProvider Started.");
        
    }


    override close() {
       LOG.info("SalGroupServiceProvide stopped.");
        groupRegistration.close;
    }
    
    override addGroup(AddGroupInput input) {
        LOG.info("addGroup - " + input);
        return null;
        
    }
    
    override removeGroup(RemoveGroupInput input) {
        LOG.info("removeGroup - " + input);
        return null;
    }
    
    override updateGroup(UpdateGroupInput input) {
        LOG.info("updateGroup - " + input);
        return null;
    }
    
    
    def CompositeObjectRegistration<OpenflowpluginTestServiceProvider> register(ProviderContext ctx) {
        val builder = CompositeObjectRegistration
                .<OpenflowpluginTestServiceProvider> builderFor(this);

        groupRegistration = ctx.addRoutedRpcImplementation(SalGroupService, this);
        val nodeIndentifier = InstanceIdentifier.builder(Nodes).child(Node, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID)));
        groupRegistration.registerPath(NodeContext, nodeIndentifier.toInstance());
        builder.add(groupRegistration);

        return builder.toInstance();
    }
    
}


