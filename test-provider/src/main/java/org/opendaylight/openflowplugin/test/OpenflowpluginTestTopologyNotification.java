/**
 * Copyright (c) 2014, 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.FlowTopologyDiscoveryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkOverutilized;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkUtilizationNormal;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestTopologyNotification {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestTopologyNotification.class);

    private DataBroker dataBroker;
    private ProviderContext pc;
    private final BundleContext ctx;
    private final TopologyEventListener topologyEventListener = new TopologyEventListener();
    private static NotificationService notificationService;
    private Registration listenerReg;

    public OpenflowpluginTestTopologyNotification(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        notificationService = session.getSALService(NotificationService.class);
        // For switch events
        listenerReg = notificationService.registerNotificationListener(topologyEventListener);
        dataBroker = session.getSALService(DataBroker.class);
    }

    final class TopologyEventListener implements FlowTopologyDiscoveryListener {

        List<LinkDiscovered> linkdiscovered = new ArrayList<>();
        List<LinkOverutilized> linkoverutilized = new ArrayList<>();
        List<LinkRemoved> linkremoved = new ArrayList<>();
        List<LinkUtilizationNormal> linkutilizationnormal = new ArrayList<>();

        @Override
        public void onLinkDiscovered(LinkDiscovered notification) {
            LOG.debug("-------------------------------------------");
            LOG.debug("LinkDiscovered notification ........");
            LOG.debug("-------------------------------------------");
        }

        @Override
        public void onLinkOverutilized(LinkOverutilized notification) {
            LOG.debug("-------------------------------------------");
            LOG.debug("LinkOverutilized notification ........");
            LOG.debug("-------------------------------------------");
        }

        @Override
        public void onLinkRemoved(LinkRemoved notification) {
            LOG.debug("-------------------------------------------");
            LOG.debug("LinkRemoved notification   ........");
            LOG.debug("-------------------------------------------");
        }

        @Override
        public void onLinkUtilizationNormal(LinkUtilizationNormal notification) {
            LOG.debug("-------------------------------------------");
            LOG.debug("LinkUtilizationNormal notification ........");
            LOG.debug("-------------------------------------------");
        }

    }
}
