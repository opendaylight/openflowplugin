/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.MeterId;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;

/**
 * Mutable subclass of {@link MBodyMeterStats}.
 *
 * @author Scott Simes
 */
public class MBodyMutableMeterStats extends MBodyMeterStats
        implements MutableStructure {

    private static final int FIXED_LENGTH = 40;

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body METER_STATS type.
     * <p>
     * Note the a freshly constructed instance has {@link MeterId#ALL} as the
     * default meter id value.
     *
     * @param pv the protocol version
     */
    public MBodyMutableMeterStats(ProtocolVersion pv) {
        super(pv);
        meterId = MeterId.ALL;
        length = FIXED_LENGTH;
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyMeterStats meterStats = new MBodyMeterStats(version);
        meterStats.meterId = this.meterId;
        meterStats.length = this.length;
        meterStats.flowCount = this.flowCount;
        meterStats.pktInCount = this.pktInCount;
        meterStats.byteInCount = this.byteInCount;
        meterStats.durationSec = this.durationSec;
        meterStats.durationNSec = this.durationNSec;
        meterStats.bandStats = this.bandStats;
        return meterStats;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }

    // =====================================================================
    // ==== SETTERS

    /**
     * Sets the meter ID for this meter stats; Since 1.3.
     *
     * @param meterId the meter ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if meterId is null
     */
    public MBodyMutableMeterStats meterId(MeterId meterId) {
        mutt.checkWritable(this);
        notNull(meterId);
        this.meterId = meterId;
        return this;
    }

    /**
     * Sets the count of flows bound to this meter; Since 1.3.
     *
     * @param flowCount the count of flows
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableMeterStats flowCount(long flowCount) {
        mutt.checkWritable(this);
        this.flowCount = flowCount;
        return this;
    }

    /**
     * Sets the number of packet in input for this meter; Since 1.3.
     *
     * @param packetInCount the packet in count
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableMeterStats packetInCount(long packetInCount) {
        mutt.checkWritable(this);
        this.pktInCount = packetInCount;
        return this;
    }

    /**
     * Sets the number of bytes in input for this meter; Since 1.3.
     *
     * @param byteInCount the packet in count
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableMeterStats byteInCount(long byteInCount) {
        mutt.checkWritable(this);
        this.byteInCount = byteInCount;
        return this;
    }

    /**
     * Sets the time this meter hes been alive; Since 1.3.
     * <p>
     * The first parameter is the number of seconds; the second is the
     * additional number of nanoseconds.
     *
     * @param seconds the number of seconds
     * @param nano the additional number of nanoseconds
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if seconds or nano is not U32
     */
    public MBodyMutableMeterStats duration(long seconds, long nano) {
        mutt.checkWritable(this);
        verifyU32(seconds);
        verifyU32(nano);
        this.durationSec = seconds;
        this.durationNSec = nano;
        return this;
    }

    /**
     * Adds a {@link MBodyMeterStats.MeterBandStats} for the meter bands
     * applied to this meter; Since 1.3.
     *
     * @param meterBandStats the meter band stats
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if meterBandStats is null
     */
    public MBodyMutableMeterStats addMeterBandStat(MeterBandStats meterBandStats) {
        mutt.checkWritable(this);
        notNull(meterBandStats);
        this.bandStats.add(meterBandStats);
        this.length += meterBandStats.getLength();
        return this;
    }
}
