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

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Mutable subclass of {@link OfmQueueGetConfigRequest}.
 *
 * @author Scott Simes
 */
public class OfmMutableQueueGetConfigRequest extends OfmQueueGetConfigRequest
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow QUEUE_GET_CONFIG_REQUEST message.
     *
     * @param header the message header
     */
    OfmMutableQueueGetConfigRequest(Header header) {
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
        OfmQueueGetConfigRequest msg = new OfmQueueGetConfigRequest(header);
        msg.port = this.port;
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
     * Sets the ID of the port to be queried; Since 1.0.
     * <p>
     * Note that in 1.0, port number is u16.
     *
     * @param port the port number
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if port is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    public void setPort(BigPortNumber port) {
        mutt.checkWritable(this);
        notNull(port);
        Port.validatePortValue(port, header.version);
        this.port = port;
    }
}
