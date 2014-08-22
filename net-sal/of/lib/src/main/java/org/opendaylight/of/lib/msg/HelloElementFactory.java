/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.util.NotYetImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.hex;
import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.msg.HelloElementType.VERSION_BITMAP;

/**
 * Provides facilities for parsing, creating and encoding {@link HelloElement}
 * instances.
 *
 * @author Simon Hunt
 */
public class HelloElementFactory extends AbstractFactory {

    /** Designates the highest value protocol version (code) that can be
     * represented in the first bitmap (codes 0..31) of a version bitmap
     * element. See section A.5.1 of the OpenFlow 1.3.1 specification (pg. 101)
     */
    private static final int MAX_BIT_IN_FIRST_BITMAP = 0x1f;

    static final int HELEM_HEADER_LEN = 4;
    static final HelloElementFactory HEF = new HelloElementFactory();

    // No instantiation but here
    private HelloElementFactory() {}

    /** Returns an identifying tag for the hello element factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "HEF";
    }

    // =======================================================================
    // === Parse Elements

    /** Parses a list of elements from the supplied buffer.
     *
     * @param pkt the buffer to read from
     * @param pv the protocol version
     * @return the list of parsed elements
     * @throws MessageParseException if unable to parse the element
     */
    static List<HelloElement> parseElementList(OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        List<HelloElement> elemList = new ArrayList<>();
        final int targetRi = pkt.targetIndex();
        while (pkt.ri() < targetRi) {
            HelloElement elem = parseElement(pkt, pv);
            if (elem != null)
                elemList.add(elem);
        }
        return elemList;
    }

    /** Parses a single element from the supplied buffer.
     * If it is one that we do not recognize, we should return null.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the element.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed element
     * @throws MessageParseException if unable to parse the element
     */
    static HelloElement parseElement(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        try {
            HelloElement.Header header = HelloElement.parseHeader(pkt, pv);
            return createElemInstance(header, pkt, pv);
        } catch (MessageParseException mpe) {
            // rethrow MPE
            throw mpe;
        } catch (Exception e) {
            // wrap any other exception in an MPE
            throw HEF.mpe(pkt, e);
        }
    }

    /** Creates the element instance, based on the header information.
     *
     * @param header the already-parsed header
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return the instantiated element
     * @throws MessageParseException if there is an issue parsing the element
     */
    private static HelloElement createElemInstance(HelloElement.Header header,
                                                   OfPacketReader pkt,
                                                   ProtocolVersion pv)
            throws MessageParseException {
        HelloElement elem = null;
        switch (header.type) {
            case VERSION_BITMAP:
                elem = bitmapElem(new HelloElemVersionBitmap(pv, header), pkt);
                break;
            default:
                // don't know what this element is - so skip it
                pkt.skip(header.length - HELEM_HEADER_LEN);
                break;
        }
        return elem;
    }

    /** Completes parsing a version-bitmap hello element.
     *
     * @param elem the element
     * @param pkt the data buffer
     * @return the parsed element
     */
    private static HelloElement bitmapElem(HelloElemVersionBitmap elem,
                                           OfPacketReader pkt) {
        /*
         * IMPLEMENTATION NOTE:
         *   The 1.3.1 specification states that:
         *   (a) The version field part of the HELLO message header must be
         *        set to the highest OpenFlow protocol version supported
         *        by the sender.
         *   (b) The number of bitmaps included in the bitmaps field of the
         *        version-bitmap hello element depends on the highest version
         *        number supported.
         *
         *   Thus we can infer (b) from (a), since the decoded value for (a)
         *   is stored in the header of the HelloElement.
         *   ---------------------------------------------------------------
         *   However, having said all that, we don't need to worry until we
         *   reach version 0x20 (32) of the protocol; right now we are at
         *   0x04 (1.3.1). So we are making the simplifying assumption that
         *   there will only be a single u32 bitmap for the near future.
         */

        int bitmap = pkt.readInt();
        if ((bitmap & ~SUPPORTED_VERSION_MASK) != 0)
            throw new IllegalArgumentException(E_BAD_BITS + hex(bitmap));

        elem.supportedVersions = new TreeSet<>();
        for (ProtocolVersion pv: ProtocolVersion.values()) {
            int bit = 1 << pv.code();
            if ((bitmap & bit) != 0)
                elem.supportedVersions.add(pv);
        }
        return elem;
    }

    private static final String E_BAD_BITS = "Bad bits set in version bitmap: ";
    private static int SUPPORTED_VERSION_MASK;

    static {
        // generate the supported version mask
        int mask = 0;
        for (ProtocolVersion pv: ProtocolVersion.values()) {
            if (pv.code() > MAX_BIT_IN_FIRST_BITMAP)
                throw new NotYetImplementedException("Second bitmap required");
            mask |= (1 << pv.code());
        }
        SUPPORTED_VERSION_MASK = mask;
    }


    // =======================================================================
    // === Create Elements

    /** Create an element header, setting the length to the default value.
     *
     * @param type the element type
     * @return the header
     */
    private static HelloElement.Header createHeader(HelloElementType type) {
        HelloElement.Header header = new HelloElement.Header();
        header.type = type;
        header.length = HELEM_HEADER_LEN;
        return header;
    }

    private static final String E_EMPTY_SET = "suppVers cannot be empty set";

    /** Creates a VERSION BITMAP hello element for the given supported
     * versions. Note that the {@link ProtocolVersion} in the header of the
     * {@link OfmHello} message must be set to the highest version listed
     * in the bitmap; so that version is embedded in this element structure
     * so that it can be retrieved easily when constructing the HELLO message.
     *
     * @param suppVers supported versions
     * @return the element
     * @throws NullPointerException if suppVers is null
     * @throws IllegalArgumentException if suppVers is empty
     */
    public static HelloElement
    createVersionBitmapElement(Set<ProtocolVersion> suppVers) {
        notNull(suppVers);
        if (suppVers.size() == 0)
            throw new IllegalArgumentException(E_EMPTY_SET);

        ProtocolVersion maxPv = ProtocolVersion.max(suppVers);

        HelloElement.Header hdr = createHeader(VERSION_BITMAP);
        HelloElemVersionBitmap elem = new HelloElemVersionBitmap(maxPv, hdr);
        elem.supportedVersions = new TreeSet<>(suppVers);
        elem.header.length = elem.calcTotalLength();
        return elem;
    }

    // =======================================================================
    // === Encode Elements

    /** Encodes a hello element, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the element.
     *
     * @param elem the hello element
     * @param pkt the buffer into which the element is to be written
     */
    public static void encodeElement(HelloElement elem, OfPacketWriter pkt) {
        // First, write out the header..
        ProtocolVersion pv = elem.getVersion();
        pkt.writeU16(elem.header.type.getCode(pv));
        pkt.writeU16(elem.header.length);

        // now deal with the payload, based on type
        switch (elem.header.type) {
            case VERSION_BITMAP:
                encodeVersionBitmap((HelloElemVersionBitmap) elem, pkt);
                break;
            default:
                // should never get called, but here to keep FindBugs happy
                break;
        }
    }

    /** Encodes a list of hello elements, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the written
     * elements.
     *
     * @param elems the list of hello elements
     * @param pkt the buffer into which the elements are to be written
     */
    public static void encodeElementList(List<HelloElement> elems,
                                         OfPacketWriter pkt) {
        if (elems != null)
            for (HelloElement e: elems)
                encodeElement(e, pkt);
    }


    /** Encodes a version bitmap payload.
     *
     * @param elem the element to encode
     * @param pkt the buffer to write into
     */
    private static void encodeVersionBitmap(HelloElemVersionBitmap elem,
                                            OfPacketWriter pkt) {
        // Implementation note: this will need updating if the protocol
        //  version (wire value) ever exceeds 0x1f (31)
        int bitmap = 0;
        for (ProtocolVersion pv: elem.supportedVersions)
            bitmap |= (1 << pv.code());
        pkt.writeInt(bitmap);
        // Implementation note: no additional padding required for now
        // Since 4-byte header and 4-byte bitmap is divisible by 8 bytes.
    }
}