/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import com.google.common.base.MoreObjects;
import java.math.BigInteger;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Nxm header.
 *
 * @author msunal
 */
public final class NxmHeader {

    // Full 4 or 8 byte header as big integer
    private final Uint64 header;
    // Full 4 or 8 byte header as long
    private final long headerAsLong;
    // 4 byte class, field, length as long
    private final long shortHeader;
    private final int oxmClass;
    private final int nxmField;
    private final boolean hasMask;
    private final int length;
    private final long experimenterId;

    /**
     * Builds a {@code NxmHeader} from a {@code BigInteger} representation,
     * that is, one for which {@link BigInteger#longValue()} produces
     * a valid long representation.
     *
     * @param header the header as {@code BigInteger}.
     * @see NxmHeader#NxmHeader(long)
     */
    public NxmHeader(final Uint64 header) {
        headerAsLong = header.longValue();
        if (isExperimenter(header)) {
            experimenterId = (int) headerAsLong;
            shortHeader = headerAsLong >>> 32;
        } else {
            shortHeader = headerAsLong;
            experimenterId = -1;
        }

        this.header = header;
        try {
            oxmClass = Math.toIntExact(extractSub(shortHeader, 16, 16));
            nxmField = Math.toIntExact(extractSub(shortHeader, 7, 9));
            hasMask = extractSub(shortHeader, 1, 8) == 1;
            length = Math.toIntExact(extractSub(shortHeader, 8, 0));
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Builds a {@code NxmHeader} from a {@code long} representation.
     * For non experimenter, the 4 byte header are the least
     * significant of the long, being the other 4 most significant
     * bytes 0. For experimenter, the full 8 byte constitute the header,
     * being the 4 least significant the experimenter id.
     *
     * @param header the header as a {@code long}.
     */
    public NxmHeader(final long header) {
        this(Uint64.fromLongBits(header));
    }

    /**
     * Build a non experimenter header from it's constituent fields.
     *
     * @param oxmClass the OXM class.
     * @param nxmField the NXM field.
     * @param hasMask the hasMask field.
     * @param length the length field.
     */
    public NxmHeader(final int oxmClass, final int nxmField, final boolean hasMask, final int length) {
        this(oxmClass, nxmField, hasMask, length, -1);
    }

    /**
     * Build a experimenter header from it's constituent fields.
     * The OXM class will be set to 0xFFFF.
     *
     * @param nxmField the NXM field.
     * @param hasMask the hasMask field.
     * @param length the length field.
     * @param experimenterId the esperimenter id field.
     */
    public NxmHeader(final int nxmField, final boolean hasMask, final int length, final long experimenterId) {
        this(EncodeConstants.EXPERIMENTER_VALUE, nxmField, hasMask, length, experimenterId);
    }

    private NxmHeader(final int oxmClass, final int nxmField, final boolean hasMask, final int length,
            final long experimenterId) {
        this.oxmClass = oxmClass;
        this.nxmField = nxmField;
        this.hasMask = hasMask;
        this.length = length;
        shortHeader = (long) oxmClass << 16 | nxmField << 9 | (hasMask ? 1 : 0) << 8 | length;
        this.experimenterId = experimenterId;
        if (isExperimenter()) {
            header = Uint64.fromLongBits((shortHeader << 32) + experimenterId);
        } else {
            header = Uint64.valueOf(shortHeader);
        }
        headerAsLong = header.longValue();
    }

    private static long extractSub(final long value, final int nrBits, final int offset) {
        final long rightShifted = value >>> offset;
        final long mask = (1L << nrBits) - 1L;
        return rightShifted & mask;
    }

    /**
     * Returns the {@code Uint64} representation of the header.
     *
     * @return the header.
     * @see NxmHeader#NxmHeader(Uint64)
     */
    public Uint64 toUint64() {
        return header;
    }

    /**
     * Returns the {@code long} representation of the header.
     *
     * @return the header.
     * @see NxmHeader#NxmHeader(long)
     */
    public long toLong() {
        return headerAsLong;
    }

    public int getOxmClass() {
        return oxmClass;
    }

    public int getNxmField() {
        return nxmField;
    }

    public boolean isHasMask() {
        return hasMask;
    }

    public int getLength() {
        return length;
    }

    public long getExperimenterId() {
        return experimenterId;
    }

    public boolean isExperimenter() {
        return oxmClass == EncodeConstants.EXPERIMENTER_VALUE;
    }

    public static boolean isExperimenter(final Uint64 uint) {
        return uint.longValue() >>> 48 == EncodeConstants.EXPERIMENTER_VALUE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortHeader, experimenterId);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NxmHeader other = (NxmHeader) obj;
        return shortHeader == other.shortHeader
                && experimenterId == other.experimenterId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("headerAsLong", headerAsLong)
                .add("shortHeader", shortHeader)
                .add("oxmClass", oxmClass)
                .add("oxmField", nxmField)
                .add("hasMask", hasMask)
                .add("length", length)
                .add("experimenterId", experimenterId)
                .toString();
    }
}
