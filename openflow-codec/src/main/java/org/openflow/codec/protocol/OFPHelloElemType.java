package org.openflow.codec.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents struct ofp_hello_elem_type
 *
 * @author AnilGujele
 *
 */
public enum OFPHelloElemType {
    /* Bitmap of version supported. */
    VERSIONBITMAP((short) 1, new Instantiable<OFPHelloElemHeader>() {
        @Override
        public OFPHelloElemHeader instantiate() {
            return new OFPHelloElemVersionBitmap();
        }
    });

    private static Map<Short, OFPHelloElemType> mapping;

    private short type;

    private Instantiable<OFPHelloElemHeader> instantiable;

    /**
     *
     * @param type
     */
    OFPHelloElemType(short type, Instantiable<OFPHelloElemHeader> instantiable) {
        this.setTypeValue(type);
        OFPHelloElemType.addMapping(type, this);
        this.instantiable = instantiable;
    }

    /**
     * add mapping to store
     *
     * @param type
     * @param helloElementType
     */
    private static void addMapping(short type, OFPHelloElemType helloElementType) {
        if (null == mapping) {
            mapping = new HashMap<Short, OFPHelloElemType>();
        }
        mapping.put(type, helloElementType);
    }

    /**
     * get OFHelloElementType correspond to value type
     *
     * @param type
     * @return OFHelloElementType
     */
    public static OFPHelloElemType valueOf(short type) {
        return mapping.get(type);
    }

    /**
     * get HelloElement type value
     *
     * @return
     */
    public short getTypeValue() {
        return type;
    }

    /**
     * set HelloElement type value
     *
     * @param type
     */
    public void setTypeValue(short type) {
        this.type = type;
    }

    /**
     * Returns a new instance of the OFPHelloElemHeader represented by this
     * OFPHelloElemType
     *
     * @return the new object
     */
    public OFPHelloElemHeader newInstance() {
        return instantiable.instantiate();
    }

}
