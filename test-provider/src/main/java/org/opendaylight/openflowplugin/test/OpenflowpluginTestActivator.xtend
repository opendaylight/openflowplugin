/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test

import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext
import org.opendaylight.controller.sal.binding.api.NotificationProviderService
import org.opendaylight.controller.sal.binding.api.data.DataProviderService
import org.osgi.framework.BundleContext

class OpenflowpluginTestActivator extends AbstractBindingAwareProvider {

    static var OpenflowpluginTestServiceProvider provider = new OpenflowpluginTestServiceProvider();
    var OpenflowpluginTestCommandProvider cmdProvider;      

    override onSessionInitiated(ProviderContext session) {
        provider.dataService = session.getSALService(DataProviderService)
        provider.notificationService = session.getSALService(NotificationProviderService)
        provider.start();
        provider.register(session);
        cmdProvider.onSessionInitiated(session);        
    }
    
    override startImpl(BundleContext ctx) {
        super.startImpl(ctx);
        cmdProvider = new OpenflowpluginTestCommandProvider(ctx);
        
    }

    override protected stopImpl(BundleContext context) {
        provider.close();
    }

}
