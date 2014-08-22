/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;

import static org.opendaylight.of.lib.CommonUtils.verMin12;

/**
 * Mutable subclass of {@link MBodyExperimenter}.
 *
 * @author Scott Simes
 */
public class MBodyMutableExperimenter extends MBodyExperimenter
        implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body EXPERIMENTER extension type.
     *
     * @param pv the protocol version
     */
    public MBodyMutableExperimenter(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyExperimenter experimenter = new MBodyExperimenter(version);
        experimenter.id = this.id;
        experimenter.type = this.type;
        experimenter.data = this.data;
        return experimenter;
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
     * Sets the experimenter ID; Since 1.0.
     * Renamed from vendor ID at 1.1.
     *
     * @param id the experimenter ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if id is null
     */
    public MBodyMutableExperimenter expId(ExperimenterId id) {
        expId(id.encodedId());
        return this;
    }
    /**
     * Sets the experimenter ID; Since 1.0.
     * Renamed from vendor ID at 1.1.
     *
     * @param encodedId the encoded experimenter ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableExperimenter expId(int encodedId) {
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
    public MBodyMutableExperimenter expType(int expType) {
        mutt.checkWritable(this);
        verMin12(version);
        this.type = expType;
        return this;
    }

    /**
     * Sets the experimenter-defined data; Since 1.0.
     *
     * @param data the data
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if data is null
     */
    public MBodyMutableExperimenter data(byte[] data) {
        mutt.checkWritable(this);
        this.data = data.clone();
        return this;
    }
}
