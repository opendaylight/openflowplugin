/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;

import java.util.Collections;
import java.util.List;

/**
 * Flow Instruction {@code APPLY_ACTIONS}.
 *
 * @author Simon Hunt
 */
public class InstrApplyActions extends InstrAction {
    /**
     * Constructs an instruction.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    InstrApplyActions(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",actList=").append(actionList()).append("}");
        return sb.toString();
    }

    /** Returns the list of actions, in the order they were defined
     * in the flow instruction structure.
     *
     * @return the list of actions
     */
    public List<Action> getActionList() {
        return actions == null ? null : Collections.unmodifiableList(actions);
    }

}
