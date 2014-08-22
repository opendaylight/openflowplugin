/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.*;

/**
 * Represents an OXM TLV Match field. This abstract class serves as the base
 * for all match fields.
 *
 * @author Simon Hunt
 */
public abstract class MatchField extends OpenflowStructure {

    private static final int CLASS_SHIFT = 8;
    private static final int FIELD_TYPE_MASK = 0xfe;
    private static final int FIELD_TYPE_SHIFT = 1;
    private static final int MASK_MASK = 0x01;
    private static final int OXM_TYPE_MASK = 0xfffffe;

    /** The match field header. */
    final Header header;

    /** Constructs a match field.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MatchField(ProtocolVersion pv, Header header) {
        super(pv);
        this.header = header;
    }

    @Override
    public String toString() {
        return "{Oxm:" + version.name() + ":" + header + "}";
    }

    /** Parses the header structure from the given data buffer. The payload
     * length is validated for the field type (and whether or not the field
     * has a mask).
     * <p>
     * Note that this method will advance the reader index of the buffer
     * by the length of an OXM TLV header (4 bytes).
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed header
     * @throws HeaderParseException if there is an issue parsing the header
     * @throws DecodeException if the header cannot be decoded
     */
    static Header parseHeader(OfPacketReader pkt, ProtocolVersion pv)
            throws HeaderParseException, DecodeException {
        return parseHeader(pkt, pv, false);
    }

    /** Parses the header structure from the given data buffer. If the
     * {@code zeroLen} argument is true, we are reading match field headers
     * only and expect the payload length to be zero; otherwise we will
     * validate that the length is correct for the field type (and whether
     * or not the field has a mask).
     * <p>
     * Note that this method will advance the reader index of the buffer
     * by the length of an OXM TLV header (4 bytes).
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @param zeroLen true if the payload length is expected to be zero
     * @return a parsed header
     * @throws HeaderParseException if there is an issue parsing the header
     * @throws DecodeException if the header cannot be decoded
     */
    static Header parseHeader(OfPacketReader pkt, ProtocolVersion pv,
                              boolean zeroLen)
            throws HeaderParseException, DecodeException {
        Header hdr = new Header();

        // read the raw class and field as a single type value, then rewind
        hdr.rawOxmType = pkt.peekU24() & OXM_TYPE_MASK;

        // start over, this time treating class and field as separate values
        hdr.rawClazz = pkt.readU16();
        hdr.clazz = OxmClass.decode(hdr.rawClazz, pv);
        int fieldAndMask = pkt.readU8();
        hdr.rawFieldType = (fieldAndMask & FIELD_TYPE_MASK) >> FIELD_TYPE_SHIFT;
        hdr.fieldType = lookupField(hdr.clazz, hdr.rawFieldType, pv);
        hdr.hasMask = (fieldAndMask & MASK_MASK) != 0;

        hdr.length = pkt.readU8();
        // validate the payload length
        if (hdr.fieldType != null) {
            OxmBasicFieldType ft = (OxmBasicFieldType) hdr.fieldType;
            // we know it is a basic field
            if (zeroLen) {
                // we are parsing Header-Only structures...
                /*
                 * IMPLEMENTATION NOTE:
                 *   Well behaved OpenFlow switches should (probably) set a
                 *   payload length of zero, since these are headers only, and
                 *   there is no payload. However, we'll be lenient...
                 */
//                if (hdr.length != 0)
//                    throw new HeaderParseException(pv + " " + ft +
//                            E_ZERO_LEN + hdr.length);
            } else {
                // we are parsing Header and Payload (and possible Mask)
                if (hdr.length != ft.expectedLength(hdr.hasMask)) {
                    // FIXME : Need to re-instate this label size check
                    //  once we have clarified the correct encoding length for:
                    //     IPV6_FLABEL (20 bits) : currently 3 bytes
                    //     MPLS_LABEL  (20 bits) : currently 3 bytes
                    //     PBB_ISID    (24 bits) : currently 3 bytes
                    // See OxmBasicFieldType enum.
//                    throw new HeaderParseException(pv + " " + ft +
//                            E_FIELD_LEN + hdr.length);
                }
            }
        }
        return hdr;
    }

//        static final String E_FIELD_LEN = " Bad match field length: ";
//        static final String E_ZERO_LEN = " Payload length not zero: ";

    /** Looks up the appropriate field type constant, based on the class.
     *
     * @param clazz OXM class (may be null)
     * @param rawFieldType decoded field type value
     * @param pv protocol version
     * @return matching field constant (if any)
     * @throws DecodeException if the field type cannot be decoded
     */
    private static OxmFieldType lookupField(OxmClass clazz, int rawFieldType,
                                            ProtocolVersion pv)
            throws DecodeException {
        OxmFieldType fieldType = null;
        if (clazz == OxmClass.OPENFLOW_BASIC)
            fieldType = OxmBasicFieldType.decode(rawFieldType, pv);
        return fieldType;
    }

    /** Combines the class and field type values into a single OXM Type value.
     *
     * @param rawClazz the class value
     * @param rawFieldType the field type value
     * @return the OXM Type value
     */
    static int calcRawOxmType(int rawClazz, int rawFieldType) {
        return (rawClazz << CLASS_SHIFT) | (rawFieldType << FIELD_TYPE_SHIFT);
    }

    /** Returns the raw (undecoded) OXM class value (u16).
     *
     * @return the raw OXM class value
     */
    public int getRawOxmClass() {
        return header.rawClazz;
    }

    /** Returns the OXM class.
     *
     * @return the OXM class
     */
    public OxmClass getOxmClass() {
        return header.clazz;
    }

    /** Returns the raw (undecoded) OXM field type value (u7).
     *
     * @return the raw OXM field type value
     */
    public int getRawFieldType() {
        return header.rawFieldType;
    }

    /** Returns the OXM field type.
     *
     * @return the OXM field type
     */
    public OxmFieldType getFieldType() {
        return header.fieldType;
    }

    /** Returns true if the match field payload includes a mask.
     *
     * @return true if a mask is included
     */
    public boolean hasMask() {
        return header.hasMask;
    }

    /** Returns the length of the match field payload in bytes.
     *
     * @return the length of the payload
     */
    public int getPayloadLength() {
        return header.length;
    }

    /** Returns the total length of the match field structure in bytes.
     *
     * @return the length of the match field
     */
    public int getTotalLength() {
        return MatchFactory.FIELD_HEADER_LEN + header.length;
    }

    //======================================================================
    /** Represents the OXM match field header. */
    static class Header {
        /** Combined OXM class and OXM field types as a single type value. */
        int rawOxmType;
        /** Raw (undecoded) OXM class. (u16) */
        int rawClazz;
        /** Decoded OXM class. */
        OxmClass clazz;
        /** Raw (undecoded) OXM field type. (u7) */
        int rawFieldType;
        /** Decoded OXM field. */
        OxmFieldType fieldType;
        /** Does the payload include a mask. */
        boolean hasMask;
        /** Payload length (excluding the header). */
        int length;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Header header = (Header) o;

            return rawOxmType == header.rawOxmType &&
                    hasMask == header.hasMask && length == header.length;
        }

        @Override
        public int hashCode() {
            int result = rawOxmType;
            result = 31 * result + (hasMask ? 1 : 0);
            result = 31 * result + length;
            return result;
        }

        @Override
        public String toString() {
            return "[cls=0x" +
                    Integer.toHexString(rawClazz) + "(" + clazz + "),ft=" +
                    rawFieldType + "(" + fieldType + "),hm=" +
                    hasMask + ",len=" + length + "]";
        }
    }
}
