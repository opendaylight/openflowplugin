/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Represents a meter stats request (multipart body); Since 1.3.
 *
 * @author Scott Simes
 */
public class MBodyMeterStatsRequest extends MBodyMeterRequest {
    /**
     * Constructs a multipart body METER STATS type request.
     *
     * @param pv the protocol version
     */
    public MBodyMeterStatsRequest(ProtocolVersion pv) {
        super(pv);
    }
}
