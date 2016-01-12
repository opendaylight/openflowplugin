/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import com.google.common.base.Preconditions;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.queue.HarvesterHandle;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueItem;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper;
import org.opendaylight.openflowplugin.api.openflow.md.queue.WaterMarkListener;
import org.opendaylight.openflowplugin.api.openflow.md.util.PollableQueuesPriorityZipper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QueueKeeper implementation based on {@link OfHeader}
 */
public class QueueKeeperFairImpl implements QueueKeeper<OfHeader> {

    private static final Logger LOG = LoggerFactory
            .getLogger(QueueKeeperFairImpl.class);

    private Queue<QueueItem<OfHeader>> queueDefault;
    private BlockingQueue<QueueItem<OfHeader>> queueUnordered;
    private AutoCloseable pollRegistration;
    private int capacity = 5000;
    private HarvesterHandle harvesterHandle;
    private PollableQueuesPriorityZipper<QueueItem<OfHeader>> queueZipper;

    private WaterMarkListener waterMarkListener;

    @Override
    public void close() throws Exception {
        Preconditions.checkNotNull(pollRegistration,
                "pollRegistration not available");
        pollRegistration.close();
    }

    @Override
    public void push(OfHeader message, ConnectionConductor conductor,
            QueueKeeper.QueueType queueType) {
        QueueItemOFImpl qItem = new QueueItemOFImpl(message, conductor,
                queueType);
        boolean enqueued = false;

        switch (queueType) {
        case DEFAULT:
            enqueued = queueDefault.offer(qItem);
            break;
        case UNORDERED:
            enqueued = queueUnordered.offer(qItem);
            break;
        default:
            LOG.warn("unsupported queue type: [{}] -> dropping message [{}]",
                    queueType, message.getImplementedInterface());
        }

        if (enqueued) {
            harvesterHandle.ping();
        } else {
            LOG.debug("ingress throttling is use -> {}", queueType);
        }

        // if enqueueing fails -> message will be dropped
    }

    /**
     * @return the ingressQueue
     */
    @Override
    public QueueItem<OfHeader> poll() {
        return queueZipper.poll();
    }

    /**
     * @param processingRegistration
     *            the processingRegistration to set
     */
    @Override
    public void setPollRegistration(AutoCloseable processingRegistration) {
        this.pollRegistration = processingRegistration;
    }

    /**
     * @param capacity
     *            the capacity of internal blocking queue
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * init blocking queue
     */
    public void init() {
        Preconditions.checkNotNull(waterMarkListener);
        queueUnordered = new ArrayBlockingQueue<>(capacity);
        queueDefault = new ArrayBlockingQueue<>(capacity);
        WrapperQueueImpl<QueueItem<OfHeader>> wrapperQueue = new WrapperQueueImpl<>(
                capacity, queueDefault, waterMarkListener);
        queueZipper = new PollableQueuesPriorityZipper<>();
        queueZipper.addSource(queueUnordered);
        queueZipper.setPrioritizedSource(wrapperQueue);
    }

    public void setWaterMarkListener(WaterMarkListener waterMarkListener) {
        this.waterMarkListener = waterMarkListener;
    }

    /**
     * @param harvesterHandle harvester handle
     */
    public void setHarvesterHandle(HarvesterHandle harvesterHandle) {
        this.harvesterHandle = harvesterHandle;
    }
}
