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


/**
 * Mutable subclass of {@link OfmEchoReply}.
 *
 * @author Scott Simes
 */
public class OfmMutableEchoReply extends OfmEchoReply
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow ECHO_REPLY message.
     *
     * @param header the message header
     */
    OfmMutableEchoReply(Header header) {
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
        OfmEchoReply msg = new OfmEchoReply(header);
        msg.data = this.data;
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

    /**
     * Sets the data; Since 1.0.
     *
     * @param data the data
     * @return self, for chaining
     * @throws NullPointerException if data is null
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableEchoReply data(byte[] data) {
        mutt.checkWritable(this);
        this.data = data.clone();
        header.length += data.length;
        return this;
    }
}
