/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.ByteUtils;

import java.util.Arrays;

/**
 * Experimenter OpenFlow match field descriptor.
 *
 * @author Simon Hunt
 */
public class MFieldExperimenter extends MatchField {

    /** Experimenter id (encoded). */
    int id;

    /** Experimenter-defined payload. */
    byte[] payload;

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MFieldExperimenter(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",expId=").append(ExperimenterId.idToString(id));
        if (payload != null)
            sb.append(",data=").append(ByteUtils.toHexArrayString(payload));
        sb.append("}");
        return sb.toString();
    }

    /** Returns the experimenter ID encoded as an int.
     *
     * @return the experimenter ID (encoded)
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

    /** Returns the ID encoded as a byte array.
     *
     * @return the ID as a byte array
     */
    // NOTE:
    //   Although ExperimenterId implements an identical method, we can't use
    //   that one, because we may have decoded an experimenter id that is not
    //   (yet) defined in the ExperimenterId enum.
    public byte[] getIdAsBytes() {
        byte[] bytes = new byte[4];
        ByteUtils.setInteger(bytes, 0, id);
        return bytes;
    }

    /** Returns a copy of the experimenter-defined match payload.
     *
     * @return a copy of the payload
     */
    public byte[] getPayload() {
        return payload == null ? null : payload.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MFieldExperimenter that = (MFieldExperimenter) o;
        return header.equals(that.header) && id == that.id
                && Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + id;
        result = 31 * result + (payload != null ? Arrays.hashCode(payload) : 0);
        return result;
    }
}
