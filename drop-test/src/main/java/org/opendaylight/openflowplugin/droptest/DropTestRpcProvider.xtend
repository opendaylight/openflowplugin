/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest

import org.opendaylight.controller.sal.binding.api.NotificationProviderService
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService
import org.opendaylight.yangtools.concepts.Registration
import org.opendaylight.yangtools.yang.binding.NotificationListener
import org.slf4j.LoggerFactory

class DropTestRpcProvider implements AutoCloseable {


    static val LOG = LoggerFactory.getLogger(DropTestProvider);

    @Property
    SalFlowService flowService;
            

    

    @Property
    NotificationProviderService notificationService;

    var DropTestRpcSender commiter

    Registration<NotificationListener> listenerRegistration

    def void start() {
        commiter = new DropTestRpcSender(this,flowService)
        listenerRegistration = notificationService.registerNotificationListener(commiter);
        LOG.debug("DropTestProvider Started.");
        
    }   
    
    override close() {
       LOG.debug("DropTestProvider stopped.");
        listenerRegistration?.close();
    }
    
}


