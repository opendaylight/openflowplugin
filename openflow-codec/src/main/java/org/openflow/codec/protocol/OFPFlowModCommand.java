package org.openflow.codec.protocol;

/**
 * Correspond to enum ofp_flow_mod_command
 *
 * @author AnilGujele
 *
 */
public enum OFPFlowModCommand {
    /* New flow. */
    OFPFC_ADD((byte) 0),
    /* Modify all matching flows. */
    OFPFC_MODIFY((byte) 1),
    /* Modify entry strictly matching wildcards and priority. */
    OFPFC_MODIFY_STRICT((byte) 2),
    /* Delete all matching flows. */
    OFPFC_DELETE((byte) 3),
    /* Delete entry strictly matching wildcards and priority. */
    OFPFC_DELETE_STRICT((byte) 4);

    private byte value;

    private static OFPFlowModCommand[] mapping;

    OFPFlowModCommand(byte value) {
        this.value = value;
        addMapping(value, this);
    }

    /**
     * add mapping
     *
     * @param key
     * @param value
     */
    private static void addMapping(byte index, OFPFlowModCommand value) {
        if (null == mapping) {
            mapping = new OFPFlowModCommand[5];
        }
        mapping[index] = value;

    }

    /**
     * get the enum having this value
     *
     * @param value
     * @return
     */
    public static OFPFlowModCommand valueOf(byte value) {
        return mapping[value];
    }

    /**
     *
     * @return
     */
    public byte getValue() {
        return value;
    }

    /**
     *
     * @param value
     */
    public void setValue(byte value) {
        this.value = value;
    }

}
