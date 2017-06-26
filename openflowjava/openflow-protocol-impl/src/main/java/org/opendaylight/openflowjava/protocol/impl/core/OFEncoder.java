/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageListenerWrapper;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.openflowjava.statistics.CounterEventTypes;
import org.opendaylight.openflowjava.statistics.StatisticsCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms OpenFlow Protocol messages to POJOs.
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class OFEncoder extends MessageToByteEncoder<MessageListenerWrapper> {

    private static final Logger LOG = LoggerFactory.getLogger(OFEncoder.class);
    private SerializationFactory serializationFactory;
    private final StatisticsCounters statisticsCounters;

    /** Constructor of class */
    public OFEncoder() {
        statisticsCounters = StatisticsCounters.getInstance();
        LOG.trace("Creating OFEncoder");
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final MessageListenerWrapper wrapper, final ByteBuf out)
            throws Exception {
        LOG.trace("Encoding");
        try {
            serializationFactory.messageToBuffer(wrapper.getMsg().getVersion(), out, wrapper.getMsg());
            if(wrapper.getMsg() instanceof FlowModInput){
                statisticsCounters.incrementCounter(CounterEventTypes.DS_FLOW_MODS_SENT);
            }
            statisticsCounters.incrementCounter(CounterEventTypes.DS_ENCODE_SUCCESS);
        } catch(final Exception e) {
            LOG.warn("Message serialization failed ", e);
            statisticsCounters.incrementCounter(CounterEventTypes.DS_ENCODE_FAIL);
            if (wrapper.getListener() != null) {
                final Future<Void> newFailedFuture = ctx.newFailedFuture(e);
                wrapper.getListener().operationComplete(newFailedFuture);
            }
            out.clear();
            return;
        }
    }

    /**
     * @param serializationFactory
     */
    public void setSerializationFactory(final SerializationFactory serializationFactory) {
        this.serializationFactory = serializationFactory;
    }

}
