/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Represents a {@link MeterBand} of type {@link MeterBandType#DROP DROP}.
 *
 * @author Simon Hunt
 */
public class MeterBandDrop extends MeterBand {
    /**
     * Constructor invoked by MeterFactory.
     *
     * @param pv the protocol version
     * @param header the meter band header
     */
    MeterBandDrop(ProtocolVersion pv, Header header) {
        super(pv, header);
    }
}
