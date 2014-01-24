/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext
import org.opendaylight.controller.sal.binding.api.NotificationProviderService
import org.opendaylight.controller.sal.binding.api.data.DataProviderService
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration
import org.opendaylight.yangtools.concepts.Registration
import org.slf4j.LoggerFactory
import org.opendaylight.yangtools.yang.binding.NotificationListener

class DropTestProvider implements AutoCloseable {


    static val LOG = LoggerFactory.getLogger(DropTestProvider);

    @Property
    DataProviderService dataService;        

    @Property
    NotificationProviderService notificationService;

    val DropTestCommiter commiter = new DropTestCommiter(this);

    Registration<NotificationListener> listenerRegistration

    def void start() {
        listenerRegistration = notificationService.registerNotificationListener(commiter);
        LOG.debug("DropTestProvider Started.");
        
    }   
    
    override close() {
       LOG.debug("DropTestProvider stopped.");
        listenerRegistration?.close();
    }
    
}


