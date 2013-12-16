/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */ 	  				 	 	 
package org.opendaylight.openflowplugin.adaptertest

import org.opendaylight.controller.sal.binding.api.NotificationService
import org.opendaylight.controller.sal.compatibility.DataPacketAdapter
import org.opendaylight.yangtools.concepts.Registration
import org.opendaylight.yangtools.yang.binding.NotificationListener
import org.slf4j.LoggerFactory

/**
 * @author jsebin
 *
 */
class PacketInTestServiceProvider  implements AutoCloseable {
    
    static val LOG = LoggerFactory.getLogger(PacketInTestServiceProvider);

    DataPacketAdapter dataPacket;
    
    @Property
    NotificationService notificationService;

    Registration<NotificationListener> listenerRegistration
    
    new(DataPacketAdapter dpa) {
        dataPacket = dpa
    }
    
    def void start() {
        listenerRegistration = notificationService.registerNotificationListener(dataPacket);        
        LOG.info("PacketInTestServiceProvider Started.");
    }
    
    override close() {
        LOG.info("PacketInTestServiceProvider stopped.");
    } 

}