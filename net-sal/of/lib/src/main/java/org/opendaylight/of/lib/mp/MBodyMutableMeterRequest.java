/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.MeterId;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.verMin13;

/**
 * Base class for mutable subclass of {@link MBodyMeterRequest}.
 * <p>
 * Used by the {@link MBodyMutableMeterStatsRequest}.
 *
 * @author Scott Simes
 */
public abstract class MBodyMutableMeterRequest extends MBodyMeterRequest
        implements MutableStructure {
    final Mutable mutt = new Mutable();

    /**
     * Constructs a multipart body METER type request.
     * <p>
     * Note that a freshly constructed instance has {@link MeterId#ALL} as the
     * default meter id value.
     *
     * @param pv the protocol version
     */
    MBodyMutableMeterRequest(ProtocolVersion pv) {
        super(pv);
        meterId = MeterId.ALL;
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
     * Sets the meter ID; Since 1.3.
     *
     * @param meterId the meter ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.3
     * @throws NullPointerException if meterId is null
     */
    public MBodyMutableMeterRequest meterId(MeterId meterId) {
        mutt.checkWritable(this);
        verMin13(version);
        notNull(meterId);
        this.meterId = meterId;
        return this;
    }
}
