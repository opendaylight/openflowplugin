/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.opendaylight.util.ByteUtils;

/**
 * Simple fixed-length message data carrier.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class FixedLengthMessage extends AbstractMessage {

    private final byte[] data;

    /**
     * Creates a new message with the specified length.
     *
     * @param length message length
     */
    public FixedLengthMessage(int length) {
        this.length = length;
        data = new byte[length];
    }

    /**
     * Creates a new message with the specified data.
     *
     * @param data message data
     */
    FixedLengthMessage(byte data[]) {
        this.length = data.length;
        this.data = data;
    }

    /**
     * Gets the backing byte array data.
     *
     * @return backing byte array
     */
    public byte[] data() {
        return data;
    }

    @Override
    public String toString() {
        return ByteUtils.toHexString(data);
    }

}
