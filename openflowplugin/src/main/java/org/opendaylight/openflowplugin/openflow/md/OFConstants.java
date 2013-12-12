package org.opendaylight.openflowplugin.openflow.md;

public class OFConstants {

    public static final short OFPP_NORMAL = ((short)0xfffa);
    public static final short OFPP_ALL  = ((short)0xfffc);
    public static final short OFPP_LOCAL = ((short)0xfffe);
    
    
    //Constant for switch feature capabilities, when adding node
    /**
     * Switch feature capability for flow statistics
     */
    public static final String OFPC_FLOW_STATS = "Flow statistics";
    /**
     * Switch feature capability for table statistics
     */
    public static final String OFPC_TABLE_STATS = "Table statistics";
    /**
     * Switch feature capability for port statistics
     */
    public static final String OFPC_PORT_STATS = "Port statistics";
    /**
     * Switch feature capability for stp
     */
    public static final String OFPC_STP = "802.1d spanning tree";
    /**
     * Reserved bit
     */
    public static final String OFPC_RESERVED = "Reserved, must be zero";
    /**
     * Switch feature capability for reassembling IP fragments
     */
    public static final String OFPC_IP_REASM = "Can reassemble IP fragments";
    /**
     * Switch feature capability for queue statistics
     */
    public static final String OFPC_QUEUE_STATS = "Queue statistics";
    /**
     * Switch feature capability for matching IP addresses in ARP packets
     */
    public static final String OFPC_ARP_MATCH_IP = "Match IP addresses in ARP pkts";
    /**
     * Switch feature capability for group statistics
     */
    public static final String OFPC_GROUP_STATS = "Group statistics";
    /**
     * Switch feature capability for blocking looping ports
     */
    public static final String OFPC_PORT_BLOCKED = "Switch will block looping ports";
    

}
