/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Abstract Flow action for those with a U32 payload.
 *
 * @author Simon Hunt
 */
abstract class ActionU32 extends Action {
    long value;

    /**
     * Constructs an action.
     *
     * @param pv the protocol version
     * @param header the action header
     */
    ActionU32(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",").append(valueLabel()).append("=")
                .append(formatValue(value)).append("}");
        return sb.toString();
    }

    /** Subclasses should return the text for the label of the value field
     * to be used in toString().
     *
     * @return the value field label
     */
    abstract String valueLabel();

    /** Subclasses can override this method if they want the value
     * displayed as some form other than hex long.
     *
     * @param v long value to be formatted
     * @return the value formatted
     */
    String formatValue(long v) {
        return hex(v);
    }

}
