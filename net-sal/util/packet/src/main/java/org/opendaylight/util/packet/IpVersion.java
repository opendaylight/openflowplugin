/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 * IP versions.
 *
 * @author Frank Wood
 */
public enum IpVersion implements ProtocolEnum {

    /** Version IPv4. */
    V4(4),
    
    /** Version IPv6. */
    V6(6),
    ;
    
    private int code;
    
    private IpVersion(int code) {
        this.code = code;
    }
    
    @Override
    public int code() {
        return code;
    }
    
    static IpVersion get(int code) {
        return ProtocolUtils.getEnum(IpVersion.class, code, V4);
    }
    
}
