/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueItem;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper.QueueType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * QueueItem implementation based on {@link OfHeader}
 */
public class QueueItemOFImpl implements QueueItem<OfHeader> {

    private OfHeader message;
    private ConnectionConductor connectionConductor;
    private QueueType queueType;

    
    
    /**
     * @param message openflow message
     * @param connectionConductor switch connection conductor
     * @param queueType queue type
     */
    public QueueItemOFImpl(OfHeader message,
            ConnectionConductor connectionConductor, QueueType queueType) {
        this.message = message;
        this.connectionConductor = connectionConductor;
        this.queueType = queueType;
    }

    @Override
    public OfHeader getMessage() {
        return message;
    }

    @Override
    public ConnectionConductor getConnectionConductor() {
        return connectionConductor;
    }

    @Override
    public QueueType getQueueType() {
        return queueType;
    }
}
