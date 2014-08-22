/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.util.net.BigPortNumber;

import java.util.Collections;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.cSize;

/**
 * Represents an OpenFlow QUEUE_GET_CONFIG_REPLY message; since 1.0.
 * <p>
 * Queue configuration takes place outside the OpenFlow protocol, either through
 * a command line tool or through an external dedicated configuration
 * protocol.
 * <p>
 * The controller can query the switch for configured queues, and the reply will
 * contain a list of configured queues.
 *
 * @author Scott Simes
 */
public class OfmQueueGetConfigReply extends OpenflowMessage {

    BigPortNumber port;

    /** list of configured queues for the port. */
    List<Queue> queues;

    /**
     * Constructs an OpenFlow QUEUE_GET_CONFIG_REPLY message.
     *
     * @param header the message header
     */
    OfmQueueGetConfigReply(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",port=").append(Port.portNumberToString(port));
        sb.append(cSize(queues)).append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
       StringBuilder sb = new StringBuilder(toString());
        if (queues != null) {
            for (Queue que : queues)
                sb.append(EOLI).append(que);
        }
        return sb.toString();
    }

    /**
     * Returns the ID of the port that was queried; Since 1.0.
     * <p>
     * This should refer to a valid physical port (i.e. &lt;= {@link Port#MAX}),
     * or to {@link Port#ANY} for all configured queues.
     * <p>
     * Note that in 1.0, the port number is u16.
     *
     * @return the port number
     */
    public BigPortNumber getPort() {
        return port;
    }

    /**
     * Returns the list of configured queues; Since 1.0.
     *
     * @return the list of queues
     */
    public List<Queue> getQueues() {
        return queues == null ? null : Collections.unmodifiableList(queues);
    }
}
