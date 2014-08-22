/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.ByteUtils;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;

/**
 * Represents an OpenFlow EXPERIMENTER message; since 1.0.
 * <p>
 * This class also represents the Vendor message that was introduced at
 * version 1.0, and then replaced with this Experimenter message starting with
 * version 1.1.  The Vender field from the OpenFlow 1.0 specification is mapped
 * to the Experimenter Id, which was introduced in the 1.1 specification.  The
 * arbitrary data byte array is common across all version.
 * <p>
 * The experimenter id uniquely identifies the experimenter.  If the most
 * significant byte is zero, the next three bytes are the experimenter's
 * IEEE OUI.  If the most significant byte is not zero, it is a value allocated
 * by the Open Networking Foundation.
 * <p>
 * The rest of the body for this message is uninterpreted by standard OpenFlow
 * processing and is arbitrarily defined by the corresponding experimenter.
 * <p>
 * If a switch does not understand an experimenter extension, it must send an
 * OFPT_ERROR message with a OFPBRC_BAD_EXPERIMENTER error code and
 * OFPET_BAD_REQUEST error type.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
// TODO: link in correct Error message types when available in the javadoc
public class OfmExperimenter extends OpenflowMessage {

    /**
     * The experimenter ID; Since 1.1.
     * Represents the Vendor ID for 1.0.
     */
    int id;

    /**
     * The experimenter type; Since 1.2.
     */
    int type;

    /**
     * Arbitrary experimenter defined additional data; Since 1.0.
     */
    byte[] data;


    /**
     * Constructs an OpenFlow Experimenter message.
     *
     * @param header the message header
     */
    OfmExperimenter(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        ProtocolVersion pv = getVersion();
        int len = sb.length();
        sb.replace(len-1, len, ",expId=").append(ExperimenterId.idToString(id));
        if(pv.gt(V_1_1))
            sb.append(",expType=").append(type);
        if (data != null && data.length > 0)
            sb.append(",#dataBytes=").append(data.length);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        if (data != null && data.length > 0)
            sb.append(EOLI).append(ByteUtils.toHexArrayString(data));
        return sb.toString();
    }

    /** Returns the experimenter ID encoded as an int; Since 1.0.
     *
     * @return the experimenter ID (encoded)
     */
    public int getId() {
        return id;
    }

    /** Returns the experimenter ID (if we know it); null otherwise; Since 1.0.
     *
     * @return the experimenter ID
     */
    public ExperimenterId getExpId() {
        return ExperimenterId.decode(id);
    }

    /**
     * Returns the experimenter-defined  type; Since 1.2.
     *
     * For 1.0 messages, the value returned is 0.
     * For 1.1 messages, the value returned is undetermined
     *  (but assumed to be 0).
     *
     * @return the experimenter-defined type
     */
    public int getExpType() {
        return type;
    }

    /**
     * Returns a copy of the experimenter-defined additional data.
     *
     * @return the data
     */
    public byte[] getData() {
        return data == null ? null : data.clone();
    }
}
