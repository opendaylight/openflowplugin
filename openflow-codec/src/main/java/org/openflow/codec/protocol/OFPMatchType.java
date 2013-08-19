package org.openflow.codec.protocol;

/**
 * Represents enum ofp_match_type
 *
 * @author AnilGujele
 *
 */
public enum OFPMatchType {
    OFPMT_STANDARD((short) 0), OFPMT_OXM((short) 1);

    private short type;
    private static OFPMatchType[] mapping;

    /**
     * constructor
     *
     * @param value
     */
    private OFPMatchType(short value) {
        type = value;
        addMapping(type, this);

    }

    /**
     * add mapping for match type
     *
     * @param index
     *            - match type value is index
     * @param matchType
     *            - match type instance is value
     */
    private static void addMapping(short index, OFPMatchType matchType) {
        if (null == mapping) {
            mapping = new OFPMatchType[2];
        }
        mapping[index] = matchType;

    }

    /**
     * get match type from value
     *
     * @param value
     * @return
     */
    public static OFPMatchType valueOf(short value) {
        return mapping[value];
    }

    /**
     * get match type value
     *
     * @return
     */
    public short getMatchTypeValue() {
        return type;
    }

    /**
     * set match type value
     *
     * @param type
     */
    public void setMatchTypeValue(short type) {
        this.type = type;
    }
}
