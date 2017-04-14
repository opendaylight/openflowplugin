/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.util;

import java.util.Queue;

/**
 * Zipper groups together a list of queues and exposes one poll method. Polling
 * iterates through all groups and returns first not-null result of poll method
 * on each queue. If after polling each grouped queue for one time there is
 * still null result, poll will return null. <br>
 * Iterating keeps last position so this polling is supposed to be fairly
 * distributed.
 *
 * @param <T> common item type of zipped queues
 */
public class PollableQueuesPriorityZipper<T> {

    private Queue<T> prioritizedSource;
    private PollableQueuesZipper<T> zipper;

    public PollableQueuesPriorityZipper() {
        zipper = new PollableQueuesZipper<>();
    }

    /**
     * Add all member queues before first invocation of {@link PollableQueuesPriorityZipper#poll()}.
     * @param queue to be added to group
     */
    public void addSource(Queue<T> queue) {
        zipper.addSource(queue);
    }

    /**
     * Next common product.
     * @return next common product of polling member groups
     */
    public T poll() {
        T item = null;

        item = prioritizedSource.poll();
        if (item == null) {
            item = zipper.poll();
        }

        return item;
    }

    public void setPrioritizedSource(Queue<T> prioritizedSource) {
        this.prioritizedSource = prioritizedSource;
    }
}
