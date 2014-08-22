/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;

import static org.opendaylight.of.lib.CommonUtils.notMutable;
import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Mutable subclass of {@link OfmPortStatus}.
 *
 * @author Radhika Hegde
 */
public class OfmMutablePortStatus extends OfmPortStatus
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /** Constructs a mutable OpenFlow PORT STATUS message.
    *
    * @param header the message header
    */
    OfmMutablePortStatus(Header header) {
        super(header);
    }

    @Override
    public void clearXid() {
        mutt.checkWritable(this);
        header.xid = 0;
    }

    @Override
    public OpenflowMessage toImmutable() {
        // Can do this once
        mutt.invalidate(this);
        // Copy over to read-only instance
        OfmPortStatus msg = new OfmPortStatus(header);
        msg.desc = this.desc;
        msg.reason = this.reason;
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

    // ========== SETTERS ==================

    /** Sets the reason for port status change; Since 1.0.
     *
     * @param reason for the status change
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if reason is null
     */
    public OfmMutablePortStatus reason(PortReason reason) {
        mutt.checkWritable(this);
        notNull(reason);
        this.reason = reason;
        return this;
    }

    /** Sets the port whose status changed; Since 1.0.
     *
     * @param port the physical port
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if port is null
     * @throws IllegalArgumentException if port is mutable
     * @throws IncompleteStructureException if port structure is incomplete
     */
    public OfmMutablePortStatus port(Port port)
            throws IncompleteStructureException {
        mutt.checkWritable(this);
        notNull(port);
        notMutable(port);
        port.validate();
        this.desc = port;
        return this;
    }
}
