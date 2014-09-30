/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.opendaylight.openflowplugin.api.openflow.md.util.PollableQueuesZipper;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * QueueKeeper implementation based on {@link OfHeader}
 */
public class QueueKeeperFairImpl implements QueueKeeper<OfHeader> {

    private static Logger LOG = LoggerFactory
            .getLogger(QueueKeeperFairImpl.class);

    private Queue<QueueItem<OfHeader>> queueDefault;
    private BlockingQueue<QueueItem<OfHeader>> queueUnordered;
    private AutoCloseable pollRegistration;
    private int capacity = 5000;
    private HarvesterHandle harvesterHandle;
    private PollableQueuesZipper<QueueItem<OfHeader>> queueZipper;

    @Override
    public void close() throws Exception {
        Preconditions.checkNotNull(pollRegistration, "pollRegistration not available");
        pollRegistration.close();
    }

    @Override
    public void push(
            OfHeader message,
            ConnectionConductor conductor,
            QueueKeeper.QueueType queueType) {
        QueueItemOFImpl qItem = new QueueItemOFImpl(message, conductor, queueType);
        boolean enqueued = false;

        switch (queueType) {
        case DEFAULT:
            enqueued = queueDefault.offer(qItem);
            if(message instanceof NotificationQueueWrapper) {
                LOG.debug("enqueuing: [{}] -> enqueuing message [{}]", queueType, ((NotificationQueueWrapper)message).getNotification());
            }
            break;
        case UNORDERED:
            enqueued = queueUnordered.offer(qItem);
            break;
        default:
            LOG.warn("unsupported queue type: [{}] -> dropping message [{}]", queueType, message.getImplementedInterface());
        }

        if (enqueued) {
            harvesterHandle.ping();
        } else {
            LOG.warn("ingress throttling is use -> {} {}", queueType,qItem.getMessage());
        }

        // if enqueueing fails -> message will be dropped
    }

    /**
     * @return the ingressQueue
     */
    @Override
    public QueueItem<OfHeader> poll() {
        QueueItem<OfHeader> nextQueueItem = queueZipper.poll();
        if(nextQueueItem != null && nextQueueItem.getMessage() instanceof NotificationQueueWrapper) {
            LOG.debug("poll: -> remove message from queue [{}]", ((NotificationQueueWrapper)nextQueueItem.getMessage()).getNotification());
        }
        return nextQueueItem;
    }

    /**
     * @param processingRegistration the processingRegistration to set
     */
    @Override
    public void setPollRegistration(AutoCloseable processingRegistration) {
        this.pollRegistration = processingRegistration;
    }

    /**
     * @param capacity the capacity of internal blocking queue
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * init blocking queue
     */
    public void init() {
        queueUnordered = new ArrayBlockingQueue<>(capacity);
        queueDefault = new ArrayBlockingQueue<>(capacity);
        queueZipper = new PollableQueuesZipper<>();
        queueZipper.addSource(queueDefault);
        queueZipper.addSource(queueUnordered);
    }

    /**
     * @param harvesterHandle
     */
    public void setHarvesterHandle(HarvesterHandle harvesterHandle) {
        this.harvesterHandle = harvesterHandle;
    }
}
