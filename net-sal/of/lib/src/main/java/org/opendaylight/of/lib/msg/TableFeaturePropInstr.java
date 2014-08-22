/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.instr.InstrExperimenter;
import org.opendaylight.of.lib.instr.InstructionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.util.StringUtils.spaces;

/**
 * Represents an "Instructions" table feature property. This implementation
 * provides the data as a set of supported instruction types for the standard
 * instructions and, if any are defined, a list of
 * {@link InstrExperimenter experimenter instructions}.
 *
 * @author Simon Hunt
 */
public class TableFeaturePropInstr extends TableFeatureProp {
    Set<InstructionType> supportedInstr;
    List<InstrExperimenter> experInstr;

    /**
     * Constructs a table feature property.
     *
     * @param header the property header
     */
    TableFeaturePropInstr(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        final int len = sb.length();
        sb.replace(len-1, len, ": supported=").append(supportedInstr);
        if (experInstr != null && experInstr.size() > 0)
            sb.append(",exper=").append(experInstr);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int getTotalLength() {
        return header.length;
    }

    @Override
    String toDebugString(int indent) {
        StringBuilder sb = new StringBuilder(super.toDebugString(indent));
        final String indStr = EOLI + spaces(indent + 2);
        sb.append(indStr).append("Supported: ").append(supportedInstr);
        if (experInstr != null && experInstr.size() > 0)
            sb.append(indStr).append("Experimenter: ").append(experInstr);
        return sb.toString();
    }

    /** Returns the set of instruction types supported by this table
     * feature; Since 1.3.
     *
     * @return the set of supported instruction types
     */
    public Set<InstructionType> getSupportedInstructions() {
        return new TreeSet<InstructionType>(supportedInstr);
    }

    /** Returns the list of experimenter instructions supported by this
     * table feature; Since 1.3.
     *
     * @return the list of supported experimenter instructions
     */
    public List<InstrExperimenter> getSupportedExperInstructions() {
        return experInstr == null
                ? null : new ArrayList<InstrExperimenter>(experInstr);
    }
}
