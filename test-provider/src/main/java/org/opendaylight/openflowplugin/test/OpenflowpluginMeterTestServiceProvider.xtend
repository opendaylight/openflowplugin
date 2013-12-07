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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier
import org.slf4j.LoggerFactory

class OpenflowpluginMeterTestServiceProvider implements AutoCloseable, SalMeterService {


    static val LOG = LoggerFactory.getLogger(OpenflowpluginMeterTestServiceProvider);

    @Property
    DataProviderService dataService;
    
    @Property
    RoutedRpcRegistration<SalMeterService> meterRegistration;
        

    @Property
    NotificationProviderService notificationService;


    def void start() {
        LOG.info("SalMeterServiceProvider Started.");
        
    }


    override close() {
       LOG.info("SalMeterServiceProvide stopped.");
        meterRegistration.close;
    }
    
    override addMeter(AddMeterInput input) {
        LOG.info("addMeter - " + input);
        return null;
        
    }
    
    override removeMeter(RemoveMeterInput input) {
        LOG.info("removeMeter - " + input);
        return null;
    }
    
    override updateMeter(UpdateMeterInput input) {
        LOG.info("updateMeter - " + input);
        return null;
    }
    
    
    def CompositeObjectRegistration<OpenflowpluginMeterTestServiceProvider> register(ProviderContext ctx) {
        val builder = CompositeObjectRegistration
                .<OpenflowpluginMeterTestServiceProvider> builderFor(this);

        meterRegistration = ctx.addRoutedRpcImplementation(SalMeterService, this);
        val nodeIndentifier = InstanceIdentifier.builder(Nodes).child(Node, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID)));
        meterRegistration.registerPath(NodeContext, nodeIndentifier.toInstance());
        builder.add(meterRegistration);
        return builder.toInstance();
    }
    
}


