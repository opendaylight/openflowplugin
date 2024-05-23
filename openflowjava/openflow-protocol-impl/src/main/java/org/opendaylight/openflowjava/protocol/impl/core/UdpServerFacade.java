/*
 * Copyright (c) 2014 Pantheon Technologies, s.r.o. and others. All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementing server over UDP for handling incoming connections.
 *
 * @author michal.polkorab
 */
final class UdpServerFacade extends ServerFacade {
    private static final Logger LOG = LoggerFactory.getLogger(UdpServerFacade.class);

    private UdpServerFacade(final EventLoopGroup group, final InetSocketAddress localAddress) {
        super(group, localAddress);

        LOG.debug("Address from udpHandler: {}", localAddress);
        LOG.info("Switch listener started and ready to accept incoming udp connections on port: {}",
            localAddress.getPort());
    }

    static ListenableFuture<UdpServerFacade> start(final ConnectionConfiguration connConfig, final boolean epollEnabled,
            final UdpChannelInitializer channelInitializer) {
        // Client bootstrap configuration
        final var bootstrap = new Bootstrap().handler(channelInitializer).option(ChannelOption.SO_BROADCAST, false);
        final var threadConfig = connConfig.getThreadConfiguration();
        final var threadCount = threadConfig == null ? 0 : threadConfig.getWorkerThreadCount();

        // Captured by bindFuture callback below
        final EventLoopGroup group;
        if (Epoll.isAvailable() && epollEnabled) {
            // Epoll
            bootstrap.channel(EpollDatagramChannel.class);
            group = new EpollEventLoopGroup(threadCount);
        } else {
            // NIO
            bootstrap.channel(NioDatagramChannel.class);
            group = new NioEventLoopGroup(threadCount);
        }
        bootstrap.group(group);

        // Attempt to bind the address
        final var address = connConfig.getAddress();
        final var port = connConfig.getPort();
        final var bindFuture = address != null ? bootstrap.bind(address.getHostAddress(), port) : bootstrap.bind(port);

        // Clean up or hand off to caller
        final var retFuture = SettableFuture.<UdpServerFacade>create();
        bindFuture.addListener((ChannelFutureListener) future -> {
            final var cause = future.cause();
            if (cause != null) {
                group.shutdownGracefully();
                retFuture.setException(cause);
            } else {
                retFuture.set(new UdpServerFacade(group, (InetSocketAddress) future.channel().localAddress()));
            }
        });
        return retFuture;
    }
}
