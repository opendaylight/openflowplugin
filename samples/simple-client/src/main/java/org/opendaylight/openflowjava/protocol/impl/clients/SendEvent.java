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

import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing sending message event
 *
 * @author michal.polkorab
 */
public class SendEvent implements ClientEvent {

    private static final Logger LOG = LoggerFactory.getLogger(SendEvent.class);
    protected byte[] msgToSend;
    protected ChannelHandlerContext ctx;

    /**
     * @param msgToSend message to be sent
     */
    public SendEvent(byte[] msgToSend) {
        this.msgToSend = new byte[msgToSend.length];
        System.arraycopy(msgToSend, 0, this.msgToSend, 0, msgToSend.length);
    }

    @Override
    public boolean eventExecuted() {
        LOG.debug("sending message");
        LOG.debug("start of run");
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(msgToSend);
        ctx.writeAndFlush(buffer);

        if (LOG.isDebugEnabled()) {
            LOG.debug(">> {}", ByteBufUtils.bytesToHexString(msgToSend));
            LOG.debug("message sent");
        }
        return true;
    }

    /**
     * @param ctx context which will be used for sending messages (SendEvents)
     */
    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

}
