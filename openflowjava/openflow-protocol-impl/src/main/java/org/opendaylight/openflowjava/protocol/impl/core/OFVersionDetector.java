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
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.statistics.CounterEventTypes;
import org.opendaylight.openflowjava.statistics.StatisticsCounters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects version of used OpenFlow Protocol and discards unsupported version messages.
 * @author michal.polkorab
 */
public class OFVersionDetector extends ByteToMessageDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(OFVersionDetector.class);
    /** IDs of accepted OpenFlow protocol versions */
    private static final List<Byte> OF_VERSIONS = new ArrayList<>(Arrays.asList(
            EncodeConstants.OF10_VERSION_ID,
            EncodeConstants.OF13_VERSION_ID
    ));
    private final StatisticsCounters statisticsCounters;
    private volatile boolean filterPacketIns;

    public OFVersionDetector() {
        LOG.trace("Creating OFVersionDetector");
        statisticsCounters = StatisticsCounters.getInstance();
    }

    public void setFilterPacketIns(final boolean enabled) {
        filterPacketIns = enabled;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        if (!in.isReadable()) {
            LOG.debug("not enough data");
            in.release();
            return;
        }

        final byte version = in.readByte();
        final short messageType = in.getUnsignedByte(in.readerIndex());
        if (messageType == EncodeConstants.OF_HELLO_MESSAGE_TYPE_VALUE || OF_VERSIONS.contains(version)) {
            LOG.debug("detected version: {}", version);
            if (!filterPacketIns || EncodeConstants.OF_PACKETIN_MESSAGE_TYPE_VALUE != messageType) {
                ByteBuf messageBuffer = in.slice();
                out.add(new VersionMessageWrapper(version, messageBuffer));
                messageBuffer.retain();
            } else {
                LOG.debug("dropped packetin");
                statisticsCounters.incrementCounter(CounterEventTypes.US_DROPPED_PACKET_IN);
            }
        } else {
            LOG.warn("detected version: {} - currently not supported", version);
        }
        in.skipBytes(in.readableBytes());
    }

}
