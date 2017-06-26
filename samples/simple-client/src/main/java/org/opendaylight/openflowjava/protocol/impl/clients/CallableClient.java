/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.clients;

import java.net.InetAddress;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.LoggerFactory;


/**
 * Callable client class, inspired by SimpleClient class
 * Simulating device/switch connected to controller
 * @author Jozef Bacigal
 * Date: 4.3.2016.
 */
public class CallableClient implements Callable<Boolean>, OFClient {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CallableClient.class);

    private int port = 6653;
    private boolean securedClient = false;
    private InetAddress ipAddress = null;
    private String name = "Empty name";

    private final EventLoopGroup workerGroup;
    private SettableFuture<Boolean> isOnlineFuture;
    private SettableFuture<Boolean> scenarioDone;
    private ScenarioHandler scenarioHandler = null;
    private Bootstrap bootstrap = null;

    public CallableClient(
            final int port,
            final boolean securedClient,
            final InetAddress ipAddress,
            final String name,
            final ScenarioHandler scenarioHandler,
            final Bootstrap bootstrap,
            final EventLoopGroup eventExecutors) {

        Preconditions.checkNotNull(ipAddress, "IP address cannot be null");
        Preconditions.checkNotNull(scenarioHandler, "Scenario handler cannot be null");
        this.port = port;
        this.securedClient = securedClient;
        this.ipAddress = ipAddress;
        this.workerGroup = eventExecutors;
        this.bootstrap = bootstrap;
        this.name = name;
        this.scenarioHandler = scenarioHandler;
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
    public void setScenarioHandler(final ScenarioHandler scenario) {
        this.scenarioHandler = scenario;
    }

    @Override
    public void setSecuredClient(final boolean securedClient) {
        this.securedClient = securedClient;
    }


    @Override
    public Boolean call() throws Exception {
        Preconditions.checkNotNull(bootstrap);
        Preconditions.checkNotNull(workerGroup);
        LOG.info("Switch {} trying connect to controller", this.name);
        SimpleClientInitializer clientInitializer = new SimpleClientInitializer(isOnlineFuture, securedClient);
        clientInitializer.setScenario(scenarioHandler);
        try {
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(clientInitializer);

            bootstrap.connect(ipAddress, port).sync();
            synchronized (scenarioHandler) {
                LOG.debug("WAITING FOR SCENARIO");
                while (!scenarioHandler.isScenarioFinished()) {
                    scenarioHandler.wait();
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return false;
        }
        if (scenarioHandler.isFinishedOK()) {
            LOG.info("Device {} finished scenario OK", this.name);
        } else {
            LOG.error("Device {} finished scenario with error", this.name);
        }
        return scenarioHandler.isFinishedOK();

    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }
}
