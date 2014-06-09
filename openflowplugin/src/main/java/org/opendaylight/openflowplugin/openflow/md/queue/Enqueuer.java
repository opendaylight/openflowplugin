/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * @param <IN> type of queue items 
 */
public interface Enqueuer<IN> {
    
    /**
     * @param queueItem item to be enqueued
     */
    void enqueueQueueItem(IN queueItem);

    /**
     * @param queueItem
     * @deprecated for testing and comparing purposes - this strategy blocks netty threads
     */
    @Deprecated
    void directProcessQueueItem(QueueItem<OfHeader> queueItem);
}
