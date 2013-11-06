/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 * @param <T> result type
 *
 */
public class TicketFinisher<T> implements Runnable {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(TicketFinisher.class);
    
    private final BlockingQueue<Ticket<T>> queue;
    private final Set<PopListener<T>> listeners;
    
    /**
     * @param queue
     * @param listeners
     */
    public TicketFinisher(BlockingQueue<Ticket<T>> queue,
            Set<PopListener<T>> listeners) {
        this.queue = queue;
        this.listeners = listeners;
    }


    @Override
    public void run() {
        try {
            Ticket<T> result = queue.take();
            for (PopListener<T> consumer : listeners) {
                consumer.onPop(result.getResult().get());
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
