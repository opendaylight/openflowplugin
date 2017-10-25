/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import com.google.common.primitives.Ints;

/**
 * @author msunal
 *
 */
public class NxmHeader {

    private final long headerAsLong;
    private final int oxmClass;
    private final int nxmField;
    private final boolean hasMask;
    private final int length;

    public NxmHeader(long header) {
        this.headerAsLong = header;
        this.oxmClass = Ints.checkedCast(extractSub(header, 16, 16));
        this.nxmField = Ints.checkedCast(extractSub(header, 7, 9));
        this.hasMask = extractSub(header, 1, 8) == 1 ? true : false;
        this.length = Ints.checkedCast(extractSub(header, 8, 0));
    }

    public NxmHeader(int oxmClass, int nxmField, boolean hasMask, int length) {
        this.oxmClass = oxmClass;
        this.nxmField = nxmField;
        this.hasMask = hasMask;
        this.length = length;
        this.headerAsLong = ((((long) oxmClass) << 16) | (nxmField << 9) | ((hasMask ? 1 : 0) << 8) | (length));
    }

    private static long extractSub(final long l, final int nrBits, final int offset) {
        final long rightShifted = l >>> offset;
        final long mask = (1L << nrBits) - 1L;
        return rightShifted & mask;
    }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (headerAsLong ^ (headerAsLong >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
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
        if (headerAsLong != other.headerAsLong) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NxmHeader [headerAsLong=" + headerAsLong + ", oxmClass=" + oxmClass + ", nxmField=" + nxmField
                + ", hasMask=" + hasMask + ", length=" + length + "]";
    }

}
