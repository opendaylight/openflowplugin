/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundle activator for LLDP speaker
 */
public class Activator extends AbstractBindingAwareConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private NodeConnectorInventoryEventTranslator eventTranslator;
    private LLDPSpeaker lldpSpeaker;

    @Override
    public void onSessionInitialized(BindingAwareBroker.ConsumerContext consumerContext) {
        LOG.trace("LLDP discovery session activated.");

        DataBroker dataBroker = consumerContext.getSALService(DataBroker.class);
        PacketProcessingService packetProcessingService =
                consumerContext.getRpcService(PacketProcessingService.class);

        lldpSpeaker = new LLDPSpeaker(packetProcessingService);
        eventTranslator = new NodeConnectorInventoryEventTranslator(dataBroker, lldpSpeaker);
    }

    @Override
    protected void stopImpl(BundleContext bundleContext) {
        LOG.trace("LLDP discovery session deactivated.");

        if (eventTranslator != null) {
            eventTranslator.close();
            eventTranslator = null;
        }
        if (lldpSpeaker != null) {
            lldpSpeaker.close();
            lldpSpeaker = null;
        }
        super.stopImpl(bundleContext);
    }
}
