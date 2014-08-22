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

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.verMin12;

/**
 * Mutable subclass of {@link OfmRoleRequest}.
 *
 * @author Pramod Shanbhag
 */
public class OfmMutableRoleRequest extends OfmRoleRequest 
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow ROLE_REQUEST message.
     *
     * @param header the message header
     */
    OfmMutableRoleRequest(Header header) {
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
        OfmRoleRequest msg = new OfmRoleRequest(header);
        msg.role = this.role;
        msg.generationId = this.generationId;
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
    
    /** Sets the controller role; Since 1.2.
     *
     * @param role the controller role
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if role is null
     * @throws VersionMismatchException if version &lt; 1.2.
     */
    public OfmMutableRoleRequest role(ControllerRole role) {
        mutt.checkWritable(this);
        verMin12(header.version);
        notNull(role);
        this.role = role;
        return this;
    }

    /** Sets the generation ID; Since 1.2.
     *
     * @param generationId the generation ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version &lt; 1.2.
     */
    public OfmMutableRoleRequest generationId(long generationId) {
        mutt.checkWritable(this);
        verMin12(header.version);
        this.generationId = generationId;
        return this;
    }
}
