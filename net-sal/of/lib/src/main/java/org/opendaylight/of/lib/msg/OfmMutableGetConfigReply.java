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
 * Mutable subclass of {@link OfmGetConfigReply}.
 *
 * @author Simon Hunt
 */
public class OfmMutableGetConfigReply extends OfmGetConfigReply
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow GET_CONFIG_REPLY message.
     *
     * @param header the message header
     */
    OfmMutableGetConfigReply(Header header) {
        super(header);
        flags = new TreeSet<ConfigFlag>();
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
        OfmGetConfigReply msg = new OfmGetConfigReply(header);
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

    /** Sets the configuration flags (may be null); Since 1.0.
     *
     * @param flags the config flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableGetConfigReply configFlags(Set<ConfigFlag> flags) {
        mutt.checkWritable(this);
        if (flags == null) {
            this.flags.clear();
        } else {
            this.flags.clear();
            this.flags.addAll(flags);
        }
        return this;
    }

    /** Sets the miss-send-length (u16); Since 1.0.
     *
     * @param missSendLength the miss send length
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if missSendLength is not u16
     */
    public OfmMutableGetConfigReply missSendLength(int missSendLength) {
        mutt.checkWritable(this);
        verifyU16(missSendLength);
        this.missSendLength = missSendLength;
        return this;
    }
}
