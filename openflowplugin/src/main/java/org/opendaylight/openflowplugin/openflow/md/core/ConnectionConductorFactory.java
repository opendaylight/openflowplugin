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
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueProcessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author mirehak
 *
 */
public final class ConnectionConductorFactory {

    private static AtomicInteger conductorId = new AtomicInteger();
    
    private ConnectionConductorFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param connectionAdapter connection conductor adaptor
     * @param queueProcessor  message queue process
     * @return conductor for given connection
     */
    public static ConnectionConductor createConductor(ConnectionAdapter connectionAdapter,
            QueueProcessor<OfHeader, DataObject> queueProcessor) {
        ConnectionConductor connectionConductor = new ConnectionConductorImpl(connectionAdapter);
        connectionConductor.setQueueProcessor(queueProcessor);
        connectionConductor.setId(conductorId.getAndIncrement());
        connectionConductor.init();
        return connectionConductor;
    }

}
