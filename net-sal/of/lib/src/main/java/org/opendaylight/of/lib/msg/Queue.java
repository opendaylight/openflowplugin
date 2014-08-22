/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.util.net.BigPortNumber;

import java.util.Collections;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.notNullIncompleteStruct;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;

/**
 * Represents a queue that can attach to a port.
 * <p>
 * An OpenFlow switch provides limited QoS support through a simple
 * queuing mechanism. One (or more) queues can attach to a port and be used
 * to map flow entries on it. Flow entries mapped to a specific queue will
 * be treated according to that queue's configuration (e.g. min rate).
 * <p>
 * Instances of this class are immutable.
 *
 * @author Simon Hunt
 */
public class Queue extends OpenflowStructure {
    QueueId queueId;
    BigPortNumber port;
    int length;
    List<QueueProperty> props;

    /**
     * Constructs an OpenFlow queue structure.
     *
     * @param pv the protocol version
     */
    Queue(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{queue(").append(version.name())
                .append("):").append(queueId);
        if (version.ge(ProtocolVersion.V_1_2))
            sb.append(",port=").append(Port.portNumberToString(port));
        sb.append(",props=").append(props).append("}");
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteStructureException {
        if (version.le(V_1_1))
            notNullIncompleteStruct(queueId);
        else
            notNullIncompleteStruct(queueId, port);
    }

    /** Returns the ID of this queue.
     *
     * @return the queue ID
     */
    public QueueId getId() {
        return queueId;
    }

    /* Implementation note:
     *   we don't expose the length field, since that is an implementation
     *   detail that the consumer should not care about.
     */

    /** Returns the ID of the port that this queue is attached to.
     * <p>
     * Protocol versions 1.0 and 1.1 will return null.
     *
     * @return returns the port
     */
    public BigPortNumber getPort() {
        return port;
    }

    /** Returns an unmodifiable view of the properties of this queue.
     *
     * @return the properties of this queue.
     */
    public List<QueueProperty> getProps() {
        return props == null ? null : Collections.unmodifiableList(props);
    }
}
