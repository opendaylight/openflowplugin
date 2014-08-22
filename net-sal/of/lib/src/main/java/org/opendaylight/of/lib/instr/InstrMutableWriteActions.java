/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Mutable version of {@link InstrWriteActions}.
 *
 * @author Simon Hunt
 */
public class InstrMutableWriteActions extends InstrMutableAction {
    /**
     * Constructs a mutable write actions instruction.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    InstrMutableWriteActions(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    protected void validateActions() {
        // TODO ...
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        InstrWriteActions act = new InstrWriteActions(version, header);
        act.actions.addAll(this.actions);
        return act;
    }
}
