/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.MeterId;

import static org.opendaylight.of.lib.CommonUtils.notNullIncompleteStruct;

/**
 * Base class for meter request (multipart body); Since 1.3.
 * <p>
 * Used by the {@link MBodyMeterStatsRequest} and the MBodyMeterConfigRequest.
 *
 * @author Scott Simes
 */
public abstract class MBodyMeterRequest extends OpenflowStructure
        implements MultipartBody {

    private static final int FIXED_LEN = 8;

    MeterId meterId;

    /**
     * Constructs a multipart body METER type request.
     *
     * @param pv the protocol version
     */
    MBodyMeterRequest(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public int getTotalLength() {
        return FIXED_LEN;
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(meterId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{meterId:").append(meterId).append("}");
        return sb.toString();
    }

    //===================================== GETTERS ===========================

    /**
     * Returns the meter id requested; since 1.3
     *
     * @return the meter id
     */
    public MeterId getMeterId() {
        return meterId;
    }
}
