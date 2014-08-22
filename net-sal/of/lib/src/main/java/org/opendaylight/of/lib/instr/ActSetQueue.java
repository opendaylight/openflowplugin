/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.util.net.BigPortNumber;

/**
 * Flow action {@code SET_QUEUE}.
 *
 * @author Simon Hunt
 */
public class ActSetQueue extends Action {

    BigPortNumber port;
    QueueId queueId;

    /**
     * Constructs an action.
     *
     * @param pv the protocol version
     * @param header the action header
     */
    ActSetQueue(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    /** Returns the port that the queue belongs to; Since 1.0; Dropped at 1.1.
     * This method returns null for protocol versions greater than 1.0.
     *
     * @return the associated port
     */
    public BigPortNumber getPort() {
        return port;
    }

    /** Returns the queue id.
     *
     * @return the queue id
     */
    public QueueId getQueueId() {
        return queueId;
    }
}
