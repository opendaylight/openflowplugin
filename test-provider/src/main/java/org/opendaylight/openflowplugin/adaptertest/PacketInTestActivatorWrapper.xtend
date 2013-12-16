/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */ 	  				 	 	 
package org.opendaylight.openflowplugin.adaptertest

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory

/**
 * @author jsebin
 *
 */
class PacketInTestActivatorWrapper implements BundleActivator {
    
    static val LOG = LoggerFactory.getLogger(PacketInTestActivatorWrapper);
    
    var PacketInBindingProvider bindingProvider;
    var PacketInTestActivator testActivator;
    
    new () {
        testActivator = new PacketInTestActivator
        bindingProvider = new PacketInBindingProvider
    }
    
    override start(BundleContext context) throws Exception {
        bindingProvider.setCmdProvider(new PacketInCommandProvider(context,testActivator.adapter))
        bindingProvider.setTestProvider(new PacketInTestServiceProvider(testActivator.adapter))
        
        testActivator.start(context)
        bindingProvider.start(context)
        LOG.info("wrapper started")
    }
    
    override stop(BundleContext context) throws Exception {
        testActivator.stop(context)
        bindingProvider.stop(context)
        LOG.info("wrapper stopped")
    }
    
}