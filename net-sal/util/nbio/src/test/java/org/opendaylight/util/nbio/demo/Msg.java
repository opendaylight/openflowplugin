/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio.demo;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.nbio.AbstractMessage;

/**
 * A simple fixed-length message, {@value #LENGTH} bytes in length.
 *
 * @author Simon Hunt
 */
public class Msg extends AbstractMessage {

    private static final String E_BAD_DATA = "data array bad length";

    /** Length of messages, in bytes. */
    public static final int LENGTH = 4;

    /*
        Let's define a simple message structure:
        +---------+---------+---------+---------+
        |  type   | counter | cookie  |  value  |
        +---------+---------+---------+---------+
     */

    private enum Field {
        TYPE, COUNTER, COOKIE, VALUE
    }

    private final byte[] data;

    /**
     * Creates a message with the given data payload.
     *
     * @param data message data
     * @throws IllegalArgumentException if data is null or is not of
     *          length {@value #LENGTH}
     */
    public Msg(byte[] data) {
        if (data == null || data.length != LENGTH)
            throw new IllegalArgumentException(E_BAD_DATA);
        this.length = LENGTH;
        this.data = data;
    }

    /**
     * Creates a new message with default payload.
     */
    public Msg() {
        this.length = LENGTH;
        data = new byte[LENGTH];
    }

    /**
     * Returns the backing data byte array.
     *
     * @return backing byte array
     */
    public byte[] data() {
        return data;
    }

    @Override
    public String toString() {
        return "Msg{" + ByteUtils.toHexString(data) + "}";
    }

    /**
     * Increments the counter field of the message.
     */
    public void incrementCounter() {
        int counter = getField(Field.COUNTER);
        setField(Field.COUNTER, (byte) (counter + 1));
    }

    /**
     * Returns the byte for the given field
     *
     * @param field the field to retrieve
     * @return the byte value
     */
    private byte getField(Field field) {
        return data[field.ordinal()];
    }

    /**
     * Sets the byte of the specified field to the given value.
     *
     * @param field the field to set
     * @param b the byte value
     */
    private void setField(Field field, byte b) {
        data[field.ordinal()] = b;
    }
}
