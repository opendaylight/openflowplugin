/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.ByteUtils;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;

/**
 * Represents an Experimenter extension to a multipart request or reply message;
 * since 1.0.
 * <p>
 * This class also represents the Vendor extension to a stats request or reply
 * message that was introduced at version 1.0, and then replaced with this
 * Experimenter extension starting with version 1.1.  The Vender ID from the
 * OpenFlow 1.0 specification is mapped to the Experimenter ID, which was
 * introduced in the 1.1 specification.
 * <p>
 * The experimenter ID uniquely identifies the experimenter.  If the most
 * significant byte is zero, the next three bytes are the experimenter's
 * IEEE OUI.  If the most significant byte is not zero, it is a value allocated
 * by the Open Networking Foundation.
 * <p>
 * The rest of the experimenter request/reply body is uninterpreted by standard
 * OpenFlow processing and is arbitrarily defined by the corresponding
 * experimenter.
 *
 * @author Scott Simes
 */
public class MBodyExperimenter extends OpenflowStructure
        implements MultipartBody {

    /**
     * The experimenter ID; Since 1.0.
     * Represents the Vendor ID for 1.0.
     */
    int id;

    /**
     * The experimenter type; Since 1.2.
     */
    int type;

    /**
     * Experimenter defined; Since 1.0.
     */
    byte [] data;

    /**
     * Constructs a multipart body EXPERIMENTER extension type.
     *
     * @param pv the protocol version
     */
    public MBodyExperimenter(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        ProtocolVersion pv = getVersion();
        int len = sb.length();
        sb.replace(len-1, len, ",expId=").append(ExperimenterId.idToString(id));
        if(pv.ge(V_1_2))
            sb.append(",expType=").append(type);

        if (data != null && data.length > 0) {
            sb.append(",#bytes=").append(data.length);
        }
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

    @Override
    public int getTotalLength() {
        ProtocolVersion pv = getVersion();
        // fixed size for Experimenter structure - ID only for 1.0 (4 bytes)
        // ID and Type for 1.1,1.2,and 1.3 (8 bytes)
        int fixed = pv == V_1_0 ? 4 : 8;
        return data == null ? fixed : fixed + data.length;
    }

    /**
     * Returns the experimenter ID encoded as an int; Since 1.0.
     *
     * @return the experimenter ID (encoded)
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the experimenter ID (if we know it); null otherwise; Since 1.0.
     *
     * @return the experimenter ID
     */
    public ExperimenterId getExpId() {
        return ExperimenterId.decode(id);
    }

    /**
     * Returns the experimenter-defined type; Since 1.3.
     * <p>
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
