/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.queue;

import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;

/**
 * @param <I> input message type (IN)
 */
public interface QueueItem<I> {

    /**
     * @return wrapped message
     */
    I getMessage();

    /**
     * @return conductor the message arrived to
     */
    ConnectionConductor getConnectionConductor();

    /**
     * @return queue type associated to this item
     */
    QueueKeeper.QueueType getQueueType();
}
