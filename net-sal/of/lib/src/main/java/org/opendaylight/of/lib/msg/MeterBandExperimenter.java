/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Represents a {@link MeterBand} of type
 * {@link MeterBandType#EXPERIMENTER EXPERIMENTER}.
 *
 * @author Simon Hunt
 */
public class MeterBandExperimenter extends MeterBand {

    /** Experimenter id (encoded). */
    int id;

    /**
     * Constructor invoked by MeterFactory.
     *
     * @param pv the protocol version
     * @param header the meter band header
     */
    MeterBandExperimenter(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",expID=").append(ExperimenterId.idToString(id))
                .append("}");
        return sb.toString();
    }

    /** Returns the encoded experimenter id.
     *
     * @return the encoded id
     */
    public int getId() {
        return id;
    }

    /** Returns the experimenter ID (if we know it); null otherwise.
     *
     * @return the experimenter ID
     */
    public ExperimenterId getExpId() {
        return ExperimenterId.decode(id);
    }

}
