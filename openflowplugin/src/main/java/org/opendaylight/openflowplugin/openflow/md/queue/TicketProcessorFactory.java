/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.TranslatorKey;
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
     * @param versionExtractor 
     * @param translatorMapping
     * @return runnable ticket processor
     */
    public static <IN, OUT> Runnable createProcessor(
            final Ticket<IN, OUT> ticket,
            final VersionExtractor<IN> versionExtractor,
            final Map<TranslatorKey, Collection<IMDMessageTranslator<IN, OUT>>> translatorMapping) {
        return new Runnable() {
            @Override
            public void run() {
                LOG.debug("message received, type: {}", ticket.getRegisteredMessageType().getSimpleName());
                List<OUT> translate;
                try {
                    translate = translate();
                    ticket.getResult().set(translate);
                } catch (Exception e) {
                    LOG.error("translation problem: {}", e.getMessage());
                    ticket.getResult().setException(e);
                }
                LOG.debug("message processing done (type: {}, ticket: {})", 
                        ticket.getRegisteredMessageType().getSimpleName(), 
                        System.identityHashCode(ticket));
            }

            /**
             * @param listenerMapping
             */
            private List<OUT> translate() {
                List<OUT> result = new ArrayList<>();
                
                IN message = ticket.getMessage();
                Class<? extends IN> messageType = ticket.getRegisteredMessageType();
                ConnectionConductor conductor = ticket.getConductor();
                Collection<IMDMessageTranslator<IN, OUT>> translators = null;
                LOG.debug("translating ticket: {}, ticket: {}", messageType.getSimpleName(), System.identityHashCode(ticket));
                
                Short version = versionExtractor.extractVersion(message);
                if (version == null) {
                   throw new IllegalArgumentException("version is NULL"); 
                }
                TranslatorKey tKey = new TranslatorKey(version, messageType.getName());
                translators = translatorMapping.get(tKey);
                
                LOG.debug("translatorKey: {} + {}", version, messageType.getName());

                if (translators != null) {
                    for (IMDMessageTranslator<IN, OUT> translator : translators) {
                        SwitchConnectionDistinguisher cookie = null;
                        // Pass cookie only for PACKT_IN
                        if (messageType.equals("PacketInMessage.class")) {
                            cookie = conductor.getAuxiliaryKey();
                        }
                        result.add(translator.translate(cookie, conductor.getSessionContext(), message));
                    }
                } else {
                    LOG.warn("No translators for this message Type: {}", messageType);
                }
                return result;
            }
        };
    }
}
