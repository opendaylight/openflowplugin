/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;

/**
 * OXM Basic match field for {@code METADATA}.
 *
 * @author Simon Hunt
 */
public class MfbMetadata extends MFieldBasicLong {
    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MfbMetadata(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    String valueLabel() {
        return "meta";
    }

}
