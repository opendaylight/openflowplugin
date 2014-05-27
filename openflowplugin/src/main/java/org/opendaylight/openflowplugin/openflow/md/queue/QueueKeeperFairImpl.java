/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * 
 */
public class QueueKeeperFairImpl implements QueueKeeper<OfHeader> {

    private BlockingQueue<QueueItem<OfHeader>> queue;
    private AutoCloseable pollRegistration;
    private SettableFuture<Void> enqueuedFuture = null;
    private QueueItemOFImpl overflowSlot;
    private int capacity = 200;
    private int lowWaterMark;
    
    private HarvesterHandle harvesterHandle;

    @Override
    public void close() throws Exception {
        Preconditions.checkNotNull(pollRegistration, "pollRegistration not available");
        pollRegistration.close();
    }

    @Override
    public ListenableFuture<Void> push(
            OfHeader message,
            ConnectionConductor conductor,
            QueueKeeper.QueueType queueType) {
        Preconditions.checkState(enqueuedFuture == null, "there is already queue overflow handling in progress");
        QueueItemOFImpl qItem = new QueueItemOFImpl(message, conductor, queueType);
        boolean enqueued = queue.offer(qItem);
        if (enqueued) {
            harvesterHandle.ping();
        } else {
            enqueuedFuture = SettableFuture.create();
            conductor.getConnectionAdapter().setChannelAutoread(false);
            Preconditions.checkState(overflowSlot == null, "there is already overflowed item");
            overflowSlot = qItem;
        }
        return enqueuedFuture;
    }
    
    /**
     * @return the ingressQueue
     */
    @Override
    public QueueItem<OfHeader> poll() {
        QueueItem<OfHeader> nextQueueItem = queue.poll();
        if (enqueuedFuture != null) {
            if (queue.size() < lowWaterMark) {
                // strategy:
                //  - enqueue item in overflowSlot
                //  - switch on autoread
                //  - set future and destroy reference to it
                Preconditions.checkState(overflowSlot != null, "there is no overflowed item present");
                Preconditions.checkState(enqueuedFuture != null, "there is no overflowed handling in progress");
                
                ConnectionAdapter connectionAdapter = overflowSlot.getConnectionConductor().getConnectionAdapter();
                queue.offer(overflowSlot);
                overflowSlot = null;

                connectionAdapter.setChannelAutoread(true);
                enqueuedFuture.set(null);
                enqueuedFuture = null;
            }
        }
        return nextQueueItem;
    }

    /**
     * @param processingRegistration the processingRegistration to set
     */
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
        queue = new ArrayBlockingQueue<>(capacity);
        lowWaterMark = (int) (capacity * 0.5);
    }

    /**
     * @param harvesterHandle
     */
    public void setHarvesterHandle(HarvesterHandle harvesterHandle) {
        this.harvesterHandle = harvesterHandle;
    }
}
