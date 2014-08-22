/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

/**
 * Represents an OpenFlow GET_CONFIG_REPLY message; Since 1.0.
 *
 * @author Simon Hunt
 */
public class OfmGetConfigReply extends SwitchConfig {
    /**
     * Constructs an OpenFlow GET_CONFIG_REPLY message.
     *
     * @param header the message header
     */
    OfmGetConfigReply(Header header) {
        super(header);
    }
}
