/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Mutable subclass of {@link MBodyPortStatsRequest}.
 *
 * @author Pramod Shanbhag
 */
public class MBodyMutablePortStatsRequest extends MBodyPortStatsRequest
        implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body PORT_STATS request type.
     * <p>
     * Note that a freshly constructed instance has the following default
     * value:
     * <ul>
     *     <li>port : {@link Port#ANY}</li>
     * </ul>
     *
     * @param pv the protocol version
     */
    public MBodyMutablePortStatsRequest(ProtocolVersion pv) {
        super(pv);
        port = Port.ANY;
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyPortStatsRequest req = new MBodyPortStatsRequest(version);
        req.port = this.port;
        return req;
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

    /** Sets the port in the port stats request; Since 1.0.
     * A value of {@link Port#ANY} indicates stats requested for all ports.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @param port the requested port
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if port is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    public MBodyMutablePortStatsRequest port(BigPortNumber port) {
        mutt.checkWritable(this);
        notNull(port);
        Port.validatePortValue(port, version);
        this.port = port;
        return this;
    }
}
