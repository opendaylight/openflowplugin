/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.connection;

import com.google.common.annotations.Beta;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.extensibility.AlienMessageListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;

/**
 * @author mirehak
 * @author michal.polkorab
 */
public interface ConnectionAdapter extends OpenflowProtocolService {

    /**
     * disconnect corresponding switch
     * @return future set to true, when disconnect completed
     */
    Future<Boolean> disconnect();

    /**
     * @return true, if connection to switch is alive
     */
    boolean isAlive();

    /**
     * @return address of the remote end - address of a switch if connected
     */
    InetSocketAddress getRemoteAddress();

    /**
     * @param messageListener here will be pushed all messages from switch
     */
    void setMessageListener(OpenflowProtocolListener messageListener);

    /**
     * @param systemListener here will be pushed all system messages from library
     */
    void setSystemListener(SystemNotificationsListener systemListener);

    /**
     * Set handler for alien messages received from device
     * @param alienMessageListener here will be pushed all alien messages from switch
     */
    void setAlienMessageListener(AlienMessageListener alienMessageListener);

    /**
     * Throws exception if any of required listeners is missing
     */
    void checkListeners();

    /**
     * notify listener about connection ready-to-use event
     */
    void fireConnectionReadyNotification();

    /**
     * set listener for connection became ready-to-use event
     * @param connectionReadyListener listens to connection ready event
     */
    void setConnectionReadyListener(ConnectionReadyListener connectionReadyListener);

    /**
     * sets option for automatic channel reading;
     * if set to false, incoming messages won't be read
     *
     * @param autoRead target value to be switched to
     */
    void setAutoRead(boolean autoRead);

    /**
     * @return true, if channel is configured to autoread
     */
    boolean isAutoRead();

    /**
     * Registers a new bypass outbound queue
     * @param <T> handler type
     * @param handler queue handler
     * @param maxQueueDepth max amount of not confirmed messaged in queue (i.e. edge for barrier message)
     * @param maxBarrierNanos regular base for barrier message
     * @return An {@link OutboundQueueHandlerRegistration}
     */
    @Beta
    <T extends OutboundQueueHandler> OutboundQueueHandlerRegistration<T> registerOutboundQueueHandler(T handler,
        int maxQueueDepth, long maxBarrierNanos);

    /**
     * Set filtering of PacketIn messages. By default these messages are not filtered.
     * @param enabled True if PacketIn messages should be filtered, false if they should be reported.
     */
    @Beta
    void setPacketInFiltering(boolean enabled);
}
