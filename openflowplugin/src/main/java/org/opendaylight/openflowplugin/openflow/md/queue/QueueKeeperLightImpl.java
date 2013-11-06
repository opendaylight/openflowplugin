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
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageListener;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author mirehak
 * @param <T> result type
 *
 */
public class QueueKeeperLightImpl<T> implements QueueKeeper<T> {
    
    private Set<PopListener<T>> listeners;
    private BlockingQueue<Ticket<T>> processQueue;
    private ScheduledThreadPoolExecutor pool;
    private int poolSize = 10;
    private Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping; 
    
    /**
     * prepare queue
     */
    public void init() {
        listeners = Collections.synchronizedSet(new HashSet<PopListener<T>>());
        processQueue = new LinkedBlockingQueue<>(100);
        pool = new ScheduledThreadPoolExecutor(poolSize);
        TicketFinisher<T> finisher = new TicketFinisher<>(processQueue, listeners);
        new Thread(finisher).start();
    }
    
    /**
     * stop processing queue
     */
    public void shutdown() {
        pool.shutdown();
    }

    @Override
    public void push(Class<? extends DataObject> registeredMessageType, DataObject message, ConnectionConductor conductor) {
        TicketImpl<T> ticket = new TicketImpl<>();
        ticket.setConductor(conductor);
        ticket.setMessage(message);
        ticket.setRegisteredMessageType(registeredMessageType);
        //TODO: block if queue limit reached 
        processQueue.add(ticket);
        scheduleTicket(ticket);
    }

    /**
     * @param ticket
     */
    private void scheduleTicket(Ticket<T> ticket) {
        pool.execute(TicketProcessorFactory.createProcessor(ticket, listenerMapping));
    }

    @Override
    public synchronized void addPopListener(PopListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized boolean removePopListener(PopListener<T> listener) {
        return listeners.remove(listener);
    }

    /**
     * @param poolSize the poolSize to set
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @Override
    public void setListenerMapping(
            Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping) {
        this.listenerMapping = listenerMapping;
    }
}
