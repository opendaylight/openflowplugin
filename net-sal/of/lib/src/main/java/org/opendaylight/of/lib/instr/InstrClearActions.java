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
 * Flow Instruction {@code CLEAR_ACTIONS}.
 *
 * @author Simon Hunt
 */
public class InstrClearActions extends InstrAction {
    /**
     * Constructs an instruction.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    InstrClearActions(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    // no actions associated, so don't use superclass implementation.
    @Override
    public String toDebugString() {
        return toString();
    }

}
