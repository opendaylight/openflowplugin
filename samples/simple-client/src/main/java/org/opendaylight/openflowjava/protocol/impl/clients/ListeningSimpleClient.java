/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.clients;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

/**
 * Listening client for testing purposes
 * @author martin.uhlir
 *
 */
public class ListeningSimpleClient implements OFClient {

    private static final Logger LOG = LoggerFactory.getLogger(ListeningSimpleClient.class);
    private int port;
    private boolean securedClient = false;
    private EventLoopGroup workerGroup;
    private SettableFuture<Boolean> isOnlineFuture;
    private SettableFuture<Boolean> scenarioDone;
    private ScenarioHandler scenarioHandler;

    /**
     * Constructor of the class
     *
     * @param port host listening port
     */
    public ListeningSimpleClient(int port) {
        this.port = port;
        init();
    }

    private void init() {
        isOnlineFuture = SettableFuture.create();
        scenarioDone = SettableFuture.create();
    }

    /**
     * Starting class of {@link ListeningSimpleClient}
     */
    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        SimpleClientInitializer clientInitializer = new SimpleClientInitializer(isOnlineFuture, securedClient);
        clientInitializer.setScenario(scenarioHandler);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(clientInitializer);

            ChannelFuture f = b.bind(port).sync();
            // Update port, as it may have been specified as 0
            this.port = ((InetSocketAddress) f.channel().localAddress()).getPort();
            isOnlineFuture.set(true);

            synchronized (scenarioHandler) {
                LOG.debug("WAITING FOR SCENARIO");
                while (! scenarioHandler.isScenarioFinished()) {
                    scenarioHandler.wait();
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            LOG.debug("listening client shutting down");
            try {
                workerGroup.shutdownGracefully().get();
                bossGroup.shutdownGracefully().get();
                LOG.debug("listening client shutdown succesful");
            } catch (InterruptedException | ExecutionException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        scenarioDone.set(true);
    }

    /**
     * @return close future
     */
    public Future<?> disconnect() {
        LOG.debug("disconnecting client");
        return workerGroup.shutdownGracefully();
    }

    @Override
    public void setSecuredClient(boolean securedClient) {
        this.securedClient = securedClient;
    }

    @Override
    public SettableFuture<Boolean> getIsOnlineFuture() {
        return isOnlineFuture;
    }

    @Override
    public SettableFuture<Boolean> getScenarioDone() {
        return scenarioDone;
    }

    @Override
    public void setScenarioHandler(ScenarioHandler scenario) {
        this.scenarioHandler = scenario;
    }

    /**
     * @return actual port number
     */
    public int getPort() {
        return this.port;
    }
}
