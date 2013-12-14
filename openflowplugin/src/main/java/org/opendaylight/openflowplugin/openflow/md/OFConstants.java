package org.opendaylight.openflowplugin.openflow.md;

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


}
