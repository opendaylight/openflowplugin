/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.VersionMismatchException;

import static org.opendaylight.of.lib.CommonUtils.verMin12;

/**
 * Mutable subclass of {@link OfmExperimenter}.
 *
 * @author Scott Simes
 */
public class OfmMutableExperimenter extends OfmExperimenter
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs an OpenFlow Experimenter message.
     *
     * @param header the message header
     */
    OfmMutableExperimenter(Header header) {
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
        OfmExperimenter msg = new OfmExperimenter(header);
        msg.id = this.id;
        msg.type = this.type;
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
     * Sets the experimenter identifier; Since 1.1.
     * Used to set the vendor identifier for 1.0.
     *
     * @param id the experimenter identifier
     * @return self, for chaining
     * @throws NullPointerException if id is null
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableExperimenter expId(ExperimenterId id) {
        expId(id.encodedId());
        return this;
    }
    /**
     * Sets the experimenter identifier; Since 1.1.
     * Used to set the vendor identifier for 1.0.
     *
     * @param encodedId the encoded experimenter identifier
     * @return self, for chaining
     * @throws NullPointerException if encodedId is null
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableExperimenter expId(int encodedId) {
        mutt.checkWritable(this);
        this.id = encodedId;
        return this;
    }

    /**
     * Sets the experimenter type; Since 1.2
     *
     * @param expType the experimenter type
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.2
     */
    public OfmMutableExperimenter expType(int expType) {
        mutt.checkWritable(this);
        verMin12(header.version);
        this.type = expType;
        return this;
    }

    /**
     * Sets the data; Since 1.0.
     *
     * @param data the data
     * @return self, for chaining
     * @throws NullPointerException if data is null
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableExperimenter data(byte[] data) {
        mutt.checkWritable(this);
        this.data = data.clone();
        header.length += data.length;
        return this;
    }
}
