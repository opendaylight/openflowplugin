/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.MeterBandType;
import org.opendaylight.of.lib.msg.MeterFlag;
import org.opendaylight.util.StringUtils;

import java.util.Collections;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.EOLI;

/**
 * Represents a meter features element; part of a reply to a meter features
 * request multipart message; Since 1.3.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class MBodyMeterFeatures extends OpenflowStructure
        implements MultipartBody {
    private static final int TOTAL_LENGTH = 16;

    long maxMeters;
    Set<MeterBandType> bandTypes;
    Set<MeterFlag> capabilities;
    int maxBands;
    int maxColor;

    /**
     * Constructs a multipart body <em>MeterFeatures</em>type.
     *
     * @param pv the protocol version
     */
    public MBodyMeterFeatures(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public int getTotalLength() {
        return TOTAL_LENGTH;
    }

    @Override
    public String toString() {
        return "{mfeats:maxMeters=" + maxMeters + ",...}";
    }

    @Override
    public String toDebugString()  {
        return toDebugString(0);
    }

    /** Returns a multi-line representation of this Meter Features object.
     *
     * @param indent the additional indent (number of spaces)
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder(toString());
        sb.append(in).append("Max Meters : ").append(maxMeters)
                .append(in).append("Band Types : ").append(bandTypes)
                .append(in).append("Capabilities : ").append(capabilities)
                .append(in).append("Max Bands : ").append(maxBands)
                .append(in).append("Max Color : ").append(maxColor);
        return sb.toString();
    }

    //=========================================================== GETTERS

    /**
     * Returns the maximum number of meters; Since 1.3.
     *
     * @return the maximum meters
     */
    public long getMaxMeters() {
        return maxMeters;
    }

    /**
     * Returns the set of supported meter band types; Since 1.3.
     *
     * @return the meter band types
     */
    public Set<MeterBandType> getBandTypes() {
        return Collections.unmodifiableSet(bandTypes);
    }

    /**
     * Returns the set of meter capabilities; Since 1.3.
     *
     * @return the capabilities
     */
    public Set<MeterFlag> getCapabilities() {
        return Collections.unmodifiableSet(capabilities);
    }

    /**
     * Returns the maximum supported bands per meter; Since 1.3.
     *
     * @return the maximum bands per meter
     */
    public int getMaxBands() {
        return maxBands;
    }

    /**
     * Returns the maximum color value; Since 1.3.
     *
     * @return the maximum color value
     */
    public int getMaxColor() {
        return maxColor;
    }
}
