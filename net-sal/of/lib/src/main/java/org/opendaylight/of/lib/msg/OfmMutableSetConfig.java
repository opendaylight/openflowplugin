/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;

import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.util.PrimitiveUtils.verifyU16;

/**
 * Mutable subclass of {@link OfmSetConfig}.
 *
 * @author Simon Hunt
 */
public class OfmMutableSetConfig extends OfmSetConfig
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow SET_CONFIG message.
     *
     * @param header the message header
     */
    OfmMutableSetConfig(Header header) {
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
        OfmSetConfig msg = new OfmSetConfig(header);
        msg.flags = this.flags;
        msg.missSendLength = this.missSendLength;
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

    /** Sets the set of configuration flags (may be null).
     *
     * @param flags the config flags
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public void setConfigFlags(Set<ConfigFlag> flags) {
        mutt.checkWritable(this);
        if (flags == null) {
            this.flags = null;
        } else {
            this.flags = new TreeSet<ConfigFlag>();
            this.flags.addAll(flags);
        }
    }

    /** Sets the miss-send-length (u16).
     *
     * @param missSendLength the miss send length
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if missSendLength is not u16
     */
    public void setMissSendLength(int missSendLength) {
        mutt.checkWritable(this);
        verifyU16(missSendLength);
        this.missSendLength = missSendLength;
    }

}
