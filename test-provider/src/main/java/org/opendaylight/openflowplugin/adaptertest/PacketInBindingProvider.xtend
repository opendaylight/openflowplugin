/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */ 	  				 	 	 
package org.opendaylight.openflowplugin.adaptertest

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext
import org.opendaylight.controller.sal.binding.api.NotificationService
import org.slf4j.LoggerFactory
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider
import org.osgi.framework.BundleContext

/**
 * @author jsebin
 *
 */
class PacketInBindingProvider extends AbstractBindingAwareProvider {
    
    static val LOG = LoggerFactory.getLogger(PacketInBindingProvider);
    
    @Property
    var PacketInTestServiceProvider testProvider
    
    @Property    
    var PacketInCommandProvider cmdProvider
    
    def closeProvider() {
        testProvider.close()
    }
    
    override onSessionInitiated(ProviderContext session) {
        testProvider.setNotificationService(session.getSALService(NotificationService))
        cmdProvider.onSessionInitiated(session)
        LOG.info("onSessionInitiated ProviderContext done")
    }

}