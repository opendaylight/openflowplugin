/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Denotes an instruction, holding just the header information.
 * This class is provided to support the parsing of the "instruction"
 * table feature property.
 *
 * @see org.opendaylight.of.lib.msg.TableFeaturePropInstr
 *
 * @author Simon Hunt
 */
public class InstrHeader extends Instruction {
    /**
     * Constructs an instruction header instance.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    InstrHeader(ProtocolVersion pv, Header header) {
        super(pv, header);
    }
}
