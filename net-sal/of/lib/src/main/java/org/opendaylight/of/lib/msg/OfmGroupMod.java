/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;


import org.opendaylight.of.lib.dt.GroupId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.*;

/**
 * Represents an OpenFlow GROUP_MOD message; Since 1.1.
 *
 * @author Simon Hunt
 */
public class OfmGroupMod extends OpenflowMessage {
    GroupModCommand command;
    GroupType groupType;
    GroupId groupId;
    List<Bucket> buckets = new ArrayList<Bucket>();

    /**
     * Constructs an OpenFlow GROUP_MOD message.
     *
     * @param header the message header
     */
    OfmGroupMod(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",cmd=").append(command)
                .append(",typ=").append(groupType)
                .append(",grpId=").append(groupId)
                .append(",#b:").append(cSize(buckets))
                .append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        if (buckets == null || buckets.isEmpty())
            sb.append(EOLI).append(NONE);
        else
            for (Bucket b: buckets)
                sb.append(EOLI).append(b.toDebugString(2));
        return sb.toString();
    }

    /** Returns the group mod command; Since 1.1.
     *
     * @return the group mod command
     */
    public GroupModCommand getCommand() {
        return command;
    }

    /** Returns the group type; Since 1.1.
     *
     * @return the group type
     */
    public GroupType getGroupType() {
        return groupType;
    }

    /** Returns the group ID.
     *
     * @return the group id
     */
    public GroupId getGroupId() {
        return groupId;
    }

    /** Returns the buckets associated with this group.
     *
     * @return the buckets
     */
    public List<Bucket> getBuckets() {
        return buckets == null ? null : Collections.unmodifiableList(buckets);
    }
}
