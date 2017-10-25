/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Initializes (TCP) connection to device
 * @author martin.uhlir
 *
 */
public class TcpConnectionInitializer implements ServerFacade,
        ConnectionInitializer {

    private static final Logger LOG = LoggerFactory
            .getLogger(TcpConnectionInitializer.class);
    private EventLoopGroup workerGroup;
    private ThreadConfiguration threadConfig;

    private TcpChannelInitializer channelInitializer;
    private Bootstrap b;
    private boolean isEpollEnabled;

    /**
     * Constructor
     * @param workerGroup - shared worker group
     */
    public TcpConnectionInitializer(EventLoopGroup workerGroup, boolean isEpollEnabled) {
        Preconditions.checkNotNull(workerGroup, "WorkerGroup can't be null");
        this.workerGroup = workerGroup;
        this.isEpollEnabled = isEpollEnabled;
    }

    @Override
    public void run() {
        b = new Bootstrap();
        if(isEpollEnabled) {
            b.group(workerGroup).channel(EpollSocketChannel.class)
                    .handler(channelInitializer);
        } else {
            b.group(workerGroup).channel(NioSocketChannel.class)
                    .handler(channelInitializer);
        }
    }

    @Override
    public ListenableFuture<Boolean> shutdown() {
        final SettableFuture<Boolean> result = SettableFuture.create();
        workerGroup.shutdownGracefully();
        return result;
    }

    @Override
    public ListenableFuture<Boolean> getIsOnlineFuture() {
        return null;
    }

    @Override
    public void setThreadConfig(ThreadConfiguration threadConfig) {
        this.threadConfig = threadConfig;
    }

    @Override
    public void initiateConnection(String host, int port) {
        try {
            b.connect(host, port).sync();
        } catch (InterruptedException e) {
            LOG.error("Unable to initiate connection", e);
        }
    }

    /**
     * @param channelInitializer
     */
    public void setChannelInitializer(TcpChannelInitializer channelInitializer) {
        this.channelInitializer = channelInitializer;
    }
}
