/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects idle state of switch and informs upper layers
 * @author michal.polkorab
 */
public class IdleHandler extends ReadTimeoutHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IdleHandler.class);
    private boolean first = true;

    /**
     * @param readerIdleTime
     * @param unit
     */
    public IdleHandler(final long readerIdleTime, final TimeUnit unit) {
        super(readerIdleTime, unit);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        super.channelRead(ctx, msg);
        first = true;
    }

    @Override
    protected void readTimedOut(final ChannelHandlerContext ctx) throws Exception {
        if (first) {
            LOG.debug("Switch idle");
            SwitchIdleEventBuilder builder = new SwitchIdleEventBuilder();
            builder.setInfo("Switch idle");
            ctx.fireChannelRead(builder.build());
            first = false;
        }
    }
}
