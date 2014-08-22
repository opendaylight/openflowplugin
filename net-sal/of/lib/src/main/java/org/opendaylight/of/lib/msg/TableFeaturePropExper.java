/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ExperimenterId;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.util.ByteUtils.toHexArrayString;
import static org.opendaylight.util.StringUtils.spaces;

/**
 * Represents an experimenter table feature property.
 *
 * @author Simon Hunt
 */
public class TableFeaturePropExper extends TableFeatureProp {

    int encodedExpId;
    long expDefinedType;
    byte[] data;

    /**
     * Constructs a table feature property.
     *
     * @param header the property header
     */
    TableFeaturePropExper(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        final int len = sb.length();
        sb.replace(len-1, len, ": expId=")
                .append(ExperimenterId.idToString(encodedExpId))
                .append(",expType=").append(expDefinedType);
        if (data != null && data.length > 0)
            sb.append(",#dataBytes=").append(data.length);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int getTotalLength() {
        return header.length;
    }

    @Override
    String toDebugString(int indent) {
        StringBuilder sb = new StringBuilder(super.toDebugString(indent));
        final String indStr = EOLI + spaces(indent + 2);
        sb.append(indStr).append("Exp. ID: ")
                .append(ExperimenterId.idToString(encodedExpId));
        sb.append(indStr).append("Exp. defined type: ").append(expDefinedType);
        if (data != null && data.length > 0)
            sb.append(indStr).append("Exp. data: ")
                    .append(toHexArrayString(data));
        return sb.toString();
    }


    /** Returns the experimenter ID encoded as an int; Since 1.3.
     *
     * @return the experimenter ID (encoded)
     */
    public int getId() {
        return encodedExpId;
    }

    /** Returns the experimenter ID (if we know it); null otherwise; Since 1.3.
     *
     * @return the experimenter ID
     */
    public ExperimenterId getExpId() {
        return ExperimenterId.decode(encodedExpId);
    }

    /**
     * Returns the experimenter-defined type (u32); Since 1.3.
     *
     * @return the experimenter-defined type
     */
    public long getExpType() {
        return expDefinedType;
    }

    /**
     * Returns a copy of the experimenter-defined data.
     *
     * @return the data
     */
    public byte[] getData() {
        return data == null ? null : data.clone();
    }
}
