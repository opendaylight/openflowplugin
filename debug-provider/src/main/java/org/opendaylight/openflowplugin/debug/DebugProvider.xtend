/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.debug

import org.opendaylight.controller.md.sal.common.api.data.DataChangeEvent
import org.opendaylight.controller.sal.binding.api.NotificationProviderService
import org.opendaylight.controller.sal.binding.api.data.DataChangeListener
import org.opendaylight.controller.sal.binding.api.data.DataProviderService
import org.opendaylight.yangtools.concepts.Registration
import org.opendaylight.yangtools.yang.binding.DataObject
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier
import org.opendaylight.yangtools.yang.binding.Notification
import org.opendaylight.yangtools.yang.binding.NotificationListener
import org.slf4j.LoggerFactory

class DebugProvider implements AutoCloseable {


    static val LOG = LoggerFactory.getLogger(DebugProvider);

    @Property
    DataProviderService dataService;

    @Property
    NotificationProviderService notificationService;
    val DebugNotificationListener debugNotificationListener = new DebugNotificationListener(this);

    Registration<NotificationListener> listenerRegistration

    def void start() {
        listenerRegistration = notificationService.registerNotificationListener(debugNotificationListener);
        dataService.registerDataChangeListener(InstanceIdentifier.builder().toInstance(),new DebugDataChangeListener());
        LOG.info("Debug Provider Started.");
        
    }

    protected def startChange() {
        return dataService.beginTransaction;
    }

    override close() {
       LOG.info("Flow Capable Inventory Provider stopped.");
        listenerRegistration?.close();
    }
    
}

class DebugNotificationListener implements NotificationListener {
    static val LOG = LoggerFactory.getLogger(DebugNotificationListener);
    @Property
    val DebugProvider manager;

    new(DebugProvider manager) {
        _manager = manager;
    }

    def void onNotification(Notification notification){
        LOG.error("Caught in MD-SAL: ",notification);
    }
}

