/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.util.net.BigPortNumber;

/**
 * Represents an OpenFlow QUEUE_GET_CONFIG_REQUEST message; since 1.0.
 * <p>
 * Queue configuration takes place outside the OpenFlow protocol, either through
 * a command line tool or through an external dedicated configuration
 * protocol.
 * <p>
 * The controller can query the switch for configured queues using the
 * following structure.
 *
 * @author Scott Simes
 */
public class OfmQueueGetConfigRequest extends OpenflowMessage {

    BigPortNumber port;

    /**
     * Constructs an OpenFlow QUEUE_GET_CONFIG_REQUEST message.
     *
     * @param header the message header
     */
    OfmQueueGetConfigRequest(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",port=").append(Port.portNumberToString(port))
                                          .append("}");
        return sb.toString();
    }

    /**
     * Returns the ID of the port to query; Since 1.0.
     * <p>
     * This should refer to a valid physical port (i.e. &lt;= {@link Port#MAX}),
     * or to {@link Port#ANY} to request all configured queues.
     * <p>
     * Note that in 1.0, the port number is u16.
     *
     * @return the port number
     */
    public BigPortNumber getPort() {
        return port;
    }
}
