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
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <OUT> result type
 *
 */
public class TicketFinisher<OUT> implements Runnable {

    private static final Logger LOG = LoggerFactory
            .getLogger(TicketFinisher.class);

    private final BlockingQueue<TicketResult<OUT>> queue;
    private final Map<Class<? extends OUT>, Collection<PopListener<OUT>>> popListenersMapping;
    private final RegisteredTypeExtractor<OUT> registeredOutTypeExtractor;

    /**
     * @param queue
     * @param popListenersMapping
     * @param registeredOutTypeExtractor
     */
    public TicketFinisher(BlockingQueue<TicketResult<OUT>> queue,
            Map<Class<? extends OUT>, Collection<PopListener<OUT>>> popListenersMapping,
            RegisteredTypeExtractor<OUT> registeredOutTypeExtractor) {
        this.queue = queue;
        this.popListenersMapping = popListenersMapping;
        this.registeredOutTypeExtractor = registeredOutTypeExtractor;
    }


    @Override
    public void run() {
        while (true) {
            try {
                //TODO:: handle shutdown of queue
                TicketResult<OUT> result = queue.take();
                long before = System.nanoTime();
                LOG.debug("finishing ticket(before): {}, {} remain in queue, {} capacity remaining", System.identityHashCode(result),queue.size(), queue.remainingCapacity());
                List<OUT> processedMessages = result.getResult().get();
                long after = System.nanoTime();
                LOG.debug("finishing ticket(after): {}, {} remain in queue, {} capacity remaining, processingTime {} ns", System.identityHashCode(result),queue.size(), queue.remainingCapacity(),after-before);
                for (OUT msg : processedMessages) {
                    Class<? extends Object> registeredType =
                            registeredOutTypeExtractor.extractRegisteredType(msg);
                    Collection<PopListener<OUT>> popListeners = popListenersMapping.get(registeredType);
                    if (popListeners == null) {
                        LOG.warn("no popListener registered for type {}"+registeredType);
                    } else {
                        for (PopListener<OUT> consumer : popListeners) {
                            consumer.onPop(msg);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
