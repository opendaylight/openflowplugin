/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.*;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;

import java.util.ResourceBundle;

/**
 * Represents a Flow instruction.
 *
 * @author Simon Hunt
 */
public abstract class Instruction extends OpenflowStructure {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            Instruction.class, "instruction");

    private static final String E_UNKNOWN_TYPE = RES.getString("e_unknown_type");
    private static final String E_BAD_LENGTH = RES.getString("e_bad_length");
    private static final String E_EXP_MINIMUM = RES.getString("e_exp_minimum");
    private static final String E_BUT_FOUND = RES.getString("e_but_found");

    /** Our header. */
    final Header header;

    /** Constructs an instruction.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    Instruction(ProtocolVersion pv, Header header) {
        super(pv);
        this.header = header;
    }

    /** Returns the total length of the instruction in bytes.
     *
     * @return the length of the instruction
     */
    public int getTotalLength() {
        return header.length;
    }


    @Override
    public String toString() {
        return "{Instr:" + version.name() + ":" + header + "}";
    }

    /** Returns a string representation useful for debugging.
     * This default implementation delegates to {@link #toString()}, but
     * subclasses are free to override this behavior.
     *
     * @param indent the number of spaces with which to prefix each line
     * @return a (possibly multi-line) string representation of this instruction
     */
    public String toDebugString(int indent) {
        return StringUtils.spaces(indent) + toString();
    }

    /** Returns a string representation useful for debugging.
     * This default implementation delegates to {@link #toString()}, but
     * subclasses are free to override this behavior.
     *
     * @return a (possibly multi-line) string representation of this instruction
     */
    @Override
    public String toDebugString() {
        return toDebugString(0);
    }


    /** Returns the Instruction type.
     *
     * @return the instruction type
     */
    public InstructionType getInstructionType() {
        return header.type;
    }

    /* Implementation note:
    *   we don't expose the length field, since that is an
    *   implementation detail that the consumer should not care about.
    */


    /** 
     * Parses the header structure from the given data buffer.
     * Note that this method will advance the reader index of the buffer
     * by the length of the Instruction structure header (4 bytes).
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed header
     * @throws HeaderParseException if there is an issue parsing the header
     * @throws DecodeException if the header cannot be decoded
     */
    static Header parseHeader(OfPacketReader pkt, ProtocolVersion pv)
            throws HeaderParseException, DecodeException {
        return parseHeader(pkt, pv, true);
    }

    /**
     * Parses the header structure from the given data buffer.
     * Note that this method will advance the reader index of the buffer
     * by the length of the Instruction structure header (4 bytes).
     * If the enforceLengthConstraint argument is true, an exception will be
     * thrown if the length value is less than the minimum valid value.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @param enforceLengthConstraint true to enforce minimum length check
     * @return a parsed header
     * @throws HeaderParseException if there is an issue parsing the header
     * @throws DecodeException if the header cannot be decoded
     */
    static Header parseHeader(OfPacketReader pkt, ProtocolVersion pv, 
                              boolean enforceLengthConstraint)
            throws HeaderParseException, DecodeException {
        Header hdr = new Header();
        int typeCode = pkt.readU16();
        hdr.type = InstructionType.decode(typeCode, pv);
        // TODO: consider unexpected type - return null - when non-strict parse
        if (hdr.type == null)
            throw new HeaderParseException(pv + E_UNKNOWN_TYPE + typeCode);
        hdr.length = pkt.readU16();
        if (enforceLengthConstraint && hdr.length < hdr.type.minValidLength())
            throw new HeaderParseException(pv + E_BAD_LENGTH + hdr.type +
                    E_EXP_MINIMUM + hdr.type.minValidLength() +
                    E_BUT_FOUND + hdr.length);
        return hdr;
    }

    //======================================================================
    /** Represents the Instruction header. */
    static class Header {
        /** The type of instruction. */
        InstructionType type;
        /** Length of instruction struct. */
        int length;

        @Override
        public String toString() {
            return "[type=" + type + ",len=" + length + "]";
        }
    }
}
