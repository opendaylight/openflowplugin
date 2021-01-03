/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.eric.codec.match;

import com.google.common.primitives.Ints;

/**
 * Eric header.
 */
public class EricHeader {

    private final long headerAsLong;
    private final int oxmClass;
    private final int ericField;
    private final boolean hasMask;
    private final int length;

    public EricHeader(final long header) {
        this.headerAsLong = header;
        this.oxmClass = Ints.checkedCast(extractSub(header, 16, 16));
        this.ericField = Ints.checkedCast(extractSub(header, 7, 9));
        this.hasMask = extractSub(header, 1, 8) == 1;
        this.length = Ints.checkedCast(extractSub(header, 8, 0));
    }

    public EricHeader(final int oxmClass, final int ericField, final boolean hasMask, final int length) {
        this.oxmClass = oxmClass;
        this.ericField = ericField;
        this.hasMask = hasMask;
        this.length = length;
        this.headerAsLong = (long) oxmClass << 16 | ericField << 9 | (hasMask ? 1 : 0) << 8 | length;
    }

    private static long extractSub(final long value, final int nrBits, final int offset) {
        final long rightShifted = value >>> offset;
        final long mask = (1L << nrBits) - 1L;
        return rightShifted & mask;
    }

    public long toLong() {
        return headerAsLong;
    }

    public int getOxmClass() {
        return oxmClass;
    }

    public int getEricField() {
        return ericField;
    }

    public boolean isHasMask() {
        return hasMask;
    }

    public int getLength() {
        return length;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (headerAsLong ^ headerAsLong >>> 32);
        return result;
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
        EricHeader other = (EricHeader) obj;
        if (headerAsLong != other.headerAsLong) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EricHeader [headerAsLong=" + headerAsLong + ", oxmClass=" + oxmClass + ", ericField=" + ericField
                 + ", hasMask=" + hasMask + ", length=" + length + "]";
    }

}