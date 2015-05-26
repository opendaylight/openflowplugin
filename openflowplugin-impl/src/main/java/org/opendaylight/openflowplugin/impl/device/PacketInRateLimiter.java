/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PacketInRateLimiter extends SimpleRatelimiter {
    private static final Logger LOG = LoggerFactory.getLogger(PacketInRateLimiter.class);
    private final float rejectedDrainFactor;
    private final ConnectionAdapter connectionAdapter;
    private final MessageSpy messageSpy;

    PacketInRateLimiter(final ConnectionAdapter connectionAdapter, final int lowWatermark, final int highWatermark, final MessageSpy messageSpy, float rejectedDrainFactor) {
        super(lowWatermark, highWatermark);
        Preconditions.checkArgument(rejectedDrainFactor > 0 && rejectedDrainFactor < 1);
        this.rejectedDrainFactor = rejectedDrainFactor;
        this.connectionAdapter = Preconditions.checkNotNull(connectionAdapter);
        this.messageSpy = Preconditions.checkNotNull(messageSpy);
    }

    @Override
    protected void disableFlow() {
        messageSpy.spyMessage(DeviceContext.class, MessageSpy.STATISTIC_GROUP.OFJ_BACKPRESSURE_ON);
        connectionAdapter.setPacketInFiltering(true);
        LOG.debug("PacketIn filtering on: {}", connectionAdapter.getRemoteAddress());
    }

    @Override
    protected void enableFlow() {
        messageSpy.spyMessage(DeviceContext.class, MessageSpy.STATISTIC_GROUP.OFJ_BACKPRESSURE_OFF);
        connectionAdapter.setPacketInFiltering(false);
        LOG.debug("PacketIn filtering off: {}", connectionAdapter.getRemoteAddress());
    }

    public void drainLowWaterMark() {
        adaptLowWaterMarkAndDisableFlow((int) (getOccupiedPermits() * rejectedDrainFactor));
    }
}
