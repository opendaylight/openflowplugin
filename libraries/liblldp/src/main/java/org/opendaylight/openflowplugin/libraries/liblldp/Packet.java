/*
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.libraries.liblldp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class which represents the generic network packet object. It provides
 * the basic methods which are common for all the packets, like serialize and
 * deserialize.
 */
public abstract class Packet {
    private static final Logger LOG = LoggerFactory.getLogger(Packet.class);

    // Access level granted to this packet
    protected final boolean writeAccess;

    // When deserialized from wire, packet could result corrupted
    protected boolean corrupted;

    // The packet that encapsulate this packet
    protected Packet parent;

    // The packet encapsulated by this packet
    protected Packet payload;

    // The unparsed raw payload carried by this packet
    protected byte[] rawPayload;

    // Bit coordinates of packet header fields
    protected Map<String, Pair<Integer, Integer>> hdrFieldCoordMap;

    // Header fields values: Map<FieldName,Value>
    protected Map<String, byte[]> hdrFieldsMap;

    // The class of the encapsulated packet object
    protected Supplier<Packet> payloadFactory;

    public Packet() {
        writeAccess = false;
        corrupted = false;
    }

    public Packet(final boolean writeAccess) {
        this.writeAccess = writeAccess;
        corrupted = false;
    }

    public Packet getParent() {
        return parent;
    }

    public Packet getPayload() {
        return payload;
    }

    public void setParent(final Packet parent) {
        this.parent = parent;
    }

    public void setPayload(final Packet payload) {
        this.payload = payload;
    }

    public void setHeaderField(final String headerField, final byte[] readValue) {
        hdrFieldsMap.put(headerField, readValue);
    }

    /**
     * This method deserializes the data bits obtained from the wire into the
     * respective header and payload which are of type Packet.
     *
     * @param data - data from wire to deserialize
     * @param bitOffset bit position where packet header starts in data
     *        array
     * @param size size of packet in bits
     * @return Packet
     * @throws PacketException if deserialization fails
     */
    public Packet deserialize(final byte[] data, final int bitOffset, final int size)
            throws PacketException {

        // Deserialize the header fields one by one
        int startOffset = 0;
        int numBits = 0;
        for (Entry<String, Pair<Integer, Integer>> pairs : hdrFieldCoordMap
                .entrySet()) {
            String hdrField = pairs.getKey();
            startOffset = bitOffset + getfieldOffset(hdrField);
            numBits = getfieldnumBits(hdrField);

            byte[] hdrFieldBytes;
            try {
                hdrFieldBytes = BitBufferHelper.getBits(data, startOffset,
                        numBits);
            } catch (final BufferException e) {
                throw new PacketException("getBits failed", e);
            }

            /*
             * Store the raw read value, checks the payload type and set the
             * payloadClass accordingly
             */
            setHeaderField(hdrField, hdrFieldBytes);

            if (LOG.isTraceEnabled()) {
                LOG.trace("{}: {}: {} (offset {} bitsize {})", this.getClass().getSimpleName(), hdrField,
                        HexEncode.bytesToHexString(hdrFieldBytes), startOffset, numBits);
            }
        }

        // Deserialize the payload now
        int payloadStart = startOffset + numBits;
        int payloadSize = data.length * NetUtils.NUM_BITS_IN_A_BYTE - payloadStart;

        if (payloadFactory != null) {
            payload = payloadFactory.get();
            payload.deserialize(data, payloadStart, payloadSize);
            payload.setParent(this);
        } else {
            /*
             *  The payload class was not set, it means no class for parsing
             *  this payload is present. Let's store the raw payload if any.
             */
            int start = payloadStart / NetUtils.NUM_BITS_IN_A_BYTE;
            int stop = start + payloadSize / NetUtils.NUM_BITS_IN_A_BYTE;
            rawPayload = Arrays.copyOfRange(data, start, stop);
        }

        // Take care of computation that can be done only after deserialization
        postDeserializeCustomOperation(data, payloadStart - getHeaderSize());

        return this;
    }

    /**
     * This method serializes the header and payload from the respective
     * packet class, into a single stream of bytes to be sent on the wire.
     *
     * @return The byte array representing the serialized Packet
     * @throws PacketException if serialization fails
     */
    public byte[] serialize() throws PacketException {

        // Acquire or compute the serialized payload
        byte[] payloadBytes = null;
        if (payload != null) {
            payloadBytes = payload.serialize();
        } else if (rawPayload != null) {
            payloadBytes = rawPayload;
        }
        int payloadSize = payloadBytes == null ? 0 : payloadBytes.length;

        // Allocate the buffer to contain the full (header + payload) packet
        int headerSize = getHeaderSize() / NetUtils.NUM_BITS_IN_A_BYTE;
        byte[] packetBytes = new byte[headerSize + payloadSize];
        if (payloadBytes != null) {
            System.arraycopy(payloadBytes, 0, packetBytes, headerSize, payloadSize);
        }

        // Serialize this packet header, field by field
        for (Map.Entry<String, Pair<Integer, Integer>> pairs : hdrFieldCoordMap
                .entrySet()) {
            String field = pairs.getKey();
            byte[] fieldBytes = hdrFieldsMap.get(field);
            // Let's skip optional fields when not set
            if (fieldBytes != null) {
                try {
                    BitBufferHelper.copyBitsFromLsb(packetBytes, fieldBytes,
                            getfieldOffset(field), getfieldnumBits(field));
                } catch (final BufferException e) {
                    throw new PacketException("setBytes failed", e);
                }
            }
        }

        // Perform post serialize operations (like checksum computation)
        postSerializeCustomOperation(packetBytes);

        if (LOG.isTraceEnabled()) {
            LOG.trace("packet {}: {}", this.getClass().getSimpleName(),
                    HexEncode.bytesToHexString(packetBytes));
        }

        return packetBytes;
    }

    /**
     * This method gets called at the end of the serialization process It is
     * intended for the child packets to insert some custom data into the output
     * byte stream which cannot be done or cannot be done efficiently during the
     * normal Packet.serialize() path. An example is the checksum computation
     * for IPv4
     *
     * @param myBytes serialized bytes
     * @throws PacketException on failure
     */
    protected void postSerializeCustomOperation(final byte[] myBytes) throws PacketException {
        // no op
    }

    /**
     * This method re-computes the checksum of the bits received on the wire and
     * validates it with the checksum in the bits received Since the computation
     * of checksum varies based on the protocol, this method is overridden.
     * Currently only IPv4 and ICMP do checksum computation and validation. TCP
     * and UDP need to implement these if required.
     *
     * @param data The byte stream representing the Ethernet frame
     * @param startBitOffset The bit offset from where the byte array corresponding to this Packet starts in the frame
     * @throws PacketException on failure
     */
    protected void postDeserializeCustomOperation(final byte[] data, final int startBitOffset) throws PacketException {
        // no op
    }

    /**
     * Gets the header length in bits.
     *
     * @return int the header length in bits
     */
    public int getHeaderSize() {
        int size = 0;
        /*
         * We need to iterate over the fields that were read in the frame
         * (hdrFieldsMap) not all the possible ones described in
         * hdrFieldCoordMap. For ex, 802.1Q may or may not be there
         */
        for (Map.Entry<String, byte[]> fieldEntry : hdrFieldsMap.entrySet()) {
            if (fieldEntry.getValue() != null) {
                String field = fieldEntry.getKey();
                size += getfieldnumBits(field);
            }
        }
        return size;
    }

    /**
     * This method fetches the start bit offset for header field specified by
     * 'fieldname'. The offset is present in the hdrFieldCoordMap of the
     * respective packet class
     *
     * @return Integer - startOffset of the requested field
     */
    public int getfieldOffset(final String fieldName) {
        return hdrFieldCoordMap.get(fieldName).getLeft();
    }

    /**
     * This method fetches the number of bits for header field specified by
     * 'fieldname'. The numBits are present in the hdrFieldCoordMap of the
     * respective packet class
     *
     * @return Integer - number of bits of the requested field
     */
    public int getfieldnumBits(final String fieldName) {
        return hdrFieldCoordMap.get(fieldName).getRight();
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(this.getClass().getSimpleName());
        ret.append(": [");
        for (String field : hdrFieldCoordMap.keySet()) {
            byte[] value = hdrFieldsMap.get(field);
            ret.append(field);
            ret.append(": ");
            ret.append(HexEncode.bytesToHexString(value));
            ret.append(", ");
        }
        ret.replace(ret.length() - 2, ret.length() - 1, "]");
        return ret.toString();
    }

    /**
     * Returns the raw payload carried by this packet in case payload was not
     * parsed. Caller can call this function in case the getPaylod() returns null.
     *
     * @return The raw payload if not parsable as an array of bytes, null otherwise
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public byte[] getRawPayload() {
        return rawPayload;
    }

    /**
     * Set a raw payload in the packet class.
     *
     * @param bytes The raw payload as byte array
     */
    public void setRawPayload(final byte[] bytes) {
        rawPayload = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Return whether the deserialized packet is to be considered corrupted.
     * This is the case when the checksum computed after reconstructing the
     * packet received from wire is not equal to the checksum read from the
     * stream. For the Packet class which do not have a checksum field, this
     * function will always return false.
     *
     *
     * @return true if the deserialized packet's recomputed checksum is not
     *         equal to the packet carried checksum
     */
    public boolean isCorrupted() {
        return corrupted;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + (hdrFieldsMap == null ? 0 : hdrFieldsMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Packet other = (Packet) obj;
        if (hdrFieldsMap == other.hdrFieldsMap) {
            return true;
        }
        if (hdrFieldsMap == null || other.hdrFieldsMap == null) {
            return false;
        }
        for (Entry<String, byte[]> entry : hdrFieldsMap.entrySet()) {
            String field = entry.getKey();
            if (!Arrays.equals(entry.getValue(), other.hdrFieldsMap.get(field))) {
                return false;
            }
        }
        return true;
    }
}
