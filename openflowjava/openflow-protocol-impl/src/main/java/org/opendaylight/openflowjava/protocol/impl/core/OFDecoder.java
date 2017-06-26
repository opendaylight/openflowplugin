/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.statistics.CounterEventTypes;
import org.opendaylight.openflowjava.statistics.StatisticsCounters;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms OpenFlow Protocol messages to POJOs.
 * @author michal.polkorab
 */
public class OFDecoder extends MessageToMessageDecoder<VersionMessageWrapper> {

    private static final Logger LOG = LoggerFactory.getLogger(OFDecoder.class);
    private final StatisticsCounters statisticsCounter;

    // TODO: make this final?
    private DeserializationFactory deserializationFactory;

    public OFDecoder() {
        LOG.trace("Creating OFDecoder");
	    // TODO: pass as argument
        statisticsCounter = StatisticsCounters.getInstance();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, VersionMessageWrapper msg, List<Object> out) throws Exception {
        statisticsCounter.incrementCounter(CounterEventTypes.US_RECEIVED_IN_OFJAVA);
        if (LOG.isDebugEnabled()) {
            LOG.debug("VersionMessageWrapper received");
            LOG.debug("<< {}", ByteBufUtils.byteBufToHexString(msg.getMessageBuffer()));
        }

        try {
            final DataObject dataObject = deserializationFactory.deserialize(msg.getMessageBuffer(),
                    msg.getVersion());
            if (dataObject == null) {
                LOG.warn("Translated POJO is null");
                statisticsCounter.incrementCounter(CounterEventTypes.US_DECODE_FAIL);
            } else {
                out.add(dataObject);
                statisticsCounter.incrementCounter(CounterEventTypes.US_DECODE_SUCCESS);
            }
        } catch (Exception e) {
            LOG.warn("Message deserialization failed", e);
            statisticsCounter.incrementCounter(CounterEventTypes.US_DECODE_FAIL);
        } finally {
            msg.getMessageBuffer().release();
        }
    }

    /**
     * @param deserializationFactory
     */
    public void setDeserializationFactory(DeserializationFactory deserializationFactory) {
        this.deserializationFactory = deserializationFactory;
    }

}
