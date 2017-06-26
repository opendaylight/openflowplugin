/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageConsumer;

/**
 * As UDP communication is handled only by one channel, it is needed
 * to store MessageConsumers, so that we know which consumer handles which channel

 * @author michal.polkorab
 */
public final class UdpConnectionMap {

    private static Map<InetSocketAddress, MessageConsumer> connectionMap = new ConcurrentHashMap<>();

    private UdpConnectionMap() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * @param address sender's address
     * @return corresponding MessageConsumer
     */
    public static MessageConsumer getMessageConsumer(InetSocketAddress address) {
        if(address == null){
            throw new IllegalArgumentException("Address can not be null");
        }
        return connectionMap.get(address);
    }

    /**
     * @param address sender's address
     * @param consumer MessageConsumer to be added / paired with specified address
     */
    public static void addConnection(InetSocketAddress address, MessageConsumer consumer) {
        if(address == null){
            throw new IllegalArgumentException("Address can not be null");
        }
        connectionMap.put(address, consumer);
    }

    /**
     * @param address sender's address
     */
    public static void removeConnection(InetSocketAddress address) {
        if(address == null){
            throw new IllegalArgumentException("Address can not be null");
        }
        connectionMap.remove(address);
    }
}