/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.GroupId;

import static org.opendaylight.of.lib.CommonUtils.notNullIncompleteStruct;

/**
 * Represents a group stats request (multipart body); Since 1.1.
 *
 * @author Pramod Shanbhag
 */
public class MBodyGroupStatsRequest extends OpenflowStructure 
        implements MultipartBody {
    private static final int FIXED_LEN = 8;

    GroupId groupId;

    /**
     * Constructs a multipart body GROUP type request.
     *
     * @param pv the protocol version
     */
    public MBodyGroupStatsRequest(ProtocolVersion pv) {
        super(pv);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{groupId:");
        sb.append(groupId).append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Group ID : ").append(groupId);
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(groupId);
    }
    
    @Override
    public int getTotalLength() {
        return FIXED_LEN;
    }
    
    /** Returns the group ID; Since 1.1.
     *
     * @return the group ID
     */
    public GroupId getGroupId() {
        return groupId;
    }
}
