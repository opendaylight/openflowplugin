/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;

/**
 * Represents a Hello element. This abstract class serves as the base for
 * all such elements.
 *
 * @author Simon Hunt
 */
public abstract class HelloElement extends OpenflowStructure {

    /** The element header. */
    final Header header;

    /** Constructs a hello element.
     *
     * @param pv the protocol version
     * @param header the element header
     */
    HelloElement(ProtocolVersion pv, Header header) {
        super(pv);
        this.header = header;
    }

    @Override
    public String toString() {
        return "{HelloElem:" + header + "}";
    }

    /** Returns the hello element type.
     *
     * @return the element type
     */
    public HelloElementType getElementType() {
        return header.type;
    }

    /* Implementation note:
    *   we don't expose the length field, since that is an
    *   implementation detail that the consumer should not care about.
    */

    /** Returns a short label to be used in {@link OfmHello#toString}.
     * This default implementation returns the element type constant as a
     * string.
     *
     * @return a short label for this element
     */
    String getElementLabel() {
        return header.type.toString();
    }

    //=======================================================================

    /** Parses the header structure from the given data buffer.
     * Note that this method will advance the reader index of the buffer
     * by the length of a hello element header (4 bytes).
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
        hdr.type = HelloElementType.decode(typeCode, pv);
        hdr.length = pkt.readU16();
        return hdr;
    }

    /** Returns the total length of this element, in bytes.
     *
     * @return the total length, in bytes
     */
    public int getTotalLength() {
        return header.length;
    }


    //=======================================================================

    /** Represents the hello element header. */
    static class Header {
        /** Decoded element type. */
        HelloElementType type;
        /** Length in bytes of this element. */
        int length;

        @Override
        public String toString() {
            return "[" + type + ",len=" + length + "]";
        }
    }
}
