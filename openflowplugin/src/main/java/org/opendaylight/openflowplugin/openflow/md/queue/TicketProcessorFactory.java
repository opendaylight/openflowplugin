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
 * @param <IN>
 * @param <OUT>
 */
public class TicketProcessorFactory<IN, OUT> {

    protected static final Logger LOG = LoggerFactory
            .getLogger(TicketProcessorFactory.class);

    protected VersionExtractor<IN> versionExtractor;
    protected RegisteredTypeExtractor<IN> registeredTypeExtractor;
    protected Map<TranslatorKey, Collection<IMDMessageTranslator<IN, List<OUT>>>> translatorMapping;
    protected MessageSpy<IN, OUT> spy;

    /**
     * @param versionExtractor the versionExtractor to set
     */
    public void setVersionExtractor(VersionExtractor<IN> versionExtractor) {
        this.versionExtractor = versionExtractor;
    }

    /**
     * @param registeredTypeExtractor the registeredTypeExtractor to set
     */
    public void setRegisteredTypeExtractor(
            RegisteredTypeExtractor<IN> registeredTypeExtractor) {
        this.registeredTypeExtractor = registeredTypeExtractor;
    }

    /**
     * @param translatorMapping the translatorMapping to set
     */
    public void setTranslatorMapping(
            Map<TranslatorKey, Collection<IMDMessageTranslator<IN, List<OUT>>>> translatorMapping) {
        this.translatorMapping = translatorMapping;
    }

    /**
     * @param spy the spy to set
     */
    public void setSpy(MessageSpy<IN, OUT> spy) {
        this.spy = spy;
    }


    /**
     * @param ticket
     * @return runnable ticket processor
     */
    public Runnable createProcessor(final Ticket<IN, OUT> ticket) {

        Runnable ticketProcessor = new Runnable() {
            @Override
            public void run() {
                LOG.debug("message received, type: {}", registeredTypeExtractor.extractRegisteredType(
                        ticket.getMessage()).getSimpleName());
                List<OUT> translate;
                try {
                    translate = translate();
                    ticket.getResult().set(translate);
                    // spying on result
                    if (spy != null) {
                        spy.spyIn(ticket.getMessage());
                        spy.spyOut(ticket.getResult().get());
                    }
                } catch (Exception e) {
                    LOG.error("translation problem: {}", e.getMessage());
                    ticket.getResult().setException(e);
                }
                LOG.debug("message processing done (type: {}, ticket: {})",
                        registeredTypeExtractor.extractRegisteredType(ticket.getMessage()).getSimpleName(),
                        System.identityHashCode(ticket));
            }

            /**
             *
             */
            private List<OUT> translate() {
                List<OUT> result = new ArrayList<>();

                IN message = ticket.getMessage();
                Class<? extends IN> messageType = registeredTypeExtractor.extractRegisteredType(ticket.getMessage());
                ConnectionConductor conductor = ticket.getConductor();
                Collection<IMDMessageTranslator<IN, List<OUT>>> translators = null;
                LOG.debug("translating ticket: {}, ticket: {}", messageType.getSimpleName(), System.identityHashCode(ticket));

                Short version = versionExtractor.extractVersion(message);
                if (version == null) {
                    throw new IllegalArgumentException("version is NULL");
                }
                TranslatorKey tKey = new TranslatorKey(version, messageType.getName());
                translators = translatorMapping.get(tKey);

                LOG.debug("translatorKey: {} + {}", version, messageType.getName());

                if (translators != null) {
                    for (IMDMessageTranslator<IN, List<OUT>> translator : translators) {
                        SwitchConnectionDistinguisher cookie = null;
                        // Pass cookie only for PACKT_IN
                        if (messageType.equals("PacketInMessage.class")) {
                            cookie = conductor.getAuxiliaryKey();
                        }
                        long start = System.nanoTime();
                        List<OUT> translatorOutput = translator.translate(cookie, conductor.getSessionContext(), message);
                        long end = System.nanoTime();
                        LOG.debug("translator: {} elapsed time {} ns",translator,end-start);
                        if(translatorOutput != null && !translatorOutput.isEmpty()) {
                            result.addAll(translatorOutput);
                        }
                    }
                } else {
                    LOG.warn("No translators for this message Type: {}", messageType);
                }
                return result;
            }
        };

        return ticketProcessor;
    }
}
