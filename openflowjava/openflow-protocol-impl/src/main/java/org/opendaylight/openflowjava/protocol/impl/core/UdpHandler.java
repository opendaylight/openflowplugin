/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementing server over UDP for handling incoming connections.
 *
 * @author michal.polkorab
 */
public final class UdpHandler implements ServerFacade {

    private static final Logger LOG = LoggerFactory.getLogger(UdpHandler.class);

    private int port;
    private EventLoopGroup group;
    private final InetAddress startupAddress;
    private final Runnable readyRunnable;
    private final SettableFuture<Boolean> isOnlineFuture;
    private UdpChannelInitializer channelInitializer;
    private ThreadConfiguration threadConfig;
    private Class<? extends DatagramChannel> datagramChannelClass;

    /**
     * Constructor of UdpHandler that listens on selected port.
     *
     * @param port listening port of UdpHandler server
     */
    public UdpHandler(final int port, Runnable readyRunnable) {
        this(null, port, readyRunnable);
    }

    /**
     * Constructor of UdpHandler that listens on selected address and port.
     * @param address listening address of UdpHandler server
     * @param port listening port of UdpHandler server
     */
    public UdpHandler(final InetAddress address, final int port, Runnable readyRunnable) {
        this.port = port;
        this.startupAddress = address;
        isOnlineFuture = SettableFuture.create();
        this.readyRunnable = readyRunnable;
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void run() {
        final ChannelFuture f;
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(datagramChannelClass).option(ChannelOption.SO_BROADCAST, false)
                .handler(channelInitializer);

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
            String address = isa.getHostString();

            // Update port, as it may have been specified as 0
            this.port = isa.getPort();

            LOG.debug("Address from udpHandler: {}", address);
            isOnlineFuture.set(true);
            LOG.info("Switch listener started and ready to accept incoming udp connections on port: {}", port);
            readyRunnable.run();
            // This waits until this channel is closed, and rethrows the cause of the failure if this future failed.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOG.error("Interrupted while waiting for port {} shutdown", port, e);
        } finally {
            shutdown();
        }
    }

    @Override
    public ListenableFuture<Boolean> shutdown() {
        final SettableFuture<Boolean> result = SettableFuture.create();
        group.shutdownGracefully().addListener(downResult -> {
            result.set(downResult.isSuccess());
            if (downResult.cause() != null) {
                result.setException(downResult.cause());
            }
        });
        return result;
    }

    @Override
    public ListenableFuture<Boolean> getIsOnlineFuture() {
        return isOnlineFuture;
    }

    public int getPort() {
        return port;
    }

    public void setChannelInitializer(UdpChannelInitializer channelInitializer) {
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void setThreadConfig(ThreadConfiguration threadConfig) {
        this.threadConfig = threadConfig;
    }

    /**
     * Initiate event loop groups.
     *
     * @param threadConfiguration number of threads to be created, if not specified in threadConfig
     */
    public void initiateEventLoopGroups(ThreadConfiguration threadConfiguration, boolean isEpollEnabled) {

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
    public void initiateNioEventLoopGroups(ThreadConfiguration threadConfiguration) {
        datagramChannelClass = NioDatagramChannel.class;
        if (threadConfiguration != null) {
            group = new NioEventLoopGroup(threadConfiguration.getWorkerThreadCount());
        } else {
            group = new NioEventLoopGroup();
        }
    }

    /**
     * Initiate Epoll event loop groups with Nio as fall back.
     *
     * @param threadConfiguration the ThreadConfiguration
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void initiateEpollEventLoopGroups(ThreadConfiguration threadConfiguration) {
        try {
            datagramChannelClass = EpollDatagramChannel.class;
            if (threadConfiguration != null) {
                group = new EpollEventLoopGroup(threadConfiguration.getWorkerThreadCount());
            } else {
                group = new EpollEventLoopGroup();
            }
            return;
        } catch (RuntimeException ex) {
            LOG.debug("Epoll initiation failed");
        }

        //Fallback mechanism
        initiateNioEventLoopGroups(threadConfiguration);
    }
}
