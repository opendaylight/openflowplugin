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
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.notNullIncompleteStruct;

/**
 * Represents a port stats request (multipart body); Since 1.0.
 *
 * @author Pramod Shanbhag
 */
public class MBodyPortStatsRequest extends OpenflowStructure 
        implements MultipartBody {
    private static final int BODY_FIXED_LEN = 8;

    BigPortNumber port;

    /**
     * Constructs a multipart body PORT_STATS type request.
     *
     * @param pv the protocol version
     */
    public MBodyPortStatsRequest(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{port:");
        sb.append(Port.portNumberToString(port)).append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Port Number : ").append(Port.portNumberToString(port));
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(port);
    }

    @Override
    public int getTotalLength() {
        return BODY_FIXED_LEN;
    }

    /** Returns the port for which stats are requested; Since 1.0.
     * <p>
     * A value of {@link Port#ANY} indicates stats requested for all ports.
     *
     * @return the requested port
     */
    public BigPortNumber getPort() {
        return port;
    }
}
