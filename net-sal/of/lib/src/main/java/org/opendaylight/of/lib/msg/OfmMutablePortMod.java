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
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Mutable subclass of {@link OfmPortMod}.
 *
 * @author Simon Hunt
 */
public class OfmMutablePortMod extends OfmPortMod implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow PORT_MOD message.
     *
     * @param header the message header
     */
    OfmMutablePortMod(Header header) {
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
        OfmPortMod msg = new OfmPortMod(header);
        msg.port = this.port;
        msg.hwAddress = this.hwAddress;
        msg.config = this.config;
        msg.mask = this.mask;
        msg.advertise = this.advertise;
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

    /** Sets the id of the port to be modified; Since 1.0.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @param port the port number
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if port is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    public OfmMutablePortMod port(BigPortNumber port) {
        mutt.checkWritable(this);
        notNull(port);
        Port.validatePortValue(port, header.version);
        this.port = port;
        return this;
    }

    /** Sets the hardware address to sanity check against; Since 1.0.
     * <p>
     * The hardware address is not configurable. This is used to sanity-check
     * the request, so it must be the same as returned in a
     * {@link Port} structure.
     *
     * @param hwAddress the hardware address
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if port is null
     */
    public OfmMutablePortMod hwAddress(MacAddress hwAddress) {
        mutt.checkWritable(this);
        notNull(hwAddress);
        this.hwAddress = hwAddress;
        return this;
    }

    /** Sets the configuration flags to apply; Since 1.0.
     *
     * @param flags the configuration flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if flags is null
     */
    public OfmMutablePortMod config(Set<PortConfig> flags) {
        mutt.checkWritable(this);
        notNull(flags);
        // TODO: Review - some validation on the flags?
        config = new TreeSet<PortConfig>(flags);
        return this;
    }

    /** Sets the configuration flags mask to apply; Since 1.0.
     * The flags in the mask select which bits in the config field to change.
     * May be null.
     *
     * @param mask the configuration flags mask
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutablePortMod configMask(Set<PortConfig> mask) {
        mutt.checkWritable(this);
        this.mask = mask == null ? null : new TreeSet<PortConfig>(mask);
        return this;
    }

    /** Sets the features of this port to advertise; Since 1.0.
     * May be null.
     *
     * @param features the port features to advertise
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutablePortMod advertise(Set<PortFeature> features) {
        mutt.checkWritable(this);
        advertise = features == null ? null
                                     : new TreeSet<PortFeature>(features);
        return this;
    }
}
