/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueKeeper.QueueType;

/**
 * @param <IN> input message type 
 */
public interface QueueItem<IN> {
    
    /**
     * @return wrapped message
     */
    IN getMessage();
    
    /**
     * @return conductor the message arrived to
     */
    ConnectionConductor getConnectionConductor();
    
    /**
     * @return queue type associated to this item 
     */
    QueueType getQueueType();
}
