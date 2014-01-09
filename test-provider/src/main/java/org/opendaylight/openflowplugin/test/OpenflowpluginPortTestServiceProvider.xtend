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
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier
import org.slf4j.LoggerFactory

class OpenflowpluginPortTestServiceProvider implements AutoCloseable, SalPortService {


    static val LOG = LoggerFactory.getLogger(OpenflowpluginPortTestServiceProvider);
    
    @Property
    RoutedRpcRegistration<SalPortService> portRegistration;
        

    @Property
    NotificationProviderService notificationService;


    def void start() {
        LOG.info("SalPortServiceProvider Started.");
        
    }


    override close() {
       LOG.info("SalPortServiceProvide stopped.");
        portRegistration.close;
    }
    
   
    override updatePort(UpdatePortInput input) {
        LOG.info("updatePort --------------------------------------------------------------------------------- " + input);
        return null;
    }
    
    override getPort() {
    	LOG.info("getPort success");
    	return null;
    }
    
    
    def CompositeObjectRegistration<OpenflowpluginPortTestServiceProvider> register(ProviderContext ctx) {
        val builder = CompositeObjectRegistration
                .<OpenflowpluginPortTestServiceProvider> builderFor(this);

        portRegistration = ctx.addRoutedRpcImplementation(SalPortService, this);
        val nodeIndentifier = InstanceIdentifier.builder(Nodes).child(Node, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID)));
        portRegistration.registerPath(NodeContext, nodeIndentifier.toInstance());
        builder.add(portRegistration);

        return builder.toInstance();
    }
    
}

