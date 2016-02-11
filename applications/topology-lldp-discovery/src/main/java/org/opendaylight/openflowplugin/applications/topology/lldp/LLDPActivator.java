/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLDPActivator implements BindingAwareProvider, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPActivator.class);
    private static LLDPDiscoveryProvider provider = new LLDPDiscoveryProvider();
    private static String lldpSecureKey;

    public LLDPActivator(String secureKey) {
        lldpSecureKey = secureKey;
    }

    public void onSessionInitiated(final ProviderContext session) {
        DataProviderService dataService = session.<DataProviderService>getSALService(DataProviderService.class);
        provider.setDataService(dataService);
        NotificationProviderService notificationService = session.<NotificationProviderService>getSALService(NotificationProviderService.class);
        provider.setNotificationService(notificationService);
        provider.start();
    }

    @Override
    public void close() throws Exception {
        if(provider != null) {
            try {
                provider.close();
            } catch (Exception e) {
                LOG.warn("Exception when closing down topology-lldp-discovery",e);
            }
        }
    }

    public static String getLldpSecureKey() {
        return lldpSecureKey;
    }
}
