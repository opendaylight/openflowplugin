/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.util.net.BigPortNumber;

import java.util.ArrayList;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.verMin12;

/**
 * Mutable subclass of {@link Queue}.
 *
 * @author Simon Hunt
 */
public class MutableQueue extends Queue implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable queue structure.
     *
     * @param pv the protocol version
     */
    MutableQueue(ProtocolVersion pv) {
        super(pv);
        this.length = pv.ge(ProtocolVersion.V_1_2)
                ? QueueFactory.PV_23_Q_HEAD_LEN
                : QueueFactory.PV_01_Q_HEAD_LEN;
        this.props = new ArrayList<QueueProperty>();
    }


    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        Queue queue = new Queue(this.version);
        queue.queueId = this.queueId;
        queue.port = this.port;
        queue.length = this.length;
        queue.props = this.props;
        return queue;
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

    /** Sets the id of this queue; Since 1.0.
     *
     * @param id the queue id
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if id is null
     */
    public MutableQueue id(QueueId id) {
        mutt.checkWritable(this);
        notNull(id);
        this.queueId = id;
        return this;
    }

    /** Sets the port number for this queue; Since 1.2.
     *
     * @param port the port number
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.2
     * @throws NullPointerException if port is null
     */
    public MutableQueue port(BigPortNumber port) {
        mutt.checkWritable(this);
        verMin12(version);
        notNull(port);
        this.port = port;
        return this;
    }

    /** Adds a property to this queue; Since 1.0.
     *
     * @param property the property to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if property is null
     * @throws VersionMismatchException if property is not supported for the
     *          given version
     */
    public MutableQueue addProperty(QueueProperty property) {
        mutt.checkWritable(this);
        notNull(property);
        validateProp(property);
        props.add(property);
        length += property.header.length;
        return this;
    }

    // can we add the given property to this queue?
    private void validateProp(QueueProperty prop) {
        // TODO: review - can we have more than one of a given type of prop?
        switch (prop.getType()) {
            case MIN_RATE:
                break;
            case MAX_RATE:
                verMin12(version);
                break;
            case EXPERIMENTER:
                verMin12(version);
                break;
        }
    }
}
