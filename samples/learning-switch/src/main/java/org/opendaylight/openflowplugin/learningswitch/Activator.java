/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch;

import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * learning switch activator
 */
public class Activator extends AbstractBindingAwareConsumer implements AutoCloseable {
    
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private SimpleLearningSwitchManager learningSwitch;

    
    @Override
    protected void startImpl(BundleContext context) {
        LOG.info("startImpl() passing");
        learningSwitch = new SimpleLearningSwitchManager();
    }
    
    @Override
    public void onSessionInitialized(ConsumerContext session) {
        LOG.info("inSessionInitialized() passing");
        learningSwitch.setData(session.getSALService(DataBrokerService.class));
        learningSwitch.setPacketProcessingService(session.getRpcService(PacketProcessingService.class));
        learningSwitch.setNotificationService(session.getSALService(NotificationService.class));
        learningSwitch.start();
    }

    @Override
    public void close() throws Exception {
        LOG.info("close() passing");
        if (learningSwitch != null) {
            learningSwitch.stop();
        }
    }

}
