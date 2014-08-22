/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Represents a meter band, used in {@link OfmMeterMod} messages; Since 1.3.
 *
 * @author Simon Hunt
 */
public abstract class MeterBand extends OpenflowStructure {
    Header header;

    /** Constructor invoked by MeterFactory.
     *
     * @param pv the protocol version
     * @param header the meter band header
     */
    MeterBand(ProtocolVersion pv, Header header) {
        super(pv);
        this.header = header;
    }

    @Override
    public String toString() {
        return "{MBand:" + version.name() + ":" + header.type +
                ",rate=" + header.rate +
                ",burst=" + header.burstSize +
                "}";
        // TODO: add formatters for rate and burst size and add to toString()
    }

    /** Returns the meter band type.
     *
      * @return the band type
     */
    public MeterBandType getType() {
        return header.type;
    }

    /* Implementation note:
    *   we don't expose the length field, since that is an implementation
    *   detail that the consumer should not care about.
    */

    /** Returns the rate for this band.
     *
     * @return the rate
     */
    public long getRate() {
        return header.rate;
    }

    /** Returns the burst size for this band.
     *
     * @return the size of bursts
     */
    public long getBurstSize() {
        return header.burstSize;
    }

    /** Meter band header. */
    static class Header {
        /** Type of meter band. */
        MeterBandType type;
        /** Length of band (when encoded as byte array). */
        int length;
        /** Rate for this band. */
        long rate;
        /** Size of bursts. */
        long burstSize;
    }
}
