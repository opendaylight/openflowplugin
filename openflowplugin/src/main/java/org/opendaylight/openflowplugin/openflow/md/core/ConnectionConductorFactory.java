/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.concurrent.atomic.AtomicInteger;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueKeeperType;

/**
 * @author mirehak
 *
 */
public abstract class ConnectionConductorFactory {

    private static AtomicInteger connectionIdentifier = new AtomicInteger(0);
    /**
     * @param connectionAdapter
     * @param queueKeeper 
     * @return conductor for given connection
     */
    public static ConnectionConductor createConductor(ConnectionAdapter connectionAdapter) {
        int connectionId;

        ConnectionConductor connectionConductor = new ConnectionConductorImpl(connectionAdapter);
        // set the connection-id for the connection-conductor
        connectionId = connectionIdentifier.incrementAndGet();
        connectionConductor.setConnectionId(connectionId);
        //Source the initial QueueKeeper instance from QueueKeeperPool and
        //inject to the ConnectionConductor
        connectionConductor.setQueueKeeper(MDController.getQueueKeeperPool().selectQueueKeeper(connectionId, QueueKeeperType.INITIAL));


        //connectionConductor.setQueueKeeper(queueKeeper);
        connectionConductor.init();
        return connectionConductor;
    }

}
