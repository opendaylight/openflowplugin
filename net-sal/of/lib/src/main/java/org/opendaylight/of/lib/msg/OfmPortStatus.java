/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.NULL_REP;

/**
 * Represents an OpenFlow PORT_STATUS message; Since 1.0.
 *
 * @author Radhika Hegde
 */
public class OfmPortStatus extends OpenflowMessage {

    /** Encapsulates what changed about the physical port; Since 1.0. */
    PortReason reason;
    /** Port whose status changed; Since 1.0. */
    Port desc;
    
    /** 
     * Constructs an OpenFlow PORT_STATUS message.
     *
     * @param header the message header
     */
    OfmPortStatus(Header header) {
        super(header);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",port=").append(desc)
                .append(",status=").append(reason).append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        // TODO: When this is called from verifyMutableHeader
        // the basic mutable object returned from the message factory does not have 
        // port description or name yet. May be this method needs to be overridden 
        // in the mutable version
        sb.append(EOLI).append("Port   : ").append(desc == null ? NULL_REP : desc.toDebugString(2))
          .append(EOLI).append("Status : ").append(reason);
        return sb.toString();
    }
    
    /** Returns the port whose status changed; Since 1.0.
    *
    * @return the port 
    */
    public Port getPort() {
       return desc;
    }
    
    /** Returns the constant indicating status change; Since 1.0.
    *
    * @return the port reason
    */
    public PortReason getReason() {
       return reason;
    }    
}