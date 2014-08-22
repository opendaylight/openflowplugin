/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.instr.ActExperimenter;
import org.opendaylight.of.lib.instr.ActionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.util.StringUtils.spaces;

/**
 * Represents an "actions" table feature property. This implementation provides
 * the data as a set of supported action types for the standard actions and,
 * if any are defined, a list of {@link ActExperimenter experimenter actions}.
 *
 * @author Simon Hunt
 */
public class TableFeaturePropAction extends TableFeatureProp {
    Set<ActionType> supportedActions;
    List<ActExperimenter> experActions;

    /**
     * Constructs a table feature property.
     *
     * @param header the property header
     */
    TableFeaturePropAction(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        final int len = sb.length();
        sb.replace(len-1, len, ": supported=").append(supportedActions);
        if (experActions != null && experActions.size() > 0)
            sb.append(",exper=").append(experActions);
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
        sb.append(indStr).append("Supported: ").append(supportedActions);
        if (experActions != null && experActions.size() > 0)
            sb.append(indStr).append("Experimenter: ").append(experActions);
        return sb.toString();
    }

    /** Returns the set of action types supported by the table feature;
     * Since 1.3.
     *
     * @return the set of supported action types
     */
    public Set<ActionType> getSupportedActions() {
        return new TreeSet<ActionType>(supportedActions);
    }

    /** Returns the list of experimenter actions supported by this
     * table feature; Since 1.3.
     *
     * @return the list of experimenter actions
     */
    public List<ActExperimenter> getSupportedExperActions() {
        return experActions == null
                ? null : new ArrayList<ActExperimenter>(experActions);
    }

}
