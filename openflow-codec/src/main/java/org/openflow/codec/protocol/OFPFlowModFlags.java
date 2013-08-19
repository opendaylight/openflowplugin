package org.openflow.codec.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Correspond to enum ofp_flow_mod_flags
 *
 * @author AnilGujele
 *
 */
public enum OFPFlowModFlags {
    /* Send flow removed message when flow expires or is deleted. */
    OFPFF_SEND_FLOW_REM(1 << 0),
    /* Check for overlapping entries first. */
    OFPFF_CHECK_OVERLAP(1 << 1),
    /* Reset flow packet and byte counts. */
    OFPFF_RESET_COUNTS(1 << 2),
    /* Don't keep track of packet count. */
    OFPFF_NO_PKT_COUNTS(1 << 3),
    /* Don't keep track of byte count. */
    OFPFF_NO_BYT_COUNTS(1 << 4);

    private short value;

    private static Map<Integer, OFPFlowModFlags> mapping;

    OFPFlowModFlags(int value) {
        addMapping(value, this);
        this.setValue((short) value);
    }

    /**
     * add mapping
     *
     * @param key
     * @param value
     */
    private static void addMapping(Integer key, OFPFlowModFlags value) {
        if (null == mapping) {
            mapping = new HashMap<Integer, OFPFlowModFlags>();
        }
        mapping.put(key, value);

    }

    /**
     * get the enum having this value
     *
     * @param value
     * @return
     */
    public static OFPFlowModFlags valueOf(short value) {
        return mapping.get(value);
    }

    /**
     *
     * @return
     */
    public short getValue() {
        return value;
    }

    /**
     *
     * @param value
     */
    public void setValue(short value) {
        this.value = value;
    }

}
