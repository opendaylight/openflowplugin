/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    
    private Set<PopListener<DataObject>> listeners;
    private BlockingQueue<TicketResult<DataObject>> processQueue;
    private ScheduledThreadPoolExecutor pool;
    private int poolSize = 10;
    private Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, DataObject>>> translatorMapping;
    
    private VersionExtractor<OfHeader> versionExtractor = new VersionExtractor<OfHeader>() {
        @Override
        public Short extractVersion(OfHeader message) {
            return message.getVersion();
        }
    }; 
    
    /**
     * prepare queue
     */
    public void init() {
        listeners = Collections.synchronizedSet(new HashSet<PopListener<DataObject>>());
        processQueue = new LinkedBlockingQueue<>(100);
        pool = new ScheduledThreadPoolExecutor(poolSize);
        TicketFinisher<DataObject> finisher = new TicketFinisher<>(processQueue, listeners);
        new Thread(finisher).start();
    }
    
    /**
     * stop processing queue
     */
    public void shutdown() {
        pool.shutdown();
    }

    @Override
    public void push(Class<? extends OfHeader> registeredMessageType, OfHeader message, ConnectionConductor conductor) {
        TicketImpl<OfHeader, DataObject> ticket = new TicketImpl<>();
        ticket.setConductor(conductor);
        ticket.setMessage(message);
        ticket.setRegisteredMessageType(registeredMessageType);
        LOG.debug("ticket scheduling: {}, ticket: {}", registeredMessageType.getSimpleName(), System.identityHashCode(ticket));
        //TODO: block if queue limit reached 
        processQueue.add(ticket);
        scheduleTicket(ticket);
    }

    /**
     * @param ticket
     */
    private void scheduleTicket(Ticket<OfHeader, DataObject> ticket) {
        pool.execute(TicketProcessorFactory.createProcessor(ticket, versionExtractor, translatorMapping));
    }

    @Override
    public synchronized void addPopListener(PopListener<DataObject> listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized boolean removePopListener(PopListener<DataObject> listener) {
        return listeners.remove(listener);
    }

    /**
     * @param poolSize the poolSize to set
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @Override
    public void setTranslatorMapping(
            Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, DataObject>>> translatorMapping) {
        this.translatorMapping = translatorMapping;
    }
}
