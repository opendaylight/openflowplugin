/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.openflowjava.protocol.impl.clients;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;

import com.google.common.util.concurrent.SettableFuture;

/** Initializes udp pipeline
 *
 * @author michal.polkorab
 */
public class UdpSimpleClientInitializer extends ChannelInitializer<DatagramChannel> {

    private SettableFuture<Boolean> isOnlineFuture;
    private ScenarioHandler scenarioHandler;

    /**
     * @param isOnlineFuture future notifier of connected channel
     */
    public UdpSimpleClientInitializer(SettableFuture<Boolean> isOnlineFuture) {
        this.isOnlineFuture = isOnlineFuture;
    }

    @Override
    public void initChannel(DatagramChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        SimpleClientHandler simpleClientHandler = new SimpleClientHandler(isOnlineFuture, scenarioHandler);
        simpleClientHandler.setScenario(scenarioHandler);
        pipeline.addLast("framer", new UdpSimpleClientFramer());
        pipeline.addLast("handler", simpleClientHandler);
        isOnlineFuture = null;
    }

    /**
     * @param scenarioHandler handler of scenario events
     */
    public void setScenario(ScenarioHandler scenarioHandler) {
        this.scenarioHandler = scenarioHandler;
    }
}