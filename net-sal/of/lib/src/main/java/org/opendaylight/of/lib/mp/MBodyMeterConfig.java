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
import org.opendaylight.of.lib.msg.MeterFlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.*;

/**
 * Represents a meter config element; part of a reply to a meter configuration
 * request multipart message; Since 1.3.
 *
 * @author Scott Simes
 */
public class MBodyMeterConfig extends OpenflowStructure
        implements MultipartBody {

    int length;
    Set<MeterFlag> flags;
    MeterId meterId;
    List<MeterBand> bands = new ArrayList<MeterBand>();

    /**
     * Constructs a multipart body METER_CONFIG type.
     *
     * @param pv the protocol version
     */
    public MBodyMeterConfig(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public int getTotalLength() {
        return length;
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(meterId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",id=").append(meterId).append(",flg=")
                .append(flags).append(",#b=").append(cSize(bands))
                .append(",...}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        sb.append(EOLI).append(EOLI).append("Flags: ").append(flags).
                append(EOLI).append("MeterID: ").append(meterId).append(EOLI)
                .append("Bands:");
        for (MeterBand mb: bands)
            sb.append(EOLI).append("  ").append(mb);

        return sb.toString();
    }

    // ============================================== Getters ============

    /**
     * Returns the meter id; Since 1.3.
     *
     * @return the meter id
     */
    public MeterId getMeterId() {
        return meterId;
    }

    /**
     * Returns the meter flags; Since 1.3.
     *
     * @return the flags
     */
    public Set<MeterFlag> getFlags() {
        return flags == null ? null : Collections.unmodifiableSet(flags);
    }

    /**
     * Returns the bands applied to this meter; Since 1.3.
     *
     * @return the bands
     */
    public List<MeterBand> getBands() {
        return Collections.unmodifiableList(bands);
    }

    //=======================================================================
    // Exo-Skeletal Arrays

    private static final String LINE_PREFIX = EOLI + "-- Meter Cfg ";
    private static final String LINE = " ---------------- ";

    /** Represents an array of meter config elements. */
    public static class Array extends MBodyList<MBodyMeterConfig> {

        /**
         * Constructor, initializing the internal list.
         *
         * @param pv protocol version
         */
        Array(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public Class<MBodyMeterConfig> getElementClass() {
            return MBodyMeterConfig.class;
        }

        @Override
        public String toString() {
            return "{MeterCfgs: count=" + cSize(list) + "}";
        }

        @Override
        public String toDebugString() {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (MBodyMeterConfig mcs: list)
                sb.append(LINE_PREFIX).append(index++).append(LINE)
                        .append(mcs.toDebugString());
            return sb.toString();
        }
    }

    /** A mutable array of meter config elements. */
    public static class MutableArray extends Array implements MutableStructure {

        private final Mutable mutt = new Mutable();

        /**
         * Constructor, initializing the internal list.
         *
         * @param pv protocol version
         */
        MutableArray(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public OpenflowStructure toImmutable() {
            // Can only do this once
            mutt.invalidate(this);
            // Transfer the payload to an immutable instance
            MBodyMeterConfig.Array array = new Array(version);
            // copy elements across
            array.addAll(this.list);
            return array;
        }

        @Override
        public boolean writable() {
            return mutt.writable();
        }

        @Override
        public String toString() {
            return mutt.tagString(super.toString());
        }

        // =================================================================
        // ==== ADDERS

        /**
         * Adds a meter config object to this mutable array.
         *
         * @param meterConfig the meter config object to add
         * @return self, for chaining
         * @throws InvalidMutableException if this array is no longer writable
         * @throws NullPointerException if meterConfig is null
         * @throws IncompleteStructureException if the meter config
         *                                      is incomplete
         */
        public MutableArray addMeterConfigs(MBodyMeterConfig meterConfig)
                throws IncompleteStructureException {
            mutt.checkWritable(this);
            notNull(meterConfig);
            notMutable((OpenflowStructure) meterConfig);
            meterConfig.validate();
            list.add(meterConfig);
            return this;
        }
    }
}
