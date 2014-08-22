/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Mutable subclass of {@link MBodyMeterConfigRequest}.
 *
 * @author Scott Simes
 */
public class MBodyMutableMeterConfigRequest extends MBodyMutableMeterRequest {
    /**
     * Constructs a mutable multipart body METER_CONFIG type request.
     *
     * @param pv the protocol version
     */
    MBodyMutableMeterConfigRequest(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public OpenflowStructure toImmutable() {
        // can only do this once
        mutt.invalidate(this);
        // transfer to the immutable instance
        MBodyMeterConfigRequest config = new MBodyMeterConfigRequest(version);
        config.meterId = this.meterId;

        return config;
    }
}
