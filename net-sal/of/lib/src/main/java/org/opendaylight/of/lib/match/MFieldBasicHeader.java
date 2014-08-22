/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Denotes an OXM Basic match field, holding just the header information.
 * This class is provided to support the parsing of the "OXM" table
 * feature property.
 *
 * @see org.opendaylight.of.lib.msg.TableFeaturePropOxm
 *
 * @author Simon Hunt
 */
public class MFieldBasicHeader extends MatchField {
    /**
     * Constructs a match field.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MFieldBasicHeader(ProtocolVersion pv, Header header) {
        super(pv, header);
    }
}
