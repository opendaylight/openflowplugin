/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 * @param <IN> source type
 * @param <OUT> result type
 *
 */
public class TicketFinisher<OUT> implements Runnable {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(TicketFinisher.class);
    
    private final BlockingQueue<TicketResult<OUT>> queue;
    private final Set<PopListener<OUT>> listeners;
    
    /**
     * @param queue
     * @param listeners
     */
    public TicketFinisher(BlockingQueue<TicketResult<OUT>> queue,
            Set<PopListener<OUT>> listeners) {
        this.queue = queue;
        this.listeners = listeners;
    }


    @Override
    public void run() {
        while (true) {
            try {
                //TODO:: handle shutdown of queue
                TicketResult<OUT> result = queue.take();
                List<OUT> processedMessage = result.getResult().get();
                LOG.debug("finishing ticket: {}", System.identityHashCode(result));
                for (PopListener<OUT> consumer : listeners) {
                    consumer.onPop(processedMessage);
                }
            } catch (ExecutionException | InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
