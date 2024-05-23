/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static java.util.Objects.requireNonNull;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes (TCP) connection to device.
 *
 * @author martin.uhlir
 */
final class TcpConnectionInitializer implements ConnectionInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(TcpConnectionInitializer.class);

    private final Bootstrap bootstrap;

    TcpConnectionInitializer(final EventLoopGroup workerGroup, final TcpChannelInitializer channelInitializer,
            final boolean isEpollEnabled) {
        bootstrap = new Bootstrap()
            .group(requireNonNull(workerGroup, "WorkerGroup cannot be null"))
            .handler(channelInitializer)
            .channel(isEpollEnabled ? EpollSocketChannel.class : NioSocketChannel.class);
    }

    @Override
    public void initiateConnection(final String host, final int port) {
        try {
            bootstrap.connect(host, port).sync();
        } catch (InterruptedException e) {
            LOG.error("Unable to initiate connection", e);
        }
    }
}
