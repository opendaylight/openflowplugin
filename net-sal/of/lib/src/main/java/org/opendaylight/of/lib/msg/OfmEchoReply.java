/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

/**
 * Represents an OpenFlow ECHO REPLY message; Since 1.0.
 *
 * @author Scott Simes
 */
public class OfmEchoReply extends Echo {

    /**
     * Constructs an OpenFlow ECHO_REPLY message.
     *
     * @param header the message header
     */
    OfmEchoReply(Header header) {
        super(header);
    }
}
