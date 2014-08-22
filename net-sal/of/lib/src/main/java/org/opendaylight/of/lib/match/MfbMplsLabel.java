/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;

/**
 * OXM Basic match field for {@code MPLS_LABEL}.
 *
 * @author Simon Hunt
 */
public class MfbMplsLabel extends MFieldBasicInt {
    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MfbMplsLabel(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    String valueLabel() {
        return "mlab";
    }

    @Override
    String formatValue(int v) {
        return "0x" + Integer.toHexString(v);
    }
}
