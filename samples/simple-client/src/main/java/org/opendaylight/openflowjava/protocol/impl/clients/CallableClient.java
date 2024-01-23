/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.clients;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetAddress;
import java.util.concurrent.Callable;
import org.slf4j.LoggerFactory;

/**
 * Callable client class, inspired by SimpleClient class.
 * Simulating device/switch connected to controller.
 *
 * @author Jozef Bacigal
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
        this.port = port;
        this.securedClient = securedClient;
        this.ipAddress = requireNonNull(ipAddress, "IP address cannot be null");
        this.workerGroup = eventExecutors;
        this.bootstrap = bootstrap;
        this.name = name;
        this.scenarioHandler = requireNonNull(scenarioHandler, "Scenario handler cannot be null");
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
    @SuppressWarnings("checkstyle:IllegalCatch")
    public Boolean call() throws Exception {
        requireNonNull(bootstrap);
        requireNonNull(workerGroup);
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
        } catch (RuntimeException ex) {
            LOG.error("Error", ex);
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
