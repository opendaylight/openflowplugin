/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.err.ErrorCode;
import org.opendaylight.of.lib.err.ErrorCodeLookup;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Mutable subclass of {@link OfmError}.
 *
 * @author Simon Hunt
 */
public class OfmMutableError extends OfmError implements MutableMessage {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            OfmMutableError.class, "ofmMutableError");

    private static final String E_TYPE_NOT_SET = RES
            .getString("e_type_not_set");
    private static final String E_USE_ERR_MSG_METHOD = RES
            .getString("e_use_err_msg_method");
    private static final String E_TYPE_NOT_HELLO_FAILED = RES
            .getString("e_type_not_hello_failed");
    private static final String E_WRONG_CODE_CLASS = RES
            .getString("e_wrong_code_class");

    private static final int TYPE_AND_CODE_LEN = 4;
    private static final int FIXED_HEADER_LEN =
            OpenflowMessage.OFM_HEADER_LEN + TYPE_AND_CODE_LEN;

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow ERROR message.
     *
     * @param header the message header
     */
    OfmMutableError(Header header) {
        super(header);
    }

    @Override
    public void clearXid() {
        mutt.checkWritable(this);
        header.xid = 0;
    }

    @Override
    public OpenflowMessage toImmutable() {
        // Can only do this once
        mutt.invalidate(this);

        // Copy over to read-only instance
        OfmError msg = new OfmError(header);
        msg.type = this.type;
        msg.code = this.code;
        msg.data = this.data;
        msg.errMsg = this.errMsg;
        return msg;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }

    // =====================================================================
    // ==== SETTERS

    /** Sets the error type; Since 1.0.
     *
     * @param type the error type
     * @return self, for chaining
     * @throws NullPointerException if type is null
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableError errorType(ErrorType type) {
        mutt.checkWritable(this);
        notNull(type);
        this.type = type;
        return this;
    }

    /** Sets the error code; Since 1.0. Note that the error type must have
     * been set already, or an {@code IllegalStateException} will be thrown.
     *
     * @param code the error code
     * @return self, for chaining
     * @throws NullPointerException if type is null
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if code is not appropriate for the type
     * @throws IllegalStateException if the type has not been set yet
     */
    public OfmMutableError errorCode(ErrorCode code) {
        mutt.checkWritable(this);
        notNull(code);
        if (type == null)
            throw new IllegalStateException(E_TYPE_NOT_SET);
        Class<? extends ErrorCode> reqClass = ErrorCodeLookup.codeClass(type);
        if (code.getClass() != reqClass)
            throw new IllegalArgumentException(
                    StringUtils.format(E_WRONG_CODE_CLASS, type, code));
        ErrorCodeLookup.validate(type, code, header.version);
        this.code = code;
        return this;
    }


    /** Sets the associated data; Since 1.0. Note that the error type must have
     * been set already, or an {@code IllegalStateException} will be thrown.
     * If the error type is {@code HELLO_FAILED}, use
     * {@link #errorMessage} instead.
     *
     * @param data the data
     * @return self, for chaining
     * @throws NullPointerException if data is null
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalStateException if the type has not been set or if it is
     *              HELLO_FAILED
     */
    public OfmMutableError setData(byte[] data) {
        mutt.checkWritable(this);
        if (type == null)
            throw new IllegalStateException(E_TYPE_NOT_SET);
        if (type == ErrorType.HELLO_FAILED)
            throw new IllegalStateException(E_USE_ERR_MSG_METHOD);
        this.data = data.clone();
        header.length = FIXED_HEADER_LEN + data.length;
        return this;
    }

    /** Sets the error message; Since 1.0.
     * Note that this is only appropriate if the error type has already been
     * set to {@code HELLO_FAILED}. The message is encoded as ASCII in the
     * data field.
     *
     * @param msg the error message to set
     * @return self, for chaining
     * @throws NullPointerException if data is null
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalStateException if the type is not HELLO_FAILED
     */
    public OfmMutableError errorMessage(String msg) {
        mutt.checkWritable(this);
        notNull(msg);
        if (type != ErrorType.HELLO_FAILED)
            throw new IllegalStateException(E_TYPE_NOT_HELLO_FAILED + type);
        this.errMsg = msg;
        data = convertToPaddedAsciiBytes(msg);
        header.length = FIXED_HEADER_LEN + data.length;
        return this;
    }

    private static final String ASCII = "US-ASCII";

    /** Round the length of the specified message up such that the total
     * length of this error message will be a multiple of 8; encode as ASCII
     * and return in a newly allocated byte array.
     *
     * @param msg the message to encode
     * @return the message encoded in a byte array
     */
    private byte[] convertToPaddedAsciiBytes(String msg) {
        byte[] padded;
        int roundedUp = roundedUpArrayLength(msg);
        try {
            byte[] str = msg.getBytes(ASCII);
            padded = Arrays.copyOf(str, roundedUp);
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new IllegalStateException(e);
        }
        return padded;
    }

    /** Return the required length of the array to store the given message,
     * such that the total length of this error message will be a multiple
     * of 8.
     *
     * @param s the string to be stored
     * @return the appropriate length of the array
     */
    static int roundedUpArrayLength(String s) {
        return (s.length() + TYPE_AND_CODE_LEN + 7) / 8 * 8 - TYPE_AND_CODE_LEN;
    }
}