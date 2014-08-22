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
import java.util.Set;
import java.util.TreeSet;

/**
 * Flow Instruction {@code WRITE_ACTIONS}.
 *
 * @author Simon Hunt
 */
public class InstrWriteActions extends InstrAction {
    /**
     * Constructs an instruction.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    InstrWriteActions(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",actSet=").append(actionList()).append("}");
        return sb.toString();
    }

    /** Returns the set of actions defined
     * in the flow instruction.
     *
     * @return the list of actions
     */
    public Set<Action> getActionSet() {
        return Collections.unmodifiableSet(new TreeSet<Action>(actions));
    }
}
