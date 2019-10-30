/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes (TCP) connection to device.
 *
 * @author martin.uhlir
 */
public class TcpConnectionInitializer implements ServerFacade, ConnectionInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(TcpConnectionInitializer.class);

    private final EventLoopGroup workerGroup;
    private final boolean isEpollEnabled;
    private final SettableFuture<Boolean> hasRun = SettableFuture.create();

    private TcpChannelInitializer channelInitializer;
    private Bootstrap bootstrap;

    /**
     * Constructor.
     *
     * @param workerGroup - shared worker group
     */
    public TcpConnectionInitializer(EventLoopGroup workerGroup, boolean isEpollEnabled) {
        this.workerGroup = requireNonNull(workerGroup, "WorkerGroup can't be null");
        this.isEpollEnabled = isEpollEnabled;
    }

    @Override
    public void run() {
        bootstrap = new Bootstrap();
        if (isEpollEnabled) {
            bootstrap.group(workerGroup).channel(EpollSocketChannel.class).handler(channelInitializer);
        } else {
            bootstrap.group(workerGroup).channel(NioSocketChannel.class).handler(channelInitializer);
        }
        hasRun.set(true);
    }

    @Override
    public ListenableFuture<Boolean> shutdown() {
        final SettableFuture<Boolean> result = SettableFuture.create();
        workerGroup.shutdownGracefully();
        return result;
    }

    @Override
    public ListenableFuture<Boolean> getIsOnlineFuture() {
        return hasRun;
    }

    @Override
    public void setThreadConfig(ThreadConfiguration threadConfig) {
        // IGNORE
    }

    @Override
    public void initiateConnection(String host, int port) {
        try {
            bootstrap.connect(host, port).sync();
        } catch (InterruptedException e) {
            LOG.error("Unable to initiate connection", e);
        }
    }

    public void setChannelInitializer(TcpChannelInitializer channelInitializer) {
        this.channelInitializer = channelInitializer;
    }
}
