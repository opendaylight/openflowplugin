/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.match.MatchField;

/**
 * Flow action {@code SET_FIELD}.
 *
 * @author Simon Hunt
 */
public class ActSetField extends Action {
    MatchField field;

    /**
     * Constructs an action.
     *
     * @param pv the protocol version
     * @param header the action header
     */
    ActSetField(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",field=").append(field).append("}");
        return sb.toString();
    }

    @Override
    String getActionLabel() {
        return super.getActionLabel() + "." + field.getFieldType();
    }

    /** Returns the details of the field to set.
     *
     * @return the field
     */
    public MatchField getField() {
        return field;
    }

}
