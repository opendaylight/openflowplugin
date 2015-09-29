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

import org.opendaylight.openflowplugin.api.openflow.md.queue.PopListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TicketFinisherImpl implements TicketFinisher<DataObject> {

    private static final Logger LOG = LoggerFactory
            .getLogger(TicketFinisherImpl.class);

    private final Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenersMapping;

    private boolean finished;

    private BlockingQueue<TicketResult<DataObject>> queue;

    /**
     * @param queue ticket queue
     * @param popListenersMapping message pop listener
     */
    public TicketFinisherImpl(BlockingQueue<TicketResult<DataObject>> queue,
            Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenersMapping) {
        this.queue = queue;
        this.popListenersMapping = popListenersMapping;
    }

    @Override
    public void run() {
        while (! finished ) {
            try {
                //TODO:: handle shutdown of queue
                TicketResult<DataObject> result = queue.take();
                List<DataObject> processedMessages = result.getResult().get();
                firePopNotification(processedMessages);
            } catch (Exception e) {
                LOG.warn("processing (translate, publish) of ticket failed", e);
            }
        }
    }

    @Override
    public void firePopNotification(List<DataObject> processedMessages) {
        for (DataObject msg : processedMessages) {
            Class<? extends Object> registeredType =
                    msg.getImplementedInterface();
            Collection<PopListener<DataObject>> popListeners = popListenersMapping.get(registeredType);
            if (popListeners == null) {
                LOG.warn("no popListener registered for type {}", registeredType);
            } else {
                for (PopListener<DataObject> consumer : popListeners) {
                    try {
                        consumer.onPop(msg);
                    } catch (Exception e){
                        LOG.warn("firePopNotification: processing (translate, publish) of ticket failed for consumer {} msg {} Exception: ", consumer, msg,e);
                    }
                }
            }
        }
    }

    /**
     * initiate shutdown of this worker
     */
    @Override
    public void finish() {
        finished = true;
    }
}
