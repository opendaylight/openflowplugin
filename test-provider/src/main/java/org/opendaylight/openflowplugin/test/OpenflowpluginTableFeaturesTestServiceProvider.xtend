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

import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService
//import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalTableService
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier
import org.slf4j.LoggerFactory


class OpenflowpluginTableFeaturesTestServiceProvider implements AutoCloseable, SalTableService {


    static val LOG = LoggerFactory.getLogger(OpenflowpluginTableFeaturesTestServiceProvider);
    
    @Property
    RoutedRpcRegistration<SalTableService> tableRegistration;
        

    @Property
    NotificationProviderService notificationService;


    def void start() {
        LOG.info("SalTableServiceProvider Started.");
        
    }


    override close() {
       LOG.info("SalTableServiceProvider stopped.");
        tableRegistration.close;
    }
    
      
    override updateTable(UpdateTableInput input) {
        LOG.info("updateTable - " + input);
        return null;
    }
    
    
    def CompositeObjectRegistration<OpenflowpluginTableFeaturesTestServiceProvider> register(ProviderContext ctx) {
        val builder = CompositeObjectRegistration
                .<OpenflowpluginTableFeaturesTestServiceProvider> builderFor(this);

        tableRegistration = ctx.addRoutedRpcImplementation(SalTableService, this);
        val nodeIndentifier = InstanceIdentifier.builder(Nodes).child(Node, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID)));
        tableRegistration.registerPath(NodeContext, nodeIndentifier.toInstance());
        builder.add(tableRegistration);

        return builder.toInstance();
    }
    
}