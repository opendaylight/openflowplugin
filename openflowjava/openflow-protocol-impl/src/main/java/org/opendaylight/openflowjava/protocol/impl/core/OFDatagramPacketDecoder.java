/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageConsumer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michal.polkorab
 *
 */
public class OFDatagramPacketDecoder extends SimpleChannelInboundHandler<VersionMessageUdpWrapper>{

    private static final Logger LOG = LoggerFactory.getLogger(OFDatagramPacketDecoder.class);
    private DeserializationFactory deserializationFactory;

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final VersionMessageUdpWrapper msg)
            throws Exception {
        if (LOG.isDebugEnabled()) {
                LOG.debug("UdpVersionMessageWrapper received");
                LOG.debug("<< {}", ByteBufUtils.byteBufToHexString(msg.getMessageBuffer()));
        }

        try {
            final DataObject dataObject = deserializationFactory.deserialize(msg.getMessageBuffer(),msg.getVersion());
            if (dataObject == null) {
                LOG.warn("Translated POJO is null");
            } else {
                MessageConsumer consumer = UdpConnectionMap.getMessageConsumer(msg.getAddress());
                consumer.consume(dataObject);
            }
        } catch(Exception e) {
            LOG.warn("Message deserialization failed", e);
            // TODO: delegate exception to allow easier deserialization
            // debugging / deserialization problem awareness
        } finally {
            msg.getMessageBuffer().release();
        }
    }

    /**
     * @param deserializationFactory
     */
    public void setDeserializationFactory(final DeserializationFactory deserializationFactory) {
        this.deserializationFactory = deserializationFactory;
    }
}
