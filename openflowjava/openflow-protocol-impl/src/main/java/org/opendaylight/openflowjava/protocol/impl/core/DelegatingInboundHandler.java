/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.opendaylight.openflowjava.protocol.impl.core.connection.ConnectionAdapterImpl;
import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageConsumer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEventBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds reference to {@link ConnectionAdapterImpl} and passes messages for further processing.
 * Also informs on switch disconnection.
 * @author michal.polkorab
 */
public class DelegatingInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(DelegatingInboundHandler.class);
    private final MessageConsumer consumer;
    private boolean inactiveMessageSent = false;

    /**
     * Constructs class + creates and sets MessageConsumer.
     * @param connectionAdapter reference for adapter communicating with upper layers outside library
     */
    public DelegatingInboundHandler(final MessageConsumer connectionAdapter) {
        LOG.trace("Creating DelegatingInboundHandler");
        consumer = Preconditions.checkNotNull(connectionAdapter);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        consumer.consume((DataObject) msg);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        LOG.debug("Channel inactive");
        if (!inactiveMessageSent) {
            DisconnectEventBuilder builder = new DisconnectEventBuilder();
            builder.setInfo("Channel inactive");
            consumer.consume(builder.build());
            inactiveMessageSent = true;
        }
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) {
        LOG.debug("Channel unregistered");
        if (!inactiveMessageSent) {
            DisconnectEventBuilder builder = new DisconnectEventBuilder();
            builder.setInfo("Channel unregistered");
            consumer.consume(builder.build());
            inactiveMessageSent = true;
        }
    }

}
