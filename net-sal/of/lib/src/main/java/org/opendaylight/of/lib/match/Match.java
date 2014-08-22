/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.*;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.MatchUtils.sameMatchFields;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;
import static org.opendaylight.util.StringUtils.EOL;

/**
 * Represents an OpenFlow Match.
 *
 * @author Simon Hunt
 */
public class Match extends OpenflowStructure {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            Match.class, "match");

    private static final String E_UNKNOWN_TYPE = RES
            .getString("e_unknown_type");

    /** Our header. */
    final Header header;

    /** The match fields. */
    final List<MatchField> fields;

    /** Constructs a match structure.
     *
     * @param pv the protocol version
     * @param header the match header
     */
    Match(ProtocolVersion pv, Header header) {
        super(pv);
        this.header = header;
        this.fields = new ArrayList<MatchField>();
    }


    /** Returns the total length of the match structure in bytes,
     * including the padding to take the structure out to the
     * nearest 64-bit boundary.
     *
     * @return the length of the match
     */
    public int getTotalLength() {
        final ProtocolVersion pv = getVersion();
        int length;
        if (pv == V_1_0)
            length = MatchFactory.STANDARD_LENGTH_10;
        else if (pv == V_1_1)
            length = MatchFactory.STANDARD_LENGTH_11;
        else
            length = (header.length+7)/8*8;
        return length;
    }


    @Override
    public String toString() {
        return "{Match(" + version + "):" + header +
                ",fields=" + fieldListString() + "}";
    }

    /** Returns a comma separated list of match field names.
     *
     * @return match field names
     */
    private String fieldListString() {
        List<MatchField> mfList = fieldList();
        if (mfList.size() == 0)
            return CommonUtils.NONE;

        StringBuilder sb = new StringBuilder();
        for (MatchField mf: mfList)
            sb.append(mf.getFieldType()).append(",");
        int len = sb.length();
        sb.replace(len-1, len, "");
        return sb.toString();
    }

    /** Returns the fields in spec-defined order.
     *
     * @return the match fields in order
     */
    List<MatchField> fieldList() {
        return fields;
    }

    /** Returns a multi-line representation of this Match structure.
     *
     * @param indent a short string to prefix each line
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        String istr = StringUtils.spaces(indent + 2);
        StringBuilder sb = new StringBuilder(toString());
        for (MatchField mf: fieldList())
            sb.append(EOL).append(istr).append(mf);
        return sb.toString();
    }

    /** Returns a multi-line representation of this Match structure.
     *
     * @return a multi-line representation
     */
    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns the Match type.
     *
     * @return the match type
     */
    public MatchType getMatchType() {
        return header.type;
    }

    /* Implementation note:
    *   we don't expose the length field, since that is an
    *   implementation detail that the consumer should not care about.
    */

    /** Returns the list of match fields, in the order they were defined
     * in the match structure.
     *
     * @return the list of match fields
     */
    public List<MatchField> getMatchFields() {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Match match = (Match) o;
        return header.equals(match.header) &&
                sameMatchFields(fields, match.fields);
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + fields.hashCode();
        return result;
    }

    /** Parses the header structure from the given data buffer.
     * Note that this method will advance the reader index of the buffer
     * by the length of the Match structure header (4 bytes).
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed header
     * @throws HeaderParseException if there is an issue parsing the header
     * @throws DecodeException if the header cannot be decoded
     */
    static Header parseHeader(OfPacketReader pkt, ProtocolVersion pv)
            throws HeaderParseException, DecodeException {
        Header hdr = new Header();
        int typeCode = pkt.readU16();
        hdr.type = MatchType.decode(typeCode, pv);
        if (hdr.type == null)
            throw new HeaderParseException(pv + E_UNKNOWN_TYPE + typeCode);
        hdr.length = pkt.readU16();
        return hdr;
    }

    //======================================================================
    /** Represents the Match header. */
    static class Header {
        /** The type of match. */
        MatchType type;
        /** Length of match struct (excluding padding). */
        int length;

        @Override
        public String toString() {
            return "[type=" + type + ",len=" + length + "]";
        }

        /*
         * IMPLEMENTATION NOTE:
         *   We are not including length in the equivalence test, preferring
         *   to look at the list of fields (outside the header) instead.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Header header = (Header) o;
            return type == header.type;
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }
    }
}