/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.IncompleteMessageException;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.notNullIncompleteMsg;

/**
 * Base class for OfmRoleRequest and OfmRoleReply.
 *
 * @author Pramod Shanbhag
 */
public abstract class Role extends OpenflowMessage {
    ControllerRole role;
    long generationId;

    /**
     * Base constructor for the Role.
     *
     * @param header the message header
     */
    Role(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",role=").append(role)
                .append(",genID=").append(hex(generationId))
                .append("}");
        return sb.toString();
    }
    
    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        sb.append(EOLI).append("Controller role : ").append(role)
            .append(EOLI).append("Master election generation ID : ")
            .append(hex(generationId));
        return sb.toString();
    }
    
    @Override
    public void validate() throws IncompleteMessageException {
        notNullIncompleteMsg(role);
    }
    
    /** Returns the controller role; Since 1.2.
     *
     * @return the controller role
     */
    public ControllerRole getRole() {
        return role;
    }
    
    /** Returns master election generation ID; Since 1.2.
     *
     * @return the generation ID
     */
    public long getGenerationId() {
        return generationId;
    }
}
