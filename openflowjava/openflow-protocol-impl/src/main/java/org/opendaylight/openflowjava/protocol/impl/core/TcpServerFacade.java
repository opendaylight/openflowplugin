/*
 * Copyright (c) 2013 Pantheon Technologies, s.r.o. and others. All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementing server over TCP / TLS for handling incoming connections.
 *
 * @author michal.polkorab
 */
final class TcpServerFacade extends ServerFacade implements ConnectionInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(TcpServerFacade.class);

    /*
     * High/low write watermarks
     */
    private static final int DEFAULT_WRITE_HIGH_WATERMARK = 64 * 1024;
    private static final int DEFAULT_WRITE_LOW_WATERMARK = 32 * 1024;
    /*
     * Write spin count. This tells Netty to immediately retry a non-blocking write this many times before moving on to
     * selecting.
     */
    private static final int DEFAULT_WRITE_SPIN_COUNT = 16;

    private final TcpChannelInitializer channelInitializer;
    private final Bootstrap bootstrap;

    @GuardedBy("this")
    private EventLoopGroup childGroup;

    private TcpServerFacade(final EventLoopGroup parentGroup, final EventLoopGroup childGroup,
            final Bootstrap bootstrap, final TcpChannelInitializer channelInitializer,
            final InetSocketAddress localAddress) {
        super(parentGroup, localAddress);
        this.childGroup = requireNonNull(childGroup);
        this.bootstrap = requireNonNull(bootstrap);
        this.channelInitializer = requireNonNull(channelInitializer);

        // Log-and-hook to prevent surprise timing
        LOG.info("Switch listener started and ready to accept incoming TCP/TLS connections on {}", localAddress);
    }

    static ListenableFuture<TcpServerFacade> start(final ConnectionConfiguration connConfig, final boolean epollEnabled,
            final TcpChannelInitializer channelInitializer) {
        // Server bootstrap configuration
        final var serverBootstrap = new ServerBootstrap()
            .handler(new LoggingHandler(LogLevel.DEBUG))
            .childHandler(channelInitializer)
            .option(ChannelOption.SO_BACKLOG, 128)
            .option(ChannelOption.SO_REUSEADDR, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY , true)
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                new WriteBufferWaterMark(DEFAULT_WRITE_LOW_WATERMARK, DEFAULT_WRITE_HIGH_WATERMARK))
            .childOption(ChannelOption.WRITE_SPIN_COUNT, DEFAULT_WRITE_SPIN_COUNT);

        // Client bootstrap configuration
        final var bootstrap = new Bootstrap().handler(channelInitializer);

        /*
         * Initialize groups.
         *
         * We generally do not perform IO-unrelated tasks, so we want to have all outstanding tasks completed before
         * the executing thread goes back into select.
         *
         * Any other setting means Netty will measure the time it spent selecting and spend roughly proportional time
         * executing tasks.
         */
        final var threadConfig = connConfig.getThreadConfiguration();

        // Captured by bindFuture callback below

        final IoHandlerFactory ioFactory;
        if (Epoll.isAvailable() && epollEnabled) {
            // Epoll
            serverBootstrap.channel(EpollServerSocketChannel.class);
            bootstrap.channel(EpollSocketChannel.class);
            ioFactory = EpollIoHandler.newFactory();
        } else {
            // NIO
            serverBootstrap.channel(NioServerSocketChannel.class);
            bootstrap.channel(NioSocketChannel.class);
            ioFactory = NioIoHandler.newFactory();
        }

        final var parentGroup = new MultiThreadIoEventLoopGroup(
            threadConfig == null ? 0 : threadConfig.getBossThreadCount(), ioFactory);
        final var childGroup = new MultiThreadIoEventLoopGroup(
            threadConfig == null ? 0 : threadConfig.getWorkerThreadCount(), ioFactory);

        serverBootstrap.group(parentGroup, childGroup);
        bootstrap.group(childGroup);

        // Attempt to bind the address
        final var address = connConfig.getAddress();
        final var port = connConfig.getPort();
        final var bindFuture = address != null ? serverBootstrap.bind(address.getHostAddress(), port)
            : serverBootstrap.bind(port);

        // Clean up or hand off to caller
        final var retFuture = SettableFuture.<TcpServerFacade>create();
        bindFuture.addListener((ChannelFutureListener) future -> {
            final var cause = future.cause();
            if (cause != null) {
                childGroup.shutdownGracefully();
                parentGroup.shutdownGracefully();
                retFuture.setException(cause);
                return;
            }

            final var channel = future.channel();
            final var handler = new TcpServerFacade(parentGroup, childGroup, bootstrap, channelInitializer,
                (InetSocketAddress) channel.localAddress());
            // Hook onto the channel's termination to initiate group shutdown
            channel.closeFuture().addListener(closeFuture -> handler.shutdown());
            retFuture.set(handler);
        });
        return retFuture;
    }

    /**
     * Returns the number of connected clients / channels.
     *
     * @return number of connected clients / channels
     */
    public int getNumberOfConnections() {
        return channelInitializer.size();
    }

    @Override
    public void initiateConnection(final String host, final int port) {
        try {
            bootstrap.connect(host, port).sync();
        } catch (InterruptedException e) {
            LOG.error("Unable to initiate connection", e);
        }
    }

    @Override
    synchronized @NonNull ListenableFuture<Void> shutdown() {
        final var local = childGroup;
        if (local != null) {
            LOG.info("Cleaning up TCP/TLS connection resources on {}", localAddress());
            childGroup = null;
            local.shutdownGracefully();
        }
        return super.shutdown();
    }
}
