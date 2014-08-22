/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.Mutable;

/**
 * Mutable subclass of {@link OfmBarrierRequest}.
 *
 * @author Shruthy Mohanram
 */
public class OfmMutableBarrierRequest extends OfmBarrierRequest
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /** Constructs a mutable OpenFlow BARRIER_REQUEST message.
    *
    * @param header the header
    */
    OfmMutableBarrierRequest(Header header) {
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
        return new OfmBarrierRequest(header);
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }
}
