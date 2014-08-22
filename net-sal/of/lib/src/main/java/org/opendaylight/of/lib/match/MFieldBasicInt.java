/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Abstract OXM Basic match field superclass, holding an integer payload.
 *
 * @author Simon Hunt
 */
public abstract class MFieldBasicInt extends MFieldBasic {
    int value;
    int mask;

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MFieldBasicInt(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    /** Returns the payload value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /** Returns the payload mask.
     *
     * @return the mask
     */
    public int getMask() {
        return mask;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",").append(valueLabel()).append("=")
                .append(formatValue(value));
        if (header.hasMask)
            sb.append(",mask=").append(formatValue(mask));
        sb.append("}");
        return sb.toString();
    }

    /** Subclasses should return the text for the label of the value field
     * to be used in toString().
     *
     * @return the value field label
     */
    abstract String valueLabel();

    /** Subclasses can override this method if they want the value
     * displayed as some form other than decimal int.
     *
     * @param v value to be formated
     * @return the value formatted
     */
    String formatValue(int v) {
        return String.valueOf(v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MFieldBasicInt that = (MFieldBasicInt) o;
        return header.equals(that.header) && value == that.value &&
                mask == that.mask;
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + value;
        result = 31 * result + mask;
        return result;
    }
}
