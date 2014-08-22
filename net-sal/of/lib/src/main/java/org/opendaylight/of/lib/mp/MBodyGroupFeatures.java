/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.msg.GroupCapability;
import org.opendaylight.of.lib.msg.GroupType;
import org.opendaylight.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.EOLI;

/**
 * Represents a group features element; part of a reply to a group features
 * request multipart message; since 1.2.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class MBodyGroupFeatures extends OpenflowStructure
        implements MultipartBody {
    private static final int TOTAL_LENGTH = 40;

    Set<GroupType> types;
    Set<GroupCapability> capabilities;
    Map<GroupType, Long> maxGroups;
    Map<GroupType, Set<ActionType>> actions;

    /**
     * Constructs a multipart body <em>GroupFeatures</em> type.
     *
     * @param pv the protocol version
     */
    public MBodyGroupFeatures(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public int getTotalLength() {
        return TOTAL_LENGTH;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{gfeats:types=").append(types).append(",caps=")
                .append(capabilities).append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString()  {
        return toDebugString(0);
    }

    /** Returns a multi-line representation of this Group Features object.
     *
     * @param indent the additional indent (number of spaces)
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder(toString());
        sb.append(in).append("Types : ").append(types)
                .append(in).append("Capabilities : ").append(capabilities)
                .append(in).append("Max Groups : ")
                    .append(maxGroupsDebugString(indent + 2))
                .append(in).append("Actions : ")
                    .append(actionsDebugString(indent + 2));
        return  sb.toString();
    }

    private String maxGroupsDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<GroupType, Long> entry: maxGroups.entrySet())
            sb.append(in).append(entry);
        return sb.toString();
    }

    private String actionsDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<GroupType, Set<ActionType>> entry: actions.entrySet())
            sb.append(in).append(entry);
        return sb.toString();
    }

    //=========================================================== GETTERS

    /**
     * Returns the set of supported group types; since 1.2.
     *
     * @return the set of supported group types
     */
    public Set<GroupType> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    /**
     * Returns the set of supported group capabilities; since 1.2.
     *
     * @return the set of group capabilities
     */
    public Set<GroupCapability> getCapabilities() {
        return Collections.unmodifiableSet(capabilities);
    }

    /**
     * Returns the maximum number of groups for the given group type; since 1.2.
     *
     * @param type the group type
     * @return the maximum number of supported groups
     */
    public long getMaxGroupsForType(GroupType type) {
        return maxGroups.get(type);
    }

    /**
     * Returns the set of supported actions for the given group type; since 1.2.
     *
     * @param type the group type
     * @return the set of supported actions
     */
    public Set<ActionType> getActionsForType(GroupType type) {
        return Collections.unmodifiableSet(actions.get(type));
    }
}
