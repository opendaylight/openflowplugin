/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 * IP TOS Explicit Congestion Notification (ECN) types.
 *
 * @author Frank Wood
 */
public enum IpTosEcn implements ProtocolEnum {
        
    /** Not ECT-Capable Transport. */
    NOT_ECT(0x00),
    
    /** ECT-Capable Transport 1. */
    ECT_1(0x01),
    
    /** ECT_Capable Transport 0. */
    ECT_0(0x02),
    
    /** Congestion Experienced. */
    CE(0x03)
    ;
    
    private int code;
    
    private IpTosEcn(int code) {
        this.code = code;
    }
    
    @Override
    public int code() { return code; }
    
    static IpTosEcn get(int code) {
        return ProtocolUtils.getEnum(IpTosEcn.class, code, NOT_ECT);
    }
    
}
