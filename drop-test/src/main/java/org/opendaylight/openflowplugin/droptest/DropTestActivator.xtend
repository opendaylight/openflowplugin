/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest

import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext
import org.opendaylight.controller.sal.binding.api.NotificationProviderService
import org.opendaylight.controller.sal.binding.api.data.DataProviderService
import org.opendaylight.openflowplugin.outputtest.OutputTestCommandProvider
import org.osgi.framework.BundleContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DropTestActivator extends AbstractBindingAwareProvider {

    static Logger LOG = LoggerFactory.getLogger(DropTestActivator);

    static var DropTestProvider provider = new DropTestProvider();
    
    static var DropTestCommandProvider cmdProvider
    static var OutputTestCommandProvider outCmdProvider

    override onSessionInitiated(ProviderContext session) {
        LOG.debug("Activator DropAllPack INIT")
        provider.dataService = session.getSALService(DataProviderService)
        provider.notificationService = session.getSALService(NotificationProviderService)
        cmdProvider.onSessionInitiated(session);
        
        outCmdProvider.onSessionInitiated(session);
        LOG.debug("Activator DropAllPack END")
    }
    
    override startImpl(BundleContext ctx) {
        super.startImpl(ctx);
//        LOG.info("-------------------------------------    DROP ALL PACK TEST INITIATED ------------------------ ")
        cmdProvider = new DropTestCommandProvider(ctx,provider);
        outCmdProvider = new OutputTestCommandProvider(ctx);
    }

    override protected stopImpl(BundleContext context) {
//        LOG.info("--------------------------------------    DROP ALL PACK TEST STOPED --------------------------- ")
        provider.close();
    }

}
