/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */ 	  				 	 	 
package org.opendaylight.openflowplugin.adaptertest

import java.util.Arrays
import org.apache.felix.dm.Component
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker
import org.opendaylight.controller.sal.compatibility.DataPacketAdapter
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase
import org.opendaylight.controller.sal.packet.IPluginOutDataPacketService
import org.slf4j.LoggerFactory

/**
 * @author jsebin
 *
 */
class PacketInTestActivator extends ComponentActivatorAbstractBase {
    
    static val LOG = LoggerFactory.getLogger(PacketInTestActivator);
    
    @Property
    DataPacketAdapter adapter = new DataPacketAdapter
    
    override protected getGlobalImplementations() {
        LOG.info("getting implementations")
        return Arrays.asList(this, adapter)
    }

    override protected configureGlobalInstance(Component c, Object imp) {
        LOG.info("wiring {}", imp.class)
        configure(imp, c);
    }
    
    private def dispatch configure(PacketInTestActivator imp, Component it) {
        add(
            createServiceDependency().setService(BindingAwareBroker) //
            .setCallbacks("setBroker", "setBroker") //
            .setRequired(true))
    }
    
    private def dispatch configure(DataPacketAdapter imp, Component it) {
        add(
            createServiceDependency() //
            .setService(IPluginOutDataPacketService) //
            .setCallbacks("setDataPacketPublisher", "setDataPacketPublisher") //
            .setRequired(false))
    }
}