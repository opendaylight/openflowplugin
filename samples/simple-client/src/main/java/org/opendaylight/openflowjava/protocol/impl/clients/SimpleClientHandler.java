/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.openflowjava.protocol.impl.clients;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

/**
 *
 * @author michal.polkorab
 */
public class SimpleClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleClientHandler.class);
    private static final int LENGTH_INDEX_IN_HEADER = 2;
    private SettableFuture<Boolean> isOnlineFuture;
    protected ScenarioHandler scenarioHandler;

    /**
     * @param isOnlineFuture future notifier of connected channel
     * @param scenarioHandler handler of scenario events
     */
    public SimpleClientHandler(SettableFuture<Boolean> isOnlineFuture, ScenarioHandler scenarioHandler) {
        this.isOnlineFuture = isOnlineFuture;
        this.scenarioHandler = scenarioHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf bb = (ByteBuf) msg;
        if (LOG.isDebugEnabled()) {
            LOG.debug("<< {}", ByteBufUtils.byteBufToHexString(bb));
        }
        int length = bb.getUnsignedShort(bb.readerIndex() + LENGTH_INDEX_IN_HEADER);
        LOG.trace("SimpleClientHandler - start of read");
        byte[] message = new byte[length];
        bb.readBytes(message);
        scenarioHandler.addOfMsg(message);
        LOG.trace("end of read");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Client is active");
        if (isOnlineFuture != null) {
            isOnlineFuture.set(true);
            isOnlineFuture = null;
        }
        scenarioHandler.setCtx(ctx);
        scenarioHandler.start();

    }

    /**
     * @param scenarioHandler handler of scenario events
     */
    public void setScenario(ScenarioHandler scenarioHandler) {
        this.scenarioHandler = scenarioHandler;
    }

}
