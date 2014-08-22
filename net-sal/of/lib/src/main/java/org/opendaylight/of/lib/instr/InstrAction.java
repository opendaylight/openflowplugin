/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.util.StringUtils.EOL;
import static org.opendaylight.util.StringUtils.spaces;

/**
 * Abstract base class for action-list-based Flow Instructions.
 *
 * @author Simon Hunt
 */
abstract class InstrAction extends Instruction {
    final List<Action> actions;

    /**
     * Constructs an instruction.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    InstrAction(ProtocolVersion pv, Header header) {
        super(pv, header);
        actions = new ArrayList<Action>();
    }


    /** Returns a comma delimited list of action type names.
     *
     * @return action type names
     */
    String actionList() {
        if (actions.size() == 0)
            return "(none)";

        StringBuilder sb = new StringBuilder();
        for (Action a: actions)
            sb.append(a.getActionLabel()).append(",");
        int len = sb.length();
        sb.replace(len - 1, len, "");
        return sb.toString();
    }


    @Override
    public String toDebugString(int indent) {
        String istr = spaces(indent);
        String istr2 = spaces(indent + 4);
        StringBuilder sb = new StringBuilder(istr).append(toString());
        for (Action a: actions)
            sb.append(EOL).append(istr2).append(a);
        return sb.toString();
    }
}
