/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OfHeader to DataObject implementation
 */
public class TicketProcessorFactoryImpl implements TicketProcessorFactory<OfHeader, DataObject> {

    private static final Logger LOG = LoggerFactory
            .getLogger(TicketProcessorFactoryImpl.class);

    protected Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping;
    protected MessageSpy<DataContainer> spy;
    protected TicketFinisher<DataObject> ticketFinisher;

    /**
     * @param translatorMapping the translatorMapping to set
     */
    @Override
    public void setTranslatorMapping(
            Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping) {
        this.translatorMapping = ImmutableMap.copyOf(translatorMapping);
    }

    /**
     * @param spy the spy to set
     */
    @Override
    public void setSpy(MessageSpy<DataContainer> spy) {
        this.spy = spy;
    }

    /**
     * @param ticketFinisher the finisher to set
     */
    @Override
    public void setTicketFinisher(TicketFinisher<DataObject> ticketFinisher) {
        this.ticketFinisher = ticketFinisher;
    }

    /**
     * @param ticket ticket
     * @return runnable ticket processor
     */
    @Override
    public Runnable createProcessor(final Ticket<OfHeader, DataObject> ticket) {

        Runnable ticketProcessor = new Runnable() {
            @Override
            public void run() {
                LOG.debug("message received, type: {}", ticket.getMessage().getImplementedInterface().getSimpleName());
                List<DataObject> translate;
                try {
                    translate = translate(ticket);
                    ticket.getResult().set(translate);
                    ticket.setDirectResult(translate);
                    // spying on result
                    if (spy != null) {
                        spy.spyIn(ticket.getMessage());
                        for (DataObject outMessage : translate) {
                            spy.spyOut(outMessage);
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("translation problem: {}", e.getMessage());
                    ticket.getResult().setException(e);
                }
                LOG.debug("message processing done (type: {}, ticket: {})",
                        ticket.getMessage().getImplementedInterface().getSimpleName(),
                        System.identityHashCode(ticket));
            }
        };


        return ticketProcessor;
    }

    /**
     * @param ticket ticket
     * @return runnable ticket processor
     */
    @Override
    public Runnable createSyncProcessor(final Ticket<OfHeader, DataObject> ticket) {

        Runnable ticketProcessor = new Runnable() {
            @Override
            public void run() {
                List<DataObject> translate;
                try {
                    translate = translate(ticket);
                    // spying on result
                    if (spy != null) {
                        spy.spyIn(ticket.getMessage());
                        for (DataObject outMessage : translate) {
                            spy.spyOut(outMessage);
                        }
                    }
                    ticketFinisher.firePopNotification(translate);
                } catch (Exception e) {
                    LOG.error("translation problem: {}", e.getMessage());
                    ticket.getResult().setException(e);
                }
            }
        };


        return ticketProcessor;
    }


    /**
     * @param ticket ticket
     *
     */
    @Override
    public List<DataObject> translate(Ticket<OfHeader, DataObject> ticket) {
        List<DataObject> result = new ArrayList<>();

        OfHeader message = ticket.getMessage();
        Class<? extends DataContainer> messageType = ticket.getMessage().getImplementedInterface();
        ConnectionConductor conductor = ticket.getConductor();
        Collection<IMDMessageTranslator<OfHeader, List<DataObject>>> translators = null;
        LOG.trace("translating ticket: {}, ticket: {}", messageType.getSimpleName(), System.identityHashCode(ticket));

        Short version = message.getVersion();
        if (version == null) {
            throw new IllegalArgumentException("version is NULL");
        }
        TranslatorKey tKey = new TranslatorKey(version, messageType.getName());
        translators = translatorMapping.get(tKey);

        LOG.debug("translatorKey: {} + {}", version, messageType.getName());

        if (translators != null) {
            for (IMDMessageTranslator<OfHeader, List<DataObject>> translator : translators) {
                SwitchConnectionDistinguisher cookie = null;
                // Pass cookie only for PACKT_OfHeader
                if (messageType.equals("PacketInMessage.class")) {
                    cookie = conductor.getAuxiliaryKey();
                }
                long start = System.nanoTime();
                List<DataObject> translatorOutput = translator.translate(cookie, conductor.getSessionContext(), message);
                long end = System.nanoTime();
                LOG.trace("translator: {} elapsed time {} ns",translator,end-start);
                if(translatorOutput != null && !translatorOutput.isEmpty()) {
                    result.addAll(translatorOutput);
                }
            }
        } else {
            LOG.warn("No translators for this message Type: {}", messageType);
        }

        return result;
    }
}
