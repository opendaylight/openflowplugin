/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.yangtools.yang.binding.DataObject;

import com.google.common.util.concurrent.SettableFuture;

/**
 * @author mirehak
 * @param <T> result type
 *
 */
public class TicketImpl<T> implements Ticket<T> {

    private DataObject message;
    private ConnectionConductor conductor;
    private SettableFuture<T> future;
    private Class<? extends DataObject> registeredMessageType;

    /**
     * default ctor
     */
    public TicketImpl() {
        future = SettableFuture.create();
    }

    @Override
    public SettableFuture<T> getResult() {
        return future;
    }

    /**
     * @return the message
     */
    @Override
    public DataObject getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(DataObject message) {
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
     * @param registeredMessageType
     */
    public void setRegisteredMessageType(
            Class<? extends DataObject> registeredMessageType) {
        this.registeredMessageType = registeredMessageType;
    }

    /**
     * @return the registeredMessageType
     */
    @Override
    public Class<? extends DataObject> getRegisteredMessageType() {
        return registeredMessageType;
    }
}
