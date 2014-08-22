/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

/**
 * Represents an OpenFlow ROLE_REPLY message; Since 1.2.
 *
 * @author Pramod Shanbhag
 */
public class OfmRoleReply extends Role {
    
    /**
     * Constructs an OpenFlow ROLE_REPLY message.
     *
     * @param header the message header
     */
    OfmRoleReply(Header header) {
        super(header);
    }
}
