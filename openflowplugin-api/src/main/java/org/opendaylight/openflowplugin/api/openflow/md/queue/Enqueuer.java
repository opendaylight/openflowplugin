/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.queue;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Enqueuer.
 * @param <I> type of queue items (IN)
 */
public interface Enqueuer<I> {

    /**
     * item to be enqueued.
     * @param queueItem item to be enqueued
     */
    void enqueueQueueItem(I queueItem);

    /**
     * for testing and comparing purposes - this strategy blocks netty threads.
     * @param queueItem item
     * @deprecated for testing and comparing purposes - this strategy blocks netty threads
     */
    @Deprecated
    void directProcessQueueItem(QueueItem<OfHeader> queueItem);
}
