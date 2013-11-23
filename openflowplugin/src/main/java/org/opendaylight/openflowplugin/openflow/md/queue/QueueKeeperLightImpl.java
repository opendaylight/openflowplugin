/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
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
        processQueue = new LinkedBlockingQueue<>(100);
        pool = new ScheduledThreadPoolExecutor(poolSize);
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
        TicketImpl<OfHeader, DataObject> ticket = new TicketImpl<>();
        ticket.setConductor(conductor);
        ticket.setMessage(message);
        LOG.debug("ticket scheduling: {}, ticket: {}",
                message.getImplementedInterface().getSimpleName(), System.identityHashCode(ticket));
        //TODO: block if queue limit reached
        processQueue.add(ticket);
        scheduleTicket(ticket);
    }

    /**
     * @param ticket
     */
    private void scheduleTicket(Ticket<OfHeader, DataObject> ticket) {
        pool.execute(TicketProcessorFactory.createProcessor(ticket, versionExtractor,
                registeredSrcTypeExtractor, translatorMapping));
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
}
