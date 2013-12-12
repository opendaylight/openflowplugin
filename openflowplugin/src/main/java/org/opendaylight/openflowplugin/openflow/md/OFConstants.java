package org.opendaylight.openflowplugin.openflow.md;

import java.math.BigInteger;

/**
 * OFP related constants
 */
public class OFConstants {

    /** reserved port: process with normal L2/L3 switching  */
    public static final short OFPP_NORMAL = ((short)0xfffa);
    /** reserved port: all physical ports except input port  */
    public static final short OFPP_ALL  = ((short)0xfffc);
    /** reserved port: local openflow port  */
    public static final short OFPP_LOCAL = ((short)0xfffe);
    
    
    /** openflow protocol 1.0 - version identifier */
    public static final short OFP_VERSION_1_0 = 0x01;
    /** openflow protocol 1.3 - version identifier */
    public static final short OFP_VERSION_1_3 = 0x04;
    
    public static final Short OFPTT_ALL = 0xff;
    public static final Long ANY = Long.parseLong("ffffffff", 16);
    public static final Long OFPP_ANY = ANY;
    public static final Long OFPG_ANY = ANY;
    public static final Long OFPQ_ANY = ANY;
    public static final BigInteger DEFAULT_COOKIE = BigInteger.ZERO;
    public static final BigInteger DEFAULT_COOKIE_MASK = BigInteger.ZERO;
    public static final Long OFP_NO_BUFFER = 0xffffffffL;
}
