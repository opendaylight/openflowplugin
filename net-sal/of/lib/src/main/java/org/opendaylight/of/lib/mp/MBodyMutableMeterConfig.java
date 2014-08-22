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
import org.opendaylight.of.lib.msg.MeterBand;
import org.opendaylight.of.lib.msg.MeterBandFactory;
import org.opendaylight.of.lib.msg.MeterFlag;

import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.verMin13;

/**
 * Mutable subclass of {@link MBodyMeterConfig}.
 *
 * @author Scott Simes
 */
public class MBodyMutableMeterConfig extends MBodyMeterConfig
        implements MutableStructure {

    private static final int FIXED_LENGTH = 8;

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body METER_CONFIG element.
     * <p>
     * Note that a freshly constructed instance has {@link MeterId#ALL} as the
     * default meter id value, and initialized with an empty set of
     * {@link MeterFlag}.
     *
     * @param pv the protocol version
     */
    public MBodyMutableMeterConfig(ProtocolVersion pv) {
        super(pv);
        flags = new TreeSet<MeterFlag>();
        meterId = MeterId.ALL;
        length = FIXED_LENGTH;
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyMeterConfig meterConfig = new MBodyMeterConfig(version);
        meterConfig.length = this.length;
        meterConfig.meterId = this.meterId;
        meterConfig.flags = this.flags;
        meterConfig.bands = this.bands;
        return meterConfig;
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
     * Sets the meter ID for this configuration; Since 1.3.
     *
     * @param meterId the meter ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.3
     * @throws NullPointerException if meterId is null
     */
    public MBodyMutableMeterConfig meterId(MeterId meterId) {
        mutt.checkWritable(this);
        verMin13(version);
        notNull(meterId);
        this.meterId = meterId;
        return this;
    }

    /**
     * Set the meter flags; Since 1.3.
     *
     * @param flags the meter flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.3
     * @throws NullPointerException if flags is null
     */
    public MBodyMutableMeterConfig meterFlags(Set<MeterFlag> flags) {
        mutt.checkWritable(this);
        verMin13(version);
        notNull(flags);
        this.flags.clear();
        this.flags.addAll(flags);
        return this;
    }

    /**
     * Adds a meter band to this meter config; Since 1.3.
     *
     * @param band the meter band to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.3
     * @throws NullPointerException if band is null
     */
    public MBodyMutableMeterConfig addBand(MeterBand band) {
        mutt.checkWritable(this);
        verMin13(version);
        notNull(band);
        bands.add(band);
        this.length += MeterBandFactory.getLength(band);
        return this;
    }
}
