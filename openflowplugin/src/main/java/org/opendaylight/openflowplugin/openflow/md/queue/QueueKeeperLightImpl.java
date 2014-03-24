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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.TranslatorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public class QueueKeeperLightImpl implements QueueKeeper<OfHeader, DataObject> {

    private static final Logger LOG = LoggerFactory
            .getLogger(QueueKeeperLightImpl.class);

    private Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenersMapping;
    private BlockingQueue<TicketResult<DataObject>> processQueue;
    private ScheduledThreadPoolExecutor pool;
    private int poolSize = 10;
    private Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping;
    private TicketProcessorFactory<OfHeader, DataObject> ticketProcessorFactory;
    private MessageSpy<OfHeader, DataObject> messageSpy;

    private VersionExtractor<OfHeader> versionExtractor = new VersionExtractor<OfHeader>() {
        @Override
        public Short extractVersion(OfHeader message) {
            return message.getVersion();
        }
    };

    private RegisteredTypeExtractor<OfHeader> registeredSrcTypeExtractor =
            new RegisteredTypeExtractor<OfHeader>() {
        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends OfHeader> extractRegisteredType(
                OfHeader message) {
            return (Class<? extends OfHeader>) message.getImplementedInterface();
        }
    };

    private RegisteredTypeExtractor<DataObject> registeredOutTypeExtractor =
            new RegisteredTypeExtractor<DataObject>() {
        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends DataObject> extractRegisteredType(
                DataObject message) {
            return (Class<? extends DataObject>) message.getImplementedInterface();
        }
    };

    /**
     * prepare queue
     */
    public void init() {
        processQueue = new LinkedBlockingQueue<>();
        pool = new ScheduledThreadPoolExecutor(poolSize);

        ticketProcessorFactory = new TicketProcessorFactory<>();
        ticketProcessorFactory.setRegisteredTypeExtractor(registeredSrcTypeExtractor);
        ticketProcessorFactory.setTranslatorMapping(translatorMapping);
        ticketProcessorFactory.setVersionExtractor(versionExtractor);
        ticketProcessorFactory.setSpy(messageSpy);

        TicketFinisher<DataObject> finisher = new TicketFinisher<>(
                processQueue, popListenersMapping, registeredOutTypeExtractor);
        new Thread(finisher).start();
    }

    /**
     * stop processing queue
     */
    public void shutdown() {
        pool.shutdown();
    }

    @Override
    public void push(OfHeader message, ConnectionConductor conductor) {
        push(message,conductor,QueueKeeper.QueueType.DEFAULT);
    }

    @Override
    public void push(OfHeader message, ConnectionConductor conductor, QueueType queueType) {
        if(queueType == QueueKeeper.QueueType.DEFAULT) {
            TicketImpl<OfHeader, DataObject> ticket = new TicketImpl<>();
            ticket.setConductor(conductor);
            ticket.setMessage(message);
            LOG.debug("ticket scheduling: {}, ticket: {}",
                    message.getImplementedInterface().getSimpleName(), System.identityHashCode(ticket));
            //TODO: block if queue limit reached
            processQueue.add(ticket);
            scheduleTicket(ticket);
        } else if (queueType == QueueKeeper.QueueType.UNORDERED){
            List<DataObject> processedMessages = translate(message,conductor);
            pop(processedMessages,conductor);
        }
    }

    /**
     * @param ticket
     */
    private void scheduleTicket(Ticket<OfHeader, DataObject> ticket) {
        Runnable ticketProcessor = ticketProcessorFactory.createProcessor(ticket);
        pool.execute(ticketProcessor);
    }

    /**
     * @param poolSize the poolSize to set
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @Override
    public void setTranslatorMapping(
            Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping) {
        this.translatorMapping = translatorMapping;
    }

    @Override
    public void setPopListenersMapping(
            Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenersMapping) {
        this.popListenersMapping = popListenersMapping;
    }

    /**
     * @param messageSpy the messageSpy to set
     */
    public void setMessageSpy(MessageSpy<OfHeader, DataObject> messageSpy) {
        this.messageSpy = messageSpy;
    }

    private List<DataObject> translate(OfHeader message, ConnectionConductor conductor) {
        List<DataObject> result = new ArrayList<>();
        Class<? extends OfHeader> messageType = registeredSrcTypeExtractor.extractRegisteredType(message);
        Collection<IMDMessageTranslator<OfHeader, List<DataObject>>> translators = null;
        LOG.debug("translating message: {}", messageType.getSimpleName());

        Short version = versionExtractor.extractVersion(message);
        if (version == null) {
           throw new IllegalArgumentException("version is NULL");
        }
        TranslatorKey tKey = new TranslatorKey(version, messageType.getName());
        translators = translatorMapping.get(tKey);

        LOG.debug("translatorKey: {} + {}", version, messageType.getName());

        if (translators != null) {
            for (IMDMessageTranslator<OfHeader, List<DataObject>> translator : translators) {
                SwitchConnectionDistinguisher cookie = null;
                // Pass cookie only for PACKT_IN
                if (messageType.equals("PacketInMessage.class")) {
                    cookie = conductor.getAuxiliaryKey();
                }
                List<DataObject> translatorOutput = translator.translate(cookie, conductor.getSessionContext(), message);
                if(translatorOutput != null) {
                    result.addAll(translatorOutput);
                }
            }
            if (messageSpy != null) {
                messageSpy.spyIn(message);
                messageSpy.spyOut(result);
            }
        } else {
            LOG.warn("No translators for this message Type: {}", messageType);
        }
        return result;
    }

    private void pop(List<DataObject> processedMessages,ConnectionConductor conductor) {
        for (DataObject msg : processedMessages) {
            Class<? extends Object> registeredType =
                    registeredOutTypeExtractor.extractRegisteredType(msg);
            Collection<PopListener<DataObject>> popListeners = popListenersMapping.get(registeredType);
            if (popListeners == null) {
                LOG.warn("no popListener registered for type {}"+registeredType);
            } else {
                for (PopListener<DataObject> consumer : popListeners) {
                    consumer.onPop(msg);
                }
            }
        }
    }
}
