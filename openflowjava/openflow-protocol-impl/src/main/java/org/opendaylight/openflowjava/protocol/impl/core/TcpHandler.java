/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementing server over TCP / TLS for handling incoming connections.
 *
 * @author michal.polkorab
 */
public class TcpHandler implements ServerFacade {
    /*
     * High/low write watermarks
     */
    private static final int DEFAULT_WRITE_HIGH_WATERMARK = 64 * 1024;
    private static final int DEFAULT_WRITE_LOW_WATERMARK = 32 * 1024;
    /*
     * Write spin count. This tells netty to immediately retry a non-blocking
     * write this many times before moving on to selecting.
     */
    private static final int DEFAULT_WRITE_SPIN_COUNT = 16;

    private static final Logger LOG = LoggerFactory.getLogger(TcpHandler.class);

    private final SettableFuture<Void> isOnlineFuture = SettableFuture.create();
    private final InetAddress startupAddress;
    private final Runnable readyRunnable;

    private int port;
    private String address;
    private EventLoopGroup workerGroup;
    private EventLoopGroup bossGroup;

    private TcpChannelInitializer channelInitializer;

    private Class<? extends ServerSocketChannel> socketChannelClass;

    /**
     * Constructor of TCPHandler that listens on selected port.
     *
     * @param port listening port of TCPHandler server
     */
    public TcpHandler(final int port, final Runnable readyRunnable) {
        this(null, port, readyRunnable);
    }

    /**
     * Constructor of TCPHandler that listens on selected address and port.
     * @param address listening address of TCPHandler server
     * @param port listening port of TCPHandler server
     */
    public TcpHandler(final InetAddress address, final int port, final Runnable readyRunnable) {
        this.port = port;
        startupAddress = address;
        this.readyRunnable = readyRunnable;
    }

    /**
     * Starts server on selected port.
     */
    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void run() {
        /*
         * We generally do not perform IO-unrelated tasks, so we want to have
         * all outstanding tasks completed before the executing thread goes
         * back into select.
         *
         * Any other setting means netty will measure the time it spent selecting
         * and spend roughly proportional time executing tasks.
         */
        //workerGroup.setIoRatio(100);

        final ChannelFuture f;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(socketChannelClass)
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

            if (startupAddress != null) {
                f = bootstrap.bind(startupAddress.getHostAddress(), port).sync();
            } else {
                f = bootstrap.bind(port).sync();
            }
        } catch (InterruptedException e) {
            LOG.error("Interrupted while binding port {}", port, e);
            return;
        } catch (Throwable throwable) {
            // sync() re-throws exceptions declared as Throwable, so the compiler doesn't see them
            LOG.error("Error while binding address {} and port {}", startupAddress, port, throwable);
            throw throwable;
        }

        try {
            InetSocketAddress isa = (InetSocketAddress) f.channel().localAddress();
            address = isa.getHostString();

            // Update port, as it may have been specified as 0
            port = isa.getPort();

            LOG.debug("address from tcphandler: {}", address);
            LOG.info("Switch listener started and ready to accept incoming tcp/tls connections on port: {}", port);
            readyRunnable.run();
            isOnlineFuture.set(null);

            // This waits until this channel is closed, and rethrows the cause of the failure if this future failed.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOG.error("Interrupted while waiting for port {} shutdown", port, e);
        } finally {
            shutdown();
        }
    }

    /**
     * Shuts down {@link TcpHandler}}.
     */
    @Override
    public ListenableFuture<Void> shutdown() {
        final var result = SettableFuture.<Void>create();
        workerGroup.shutdownGracefully();
        // boss will shutdown as soon, as worker is down
        bossGroup.shutdownGracefully().addListener(downResult -> {
            final var cause = downResult.cause();
            if (cause != null) {
                result.setException(cause);
            } else {
                result.set(null);
            }
        });
        return result;
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
    public ListenableFuture<Void> getIsOnlineFuture() {
        return isOnlineFuture;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public void setChannelInitializer(final TcpChannelInitializer channelInitializer) {
        this.channelInitializer = channelInitializer;
    }

    /**
     * Initiate event loop groups.
     *
     * @param threadConfiguration number of threads to be created, if not specified in threadConfig
     */
    public void initiateEventLoopGroups(final ThreadConfiguration threadConfiguration, final boolean isEpollEnabled) {
        if (isEpollEnabled) {
            initiateEpollEventLoopGroups(threadConfiguration);
        } else {
            initiateNioEventLoopGroups(threadConfiguration);
        }
    }

    /**
     * Initiate Nio event loop groups.
     *
     * @param threadConfiguration number of threads to be created, if not specified in threadConfig
     */
    public void initiateNioEventLoopGroups(final ThreadConfiguration threadConfiguration) {
        socketChannelClass = NioServerSocketChannel.class;
        if (threadConfiguration != null) {
            bossGroup = new NioEventLoopGroup(threadConfiguration.getBossThreadCount());
            workerGroup = new NioEventLoopGroup(threadConfiguration.getWorkerThreadCount());
        } else {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
        }
        ((NioEventLoopGroup)workerGroup).setIoRatio(100);
    }

    /**
     * Initiate Epoll event loop groups with Nio as fall back.
     *
     * @param threadConfiguration the ThreadConfiguration
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void initiateEpollEventLoopGroups(final ThreadConfiguration threadConfiguration) {
        try {
            socketChannelClass = EpollServerSocketChannel.class;
            if (threadConfiguration != null) {
                bossGroup = new EpollEventLoopGroup(threadConfiguration.getBossThreadCount());
                workerGroup = new EpollEventLoopGroup(threadConfiguration.getWorkerThreadCount());
            } else {
                bossGroup = new EpollEventLoopGroup();
                workerGroup = new EpollEventLoopGroup();
            }
            ((EpollEventLoopGroup)workerGroup).setIoRatio(100);
            return;
        } catch (RuntimeException ex) {
            LOG.debug("Epoll initiation failed");
        }

        //Fallback mechanism
        initiateNioEventLoopGroups(threadConfiguration);
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }
}
