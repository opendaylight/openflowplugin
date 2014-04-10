/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.openflowplugin.outputtest.OutputTestCommandProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class DropTestActivator extends AbstractBindingAwareProvider {
    private static Logger LOG = LoggerFactory.getLogger(DropTestActivator.class);

    private static DropTestProvider provider = new DropTestProvider();

    private static DropTestRpcProvider rpcProvider = new DropTestRpcProvider();

    private static DropTestCommandProvider cmdProvider;

    private static OutputTestCommandProvider outCmdProvider;

    public void onSessionInitiated(final ProviderContext session) {
        LOG.debug("Activator DropAllPack INIT");
        provider.setDataService(session.<DataProviderService>getSALService(DataProviderService.class));

        provider.setNotificationService(session.<NotificationProviderService>getSALService(NotificationProviderService.class));

        cmdProvider.onSessionInitiated(session);

        rpcProvider.setNotificationService(session.<NotificationProviderService>getSALService(NotificationProviderService.class));

        rpcProvider.setFlowService(session.<SalFlowService>getRpcService(SalFlowService.class));
        outCmdProvider.onSessionInitiated(session);

        LOG.debug("Activator DropAllPack END");
    }

    public void startImpl(final BundleContext ctx) {
        super.startImpl(ctx);
//      LOG.debug("-------------------------------------    DROP ALL PACK TEST INITIATED ------------------------ ")
        cmdProvider = new DropTestCommandProvider(ctx, provider, rpcProvider);;
        outCmdProvider = new OutputTestCommandProvider(ctx);
    }

    protected void stopImpl(final BundleContext context) {
//      LOG.debug("--------------------------------------    DROP ALL PACK TEST STOPED --------------------------- ")
        provider.close();
    }
}
