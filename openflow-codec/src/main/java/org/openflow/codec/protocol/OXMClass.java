package org.openflow.codec.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum to define OXM class type
 *
 * @author AnilGujele
 *
 */
public enum OXMClass {

    NXM_0(0x0000), NXM_1(0x0001), OPENFLOW_BASIC(0x8000), EXPERIMENTER(0xFFFF);

    private static Map<Integer, OXMClass> valueMap;

    private int value;

    /**
     * constructor
     *
     * @param value
     */
    OXMClass(int value) {
        this.value = value;
        addMapping(value, this);
    }

    /**
     * add mapping for OXMClass
     *
     * @param value
     * @param type
     */
    private static void addMapping(int value, OXMClass type) {
        if (null == valueMap) {
            valueMap = new HashMap<Integer, OXMClass>();
        }
        valueMap.put(value, type);
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * get OXMClass for the specific value
     *
     * @param value
     * @return
     */
    public static OXMClass getOXMClass(int value) {
        return valueMap.get((int) value);
    }

}
