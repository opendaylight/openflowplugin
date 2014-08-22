/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.msg.MeterBandType;
import org.opendaylight.of.lib.msg.MeterFlag;

import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;
import static org.opendaylight.util.PrimitiveUtils.verifyU8;

/**
 * Mutable subclass of {@link MBodyMeterFeatures}.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class MBodyMutableMeterFeatures extends MBodyMeterFeatures
        implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body <em>MeterFeatures</em>type.
     *
     * @param pv the protocol version
     */
    public MBodyMutableMeterFeatures(ProtocolVersion pv) {
        super(pv);
        bandTypes = new TreeSet<MeterBandType>();
        capabilities = new TreeSet<MeterFlag>();
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        MBodyMeterFeatures meterFeatures = new MBodyMeterFeatures(version);
        meterFeatures.maxMeters = this.maxMeters;
        meterFeatures.bandTypes = this.bandTypes;
        meterFeatures.capabilities = this.capabilities;
        meterFeatures.maxBands = this.maxBands;
        meterFeatures.maxColor = this.maxColor;
        return meterFeatures;
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
     * Set the maximum number of meters supported; Since 1.3.
     * Note that this must be an unsigned 32-bit value.
     *
     * @param max the maximum number of meters
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if max is not u32
     */
    public MBodyMutableMeterFeatures maxMeters(long max) {
        mutt.checkWritable(this);
        verifyU32(max);
        maxMeters = max;
        return this;
    }

    /**
     * Sets the supported meter band types; Since 1.3.
     *
     * @param types the supported meter band types
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if types is null
     */
    public MBodyMutableMeterFeatures bandTypes(Set<MeterBandType> types) {
        mutt.checkWritable(this);
        notNull(types);
        bandTypes.clear();
        bandTypes.addAll(types);
        return this;
    }

    /**
     * Sets the supported meter capabilities; Since 1.3.
     *
     * @param caps the capabilities
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if caps is null
     */
    public MBodyMutableMeterFeatures capabilities(Set<MeterFlag> caps) {
        mutt.checkWritable(this);
        notNull(caps);
        capabilities.clear();
        capabilities.addAll(caps);
        return this;
    }

    /**
     * Sets the maximum number of supported bands per meter; Since 1.3.
     * Note that this must be an unsigned 8-bit value.
     *
     * @param max the maximum bands per meter
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if max is not u8
     */
    public MBodyMutableMeterFeatures maxBands(int max) {
        mutt.checkWritable(this);
        verifyU8(max);
        maxBands = max;
        return this;
    }

    /**
     * Set the maximum supported color value; Since 1.3.
     * Note that this must be an unsigned 8-bit value.
     *
     * @param max the maximum color value
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if max is not u8
     */
    public MBodyMutableMeterFeatures maxColor(int max) {
        mutt.checkWritable(this);
        verifyU8(max);
        maxColor = max;
        return this;
    }
}
