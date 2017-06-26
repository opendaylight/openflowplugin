/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.clients;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.Future;

import java.net.InetAddress;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

/**
 * Simple client for testing purposes
 *
 * @author michal.polkorab
 */
public class UdpSimpleClient implements OFClient {

    private static final Logger LOG = LoggerFactory.getLogger(UdpSimpleClient.class);
    private final String host;
    private final int port;
    private EventLoopGroup group;
    private SettableFuture<Boolean> isOnlineFuture;
    private SettableFuture<Boolean> scenarioDone;
    private ScenarioHandler scenarioHandler;

    /**
     * Constructor of class
     *
     * @param host address of host
     * @param port host listening port
     */
    public UdpSimpleClient(String host, int port) {
        this.host = host;
        this.port = port;
        init();
    }

    private void init() {
        isOnlineFuture = SettableFuture.create();
        scenarioDone = SettableFuture.create();
    }

    /**
     * Starting class of {@link UdpSimpleClient}
     */
    @Override
    public void run() {
        group = new NioEventLoopGroup();
        UdpSimpleClientInitializer clientInitializer = new UdpSimpleClientInitializer(isOnlineFuture);
        clientInitializer.setScenario(scenarioHandler);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, false)
                .handler(clientInitializer);

            b.connect(host, port).sync();

            synchronized (scenarioHandler) {
                LOG.debug("WAITING FOR SCENARIO");
                while (! scenarioHandler.isScenarioFinished()) {
                    scenarioHandler.wait();
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            LOG.debug("shutting down");
            try {
                group.shutdownGracefully().get();
                LOG.debug("shutdown succesful");
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
        return group.shutdownGracefully();
    }

    /**
     * Sets up {@link UdpSimpleClient} and fires run()
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String host;
        int port;
        UdpSimpleClient sc;
        if (args.length != 2) {
            LOG.error("Usage: {} <host> <port>", UdpSimpleClient.class.getSimpleName());
            LOG.error("Trying to use default setting.");
            InetAddress ia = InetAddress.getLocalHost();
            InetAddress[] all = InetAddress.getAllByName(ia.getHostName());
            host = all[0].getHostAddress();
            port = 6633;
            sc = new UdpSimpleClient(host, port);
        } else {
            host = args[0];
            port = Integer.parseInt(args[1]);
            sc = new UdpSimpleClient(host, port);
        }
        sc.run();

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

    @Override
    public void setSecuredClient(boolean securedClient) {
        // TODO: Finish implementation when DTLS is supported
    }
}
