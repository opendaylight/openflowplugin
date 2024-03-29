/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import java.util.Set;
import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener;
import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener.Component;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemoved;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy implementation flushing events into log.
 */
@Deprecated
public final class FlowEventListenerLoggingImpl {
    private static final Logger LOG = LoggerFactory.getLogger(FlowEventListenerLoggingImpl.class);

    private FlowEventListenerLoggingImpl() {
        // Hidden on purpose
    }

    static CompositeListener newListener() {
        return new CompositeListener(Set.of(
            new Component<>(FlowAdded.class, notification -> {
                LOG.info("flow to be added {}", notification);
                LOG.info("added flow Xid {}", notification.getTransactionId().getValue());
            }),
            new Component<>(FlowRemoved.class, notification -> {
                LOG.debug("removed flow {}", notification);
                LOG.debug("remove flow Xid {}", notification.getTransactionId().getValue());
            }),
            new Component<>(FlowUpdated.class, notification -> {
                LOG.debug("updated flow {}", notification);
                LOG.debug("updated flow Xid {}", notification.getTransactionId().getValue());
            }),
            new Component<>(NodeErrorNotification.class, notification -> {
                //commenting as we have a NodeErrorListener
                /*    LOG.error("Error notification  flow Xid........................."
                            + notification.getTransactionId().getValue());
                    LOG.debug("notification Begin-Transaction:"
                            + notification.getTransactionUri()
                            + "-----------------------------------------------------------------------------------");
                */
            }),
            new Component<>(SwitchFlowRemoved.class, notification -> {
                LOG.debug("Switch flow removed : Cookies {}", notification.getCookie().toString());
            })));
    }
}
