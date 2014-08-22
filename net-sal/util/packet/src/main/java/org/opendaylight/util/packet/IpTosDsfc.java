/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 * IP TOS Differentiated Services Field Codepoints (DSFC) types. 
 *
 * @author Frank Wood
 */
public enum IpTosDsfc implements ProtocolEnum {
    
    /** CS0 type. */
    CS0(0x00),
    
    /** CS1 type. */
    CS1(0x08),
    
    /** CS2 type. */
    CS2(0x10),
    
    /** CS3 type. */
    CS3(0x18),
    
    /** CS4 type. */
    CS4(0x20),
    /** CS5 type. */
    CS5(0x28),
    
    /** CS6 type. */
    CS6(0x30),
    
    /** CS7 type. */
    CS7(0x38),
    
    /** AF11 type. */
    AF11(0x0a),
    
    /** AF12 type. */
    AF12(0x0c),
    
    /** AF13 type. */
    AF13(0x0e),
    
    /** AF21 type. */
    AF21(0x12),
    
    /** AF22 type. */
    AF22(0x14),
    
    /** AF23 type. */
    AF23(0x16),
    
    /** AF31 type. */
    AF31(0x1a),
    
    /** AF32 type. */
    AF32(0x1c),
    
    /** AF33 type. */
    AF33(0x1e),
    
    /** AF41 type. */
    AF41(0x22),
    
    /** AF42 type. */
    AF42(0x24),
    
    /** AF43 type. */
    AF43(0x26),
    
    /** EF_PHB type. */
    EF_PHB(0x23),
    
    /** Voice Admit type. */
    VOICE_ADMIT(0x2c),
    ;
    
    private int code;
    
    private IpTosDsfc(int code) {
        this.code = code;
    }
    
    @Override
    public int code() {
        return code;
    }
    
    static IpTosDsfc get(int code) {
        return ProtocolUtils.getEnum(IpTosDsfc.class, code, CS0);
    }

}
