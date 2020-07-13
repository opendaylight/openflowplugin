/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLEngine;
import org.opendaylight.openflowjava.protocol.impl.core.connection.ConnectionAdapterFactory;
import org.opendaylight.openflowjava.protocol.impl.core.connection.ConnectionAdapterFactoryImpl;
import org.opendaylight.openflowjava.protocol.impl.core.connection.ConnectionFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes TCP / TLS channel.
 * @author michal.polkorab
 */
public class TcpChannelInitializer extends ProtocolChannelInitializer<SocketChannel> {

    private static final Logger LOG = LoggerFactory.getLogger(TcpChannelInitializer.class);
    private final DefaultChannelGroup allChannels;
    private final ConnectionAdapterFactory connectionAdapterFactory;

    /**
     * Default constructor.
     */
    public TcpChannelInitializer() {
        this(new DefaultChannelGroup("netty-receiver", null), new ConnectionAdapterFactoryImpl());
    }

    /**
     * Testing constructor.
     */
    protected TcpChannelInitializer(final DefaultChannelGroup channelGroup,
            final ConnectionAdapterFactory connAdaptorFactory) {
        allChannels = channelGroup;
        connectionAdapterFactory = connAdaptorFactory;
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void initChannel(final SocketChannel ch) {
        if (ch.remoteAddress() != null) {
            final InetAddress switchAddress = ch.remoteAddress().getAddress();
            final int port = ch.localAddress().getPort();
            final int remotePort = ch.remoteAddress().getPort();
            LOG.debug("Incoming connection from (remote address): {}:{} --> :{}",
                switchAddress.toString(), remotePort, port);

            if (!getSwitchConnectionHandler().accept(switchAddress)) {
                ch.disconnect();
                LOG.debug("Incoming connection rejected");
                return;
            }
        }
        LOG.debug("Incoming connection accepted - building pipeline");
        allChannels.add(ch);
        ConnectionFacade connectionFacade = null;
        connectionFacade = connectionAdapterFactory.createConnectionFacade(ch, null, useBarrier(),
                getChannelOutboundQueueSize());
        try {
            LOG.debug("Calling OF plugin: {}", getSwitchConnectionHandler());
            getSwitchConnectionHandler().onSwitchConnected(connectionFacade);
            connectionFacade.checkListeners();
            boolean tlsPresent = false;

            // If this channel is configured to support SSL it will only support SSL
            if (getTlsConfiguration() != null) {
                tlsPresent = true;
                final SslContextFactory sslFactory = new SslContextFactory(getTlsConfiguration());
                final SSLEngine engine = sslFactory.getServerContext().createSSLEngine();
                engine.setNeedClientAuth(true);
                engine.setUseClientMode(false);
                List<String> suitesList = getTlsConfiguration().getCipherSuites();
                if (suitesList != null && !suitesList.isEmpty()) {
                    LOG.debug("Requested Cipher Suites are: {}", suitesList);
                    String[] suites = suitesList.toArray(new String[suitesList.size()]);
                    engine.setEnabledCipherSuites(suites);
                    LOG.debug("Cipher suites enabled in SSLEngine are: {}",
                            Arrays.toString(engine.getEnabledCipherSuites()));
                }
                final SslHandler ssl = new SslHandler(engine);
                final Future<Channel> handshakeFuture = ssl.handshakeFuture();
                final ConnectionFacade finalConnectionFacade = connectionFacade;
                if (sslFactory.isCustomTrustManagerEnabled()) {
                    handshakeFuture.addListener(future -> finalConnectionFacade
                            .onSwitchCertificateIdentified(sslFactory.getSwitchCertificate()));
                }
                handshakeFuture.addListener(future -> finalConnectionFacade.fireConnectionReadyNotification());
                ch.pipeline().addLast(PipelineHandlers.SSL_HANDLER.name(), ssl);
            }
            ch.pipeline().addLast(PipelineHandlers.OF_FRAME_DECODER.name(),
                    new OFFrameDecoder(connectionFacade, tlsPresent));
            ch.pipeline().addLast(PipelineHandlers.OF_VERSION_DETECTOR.name(), new OFVersionDetector());
            final OFDecoder ofDecoder = new OFDecoder();
            ofDecoder.setDeserializationFactory(getDeserializationFactory());
            ch.pipeline().addLast(PipelineHandlers.OF_DECODER.name(), ofDecoder);
            final OFEncoder ofEncoder = new OFEncoder();
            ofEncoder.setSerializationFactory(getSerializationFactory());
            ch.pipeline().addLast(PipelineHandlers.OF_ENCODER.name(), ofEncoder);
            ch.pipeline().addLast(PipelineHandlers.IDLE_HANDLER.name(),
                    new IdleHandler(getSwitchIdleTimeout(), TimeUnit.MILLISECONDS));
            ch.pipeline().addLast(PipelineHandlers.DELEGATING_INBOUND_HANDLER.name(),
                    new DelegatingInboundHandler(connectionFacade));

            if (!tlsPresent) {
                connectionFacade.fireConnectionReadyNotification();
            }
        } catch (RuntimeException e) {
            LOG.warn("Failed to initialize channel", e);
            ch.close();
        }
    }

    /**
     * Returns the connection iterator.
     *
     * @return iterator through active connections
     */
    public Iterator<Channel> getConnectionIterator() {
        return allChannels.iterator();
    }

    /**
     * Returns the number of active channels.
     *
     * @return amount of active channels
     */
    public int size() {
        return allChannels.size();
    }
}
