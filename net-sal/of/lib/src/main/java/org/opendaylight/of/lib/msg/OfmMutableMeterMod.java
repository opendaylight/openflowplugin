/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.MeterId;

import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.verMin13;

/**
 * Mutable subclass of {@link OfmMeterMod}.
 *
 * @author Simon Hunt
 */
public class OfmMutableMeterMod extends OfmMeterMod implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow METER_MOD message.
     *
     * @param header the message header
     */
    OfmMutableMeterMod(Header header) {
        super(header);
    }

    @Override
    public void clearXid() {
        mutt.checkWritable(this);
        header.xid = 0;
    }

    @Override
    public OpenflowMessage toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Copy over to read-only instance
        OfmMeterMod msg = new OfmMeterMod(header);
        msg.command = this.command;
        msg.flags = this.flags;
        msg.meterId = this.meterId;
        msg.bands = this.bands;
        return msg;
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

    /** Sets the meter mod command; Since 1.3.
     *
     * @param cmd the meter-mod command
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.3
     * @throws NullPointerException if cmd is null
     */
    public OfmMutableMeterMod command(MeterModCommand cmd) {
        mutt.checkWritable(this);
        verMin13(header.version);
        notNull(cmd);
        this.command = cmd;
        return this;
    }

    /** Sets the meter flags; Since 1.3.
     *
     * @param flags the meter-mod flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.3
     * @throws NullPointerException if flags is null
     */
    public OfmMutableMeterMod meterFlags(Set<MeterFlag> flags) {
        mutt.checkWritable(this);
        verMin13(header.version);
        notNull(flags);
        this.flags = new TreeSet<MeterFlag>(flags);
        // TODO: Review - validation of flags combinations?
        return this;
    }

    /** Sets the meter id; Since 1.3.
     *
     * @param meterId the meter id
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.3
     * @throws NullPointerException if meterId is null
     */
    public OfmMutableMeterMod meterId(MeterId meterId) {
        mutt.checkWritable(this);
        verMin13(header.version);
        notNull(meterId);
        this.meterId = meterId;
        return this;
    }

    /** Adds a meter band to this meter-mod message; Since 1.3.
     *
     * @param band the meter band to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.3
     * @throws NullPointerException if band is null
     */
    public OfmMutableMeterMod addBand(MeterBand band) {
        mutt.checkWritable(this);
        verMin13(header.version);
        notNull(band);
        // TODO: Review - some validation required?
        bands.add(band);
        header.length += band.header.length;
        return this;
    }
}
