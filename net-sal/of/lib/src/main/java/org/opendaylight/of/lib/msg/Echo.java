/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.util.ByteUtils;

import static org.opendaylight.of.lib.CommonUtils.EOLI;

/**
 * Base class for the OfmEchoRequest and OfmEchoReply.
 *
 * @author Scott Simes
 */
abstract class Echo extends OpenflowMessage {

    /**
     * The arbitrary data which may be included with the echo request/reply.
     * <p>
     * The data of an echo reply must be the unmodified data that was received
     * in the {@link OfmEchoRequest}.
     */
    byte[] data;

    /**
     * Base constructor that stores the header.
     *
     * @param header the message header
     */
    Echo(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (data != null && data.length > 0) {
            int len = sb.length();
            sb.replace(len-1, len, ",#bytes=").append(data.length).append("}");
        }
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        if (data != null && data.length > 0)
            sb.append(EOLI).append(ByteUtils.toHexArrayString(data));
        return sb.toString();
    }

    /**
     * Returns a copy of the data.
     *
     * @return the data
     */
    public byte[] getData() {
        return data == null ? null : data.clone();
    }
}
