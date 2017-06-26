/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;

/**
 * Wraps outgoing message and includes listener attached to this message. This object
 * is sent to OFEncoder. When OFEncoder fails to serialize the message,
 * listener is filled with exception. The exception is then delegated to upper ODL layers.
 * This object is used for UDP communication - it also carries recipient address

 * @author michal.polkorab
 */
public class UdpMessageListenerWrapper extends MessageListenerWrapper {

    private InetSocketAddress address;

    /**
     * @param msg message to be sent
     * @param listener listener attached to channel.write(msg) Future
     * @param address recipient's address
     */
    public UdpMessageListenerWrapper(Object msg, GenericFutureListener<Future<Void>> listener,
            InetSocketAddress address) {
        super(msg, listener);
        this.address = address;
    }

    /**
     * @return recipient address
     */
    public InetSocketAddress getAddress() {
        return address;
    }
}