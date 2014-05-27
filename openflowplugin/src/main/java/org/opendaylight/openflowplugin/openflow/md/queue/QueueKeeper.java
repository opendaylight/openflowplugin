/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * This processing mechanism based on queue. Processing consists of 2 steps: translate and publish. 
 * Proposed workflow (might slightly deviate in implementations):
 * <ol>
 * <li>messages of input type are pushed in (via {@link QueueKeeper#push(Object, ConnectionConductor)} and similar)</li>
 * <li>ticket (executable task) is build upon each pushed message and enqueued</li>
 * <li>ticket is translated using appropriate translator</li>
 * <li>ticket is dequeued and result is published by appropriate popListener</li>
 * </ol>
 * Message order might be not important, e.g. when speed is of the essence
 * @param <IN> source type
 * @param <OUT> result type
 */
public interface QueueKeeper<IN> extends AutoCloseable {
    
    /** type of message enqueue */
    public enum QueueType {
        /** ordered processing */
        DEFAULT,
        /** unordered processing - bypass fair processing */
        UNORDERED}

    /**
     * enqueue message for processing
     * @param message
     * @param conductor source of message
     * @param queueType - {@link QueueType#DEFAULT} if message order matters, {@link QueueType#UNORDERED} otherwise
     * @return if queue available -> null, if queue full -> Future (value is set when queue is at least 50% free) 
     */
    ListenableFuture<Void> push(IN message, ConnectionConductor conductor, QueueType queueType);

    /**
     * @return oldest item from queue - if available and remove it from queue
     */
    QueueItem<IN> poll();
}
