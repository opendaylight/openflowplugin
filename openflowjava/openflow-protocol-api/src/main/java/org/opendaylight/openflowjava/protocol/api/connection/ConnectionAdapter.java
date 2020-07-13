/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.connection;

import com.google.common.annotations.Beta;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.extensibility.AlienMessageListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;

/**
 * Manages a switch connection.
 *
 * @author mirehak
 * @author michal.polkorab
 */
public interface ConnectionAdapter extends OpenflowProtocolService {

    /**
     * Disconnect corresponding switch.
     *
     * @return future set to true, when disconnect completed
     */
    Future<Boolean> disconnect();

    /**
     * Determines if the connection to the switch is alive.
     *
     * @return true, if connection to switch is alive
     */
    boolean isAlive();

    /**
     * Returns the address of the connected switch.
     *
     * @return address of the remote end - address of a switch if connected
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Sets the protocol message listener.
     *
     * @param messageListener here will be pushed all messages from switch
     */
    void setMessageListener(OpenflowProtocolListener messageListener);

    /**
     * Sets the system message listener.
     *
     * @param systemListener here will be pushed all system messages from library
     */
    void setSystemListener(SystemNotificationsListener systemListener);

    /**
     * Set handler for alien messages received from device.
     *
     * @param alienMessageListener here will be pushed all alien messages from switch
     */
    void setAlienMessageListener(AlienMessageListener alienMessageListener);

    /**
     * Throws exception if any of required listeners is missing.
     */
    void checkListeners();

    /**
     * Notify listener about connection ready-to-use event.
     */
    void fireConnectionReadyNotification();

    /**
     * Notify listener about switch certificate information.
     * @param switchcertificate X509Certificate of switch
     */
    void onSwitchCertificateIdentified(X509Certificate switchcertificate);

    /**
     * Set listener for connection became ready-to-use event.
     *
     * @param connectionReadyListener listens to connection ready event
     */
    void setConnectionReadyListener(ConnectionReadyListener connectionReadyListener);

    /**
     * Sets option for automatic channel reading - if set to false, incoming messages won't be read.
     *
     * @param autoRead target value to be switched to
     */
    void setAutoRead(boolean autoRead);

    /**
     * Determines if the channel is configured to auto-read.
     *
     * @return true, if channel is configured to auto-read
     */
    boolean isAutoRead();

    /**
     * Registers a new bypass outbound queue.
     *
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

    /**
     * Set datapathId for the dpn.
     * @param datapathId of the dpn
     */
    void setDatapathId(BigInteger datapathId);

    /**
     * Sets executorService.
     * @param executorService for all dpns
     */
    void setExecutorService(ExecutorService executorService);
}
