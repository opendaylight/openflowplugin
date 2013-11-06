/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Collection;
import java.util.Map;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 * 
 */
public abstract class TicketProcessorFactory {

    protected static final Logger LOG = LoggerFactory
            .getLogger(TicketProcessorFactory.class);

    /**
     * @param ticket
     * @param listenerMapping
     * @return runnable ticket processor
     */
    public static <T> Runnable createProcessor(
            final Ticket<T> ticket,
            final Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping) {
        return new Runnable() {
            @Override
            public void run() {
                // TODO: delegate processing of message - notify listeners
                LOG.debug("experimenter received, type: " + ticket.getRegisteredMessageType());

                notifyListener();

                ticket.getResult().set(null);
            }

            /**
             * @param listenerMapping
             */
            private void notifyListener() {
                DataObject message = ticket.getMessage();
                Class<? extends DataObject> messageType = ticket.getRegisteredMessageType();
                Collection<IMDMessageListener> listeners = listenerMapping.get(messageType);
                ConnectionConductor conductor = ticket.getConductor();

                if (listeners != null) {
                    for (IMDMessageListener listener : listeners) {
                        // Pass cookie only for PACKT_IN
                        if (messageType.equals("PacketInMessage.class")) {
                            listener.receive(conductor.getAuxiliaryKey(),
                                    conductor.getSessionContext(), message);
                        } else {
                            listener.receive(null, conductor.getSessionContext(), message);
                        }
                    }
                } else {
                    LOG.warn("No listeners for this message Type {}", messageType);
                }
            }
        };
    }
}
