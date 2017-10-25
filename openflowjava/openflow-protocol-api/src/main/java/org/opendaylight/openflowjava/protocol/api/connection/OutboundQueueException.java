/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.connection;

/**
 * Exception reported when an exceptional event occurs on an {@link OutboundQueue},
 * which the {@link OutboundQueueHandler} needs to be aware of.
 */
public class OutboundQueueException extends Exception {
    /**
     * Exception reported when the device disconnects.
     */
    public static final OutboundQueueException DEVICE_DISCONNECTED = new OutboundQueueException("Device disconnected");

    private static final long serialVersionUID = 1L;

    public OutboundQueueException(final String message) {
        super(message);
    }

    public OutboundQueueException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
