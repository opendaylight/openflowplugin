/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.List;

import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper.QueueType;

import com.google.common.util.concurrent.SettableFuture;

/**
 * @param <IN> source type
 * @param <OUT> result type
 */
public class TicketImpl<IN, OUT> implements Ticket<IN, OUT> {
    
    private IN message;
    private ConnectionConductor conductor;
    private SettableFuture<List<OUT>> future;
    private QueueType queueType;
    private List<OUT> directResult;
    
    /**
     * default ctor
     */
    public TicketImpl() {
        future = SettableFuture.create();
    }

    @Override
    public SettableFuture<List<OUT>> getResult() {
        return future;
    }

    /**
     * @return the message
     */
    @Override
    public IN getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(IN message) {
        this.message = message;
    }

    /**
     * @return the conductor
     */
    @Override
    public ConnectionConductor getConductor() {
        return conductor;
    }

    /**
     * @param conductor the conductor to set
     */
    public void setConductor(ConnectionConductor conductor) {
        this.conductor = conductor;
    }
    
    /**
     * @param queueType the queueType to set
     */
    public void setQueueType(QueueType queueType) {
        this.queueType = queueType;
    }
    
    @Override
    public QueueType getQueueType() {
        return queueType;
    }

    /**
     * @return the directResult
     */
    @Override
    public List<OUT> getDirectResult() {
        return directResult;
    }

    /**
     * @param directResult the directResult to set
     */
    @Override
    public void setDirectResult(List<OUT> directResult) {
        this.directResult = directResult;
    }
}
