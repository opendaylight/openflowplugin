/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import java.util.Set;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.statistics.CounterEventTypes;
import org.opendaylight.openflowjava.statistics.StatisticsCounters;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects version of used OpenFlow Protocol and discards unsupported version messages.
 *
 * @author michal.polkorab
 */
public class OFVersionDetector extends ByteToMessageDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(OFVersionDetector.class);
    /** IDs of accepted OpenFlow protocol versions. */
    private static final Set<Uint8> OF_VERSIONS = Set.of(
            EncodeConstants.OF_VERSION_1_0,
            EncodeConstants.OF_VERSION_1_3
    );
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

        final Uint8 version = readUint8(in);
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
