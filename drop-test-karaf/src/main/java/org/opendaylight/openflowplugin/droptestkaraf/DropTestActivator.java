/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptestkaraf;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.testcommon.DropTestDsProvider;
import org.opendaylight.openflowplugin.testcommon.DropTestRpcProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class DropTestActivator extends AbstractBindingAwareProvider {
    private static Logger LOG = LoggerFactory.getLogger(DropTestActivator.class);

    private static DropTestDsProvider dropDsProvider = new DropTestDsProvider();

    private static DropTestRpcProvider dropRpcProvider = new DropTestRpcProvider();

    public void onSessionInitiated(final ProviderContext session) {
        LOG.debug("Activator DropAllPack INIT");

        dropDsProvider.setDataService(session.<DataBroker>getSALService(DataBroker.class));
        dropDsProvider.setNotificationService(session.<NotificationProviderService>getSALService(NotificationProviderService.class));

        dropRpcProvider.setNotificationService(session.<NotificationProviderService>getSALService(NotificationProviderService.class));
        dropRpcProvider.setFlowService(session.<SalFlowService>getRpcService(SalFlowService.class));

        LOG.debug("Activator DropAllPack END");
    }

    public void startImpl(final BundleContext ctx) {
        super.startImpl(ctx);
        LOG.debug("-------------------------------------    DROP ALL PACK TEST INITIATED ------------------------ ");
    }

    protected void stopImpl(final BundleContext context) {
      LOG.debug("--------------------------------------    DROP ALL PACK TEST STOPPED --------------------------- ");
      dropDsProvider.close();
      dropRpcProvider.close();
      super.stopImpl(context);
    }
    
    /**
     * @return the dropDsProvider
     */
    public static DropTestDsProvider getDropDsProvider() {
        return dropDsProvider;
    }
    
    /**
     * @return the dropRpcProvider
     */
    public static DropTestRpcProvider getDropRpcProvider() {
        return dropRpcProvider;
    }
}
