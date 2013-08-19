/**
 *
 */
package org.openflow.codec.protocol.queue;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.openflow.codec.protocol.Instantiable;
import org.openflow.codec.util.U16;

/**
 * List of OpenFlow Queue Property types and mappings to wire protocol value and
 * derived classes
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPQueuePropertyType {

    public static OFPQueuePropertyType MIN_RATE = new OFPQueuePropertyType(1, "MIN_RATE",
            OFPQueuePropertyMinRate.class, new Instantiable<OFPQueueProperty>() {
                @Override
                public OFPQueueProperty instantiate() {
                    return new OFPQueuePropertyMinRate();
                }
            });
    public static OFPQueuePropertyType MAX_RATE = new OFPQueuePropertyType(2, "MAX_RATE",
            OFPQueuePropertyMaxRate.class, new Instantiable<OFPQueueProperty>() {
                @Override
                public OFPQueueProperty instantiate() {
                    return new OFPQueuePropertyMaxRate();
                }
            });
    public static OFPQueuePropertyType EXPERIMENTER = new OFPQueuePropertyType(0xffff, "EXPERIMENTER",
            OFPQueuePropertyExperimenter.class, new Instantiable<OFPQueueProperty>() {
                @Override
                public OFPQueueProperty instantiate() {
                    return new OFPQueuePropertyExperimenter();
                }
            });

    protected static Map<Integer, OFPQueuePropertyType> mapping;

    protected Class<? extends OFPQueueProperty> clazz;
    protected Constructor<? extends OFPQueueProperty> constructor;
    protected Instantiable<OFPQueueProperty> instantiable;
    protected int minLen;
    protected String name;
    protected short type;

    /**
     * Store some information about the OpenFlow Queue Property type, including
     * wire protocol type number, length, and derived class
     *
     * @param type
     *            Wire protocol number associated with this OFPQueuePropertyType
     * @param name
     *            The name of this type
     * @param clazz
     *            The Java class corresponding to this type of OpenFlow Queue
     *            Property
     * @param instantiable
     *            the instantiable for the OFPQueueProperty this type represents
     */
    OFPQueuePropertyType(int type, String name, Class<? extends OFPQueueProperty> clazz,
            Instantiable<OFPQueueProperty> instantiable) {
        this.type = (short) type;
        this.name = name;
        this.clazz = clazz;
        this.instantiable = instantiable;
        try {
            this.constructor = clazz.getConstructor(new Class[] {});
        } catch (Exception e) {
            throw new RuntimeException("Failure getting constructor for class: " + clazz, e);
        }
        OFPQueuePropertyType.addMapping(type, this);
    }

    /**
     * Adds a mapping from type value to OFPQueuePropertyType enum
     *
     * @param i
     *            OpenFlow wire protocol Action type value
     * @param t
     *            type
     */
    static public void addMapping(int i, OFPQueuePropertyType t) {
        if (mapping == null)
            mapping = new HashMap<Integer, OFPQueuePropertyType>();
        OFPQueuePropertyType.mapping.put(i, t);
    }

    /**
     * Given a wire protocol OpenFlow type number, return the OFPType associated
     * with it
     *
     * @param i
     *            wire protocol number
     * @return OFPType enum type
     */

    static public OFPQueuePropertyType valueOf(short i) {
        return OFPQueuePropertyType.mapping.get(U16.f(i));
    }

    /**
     * @return Returns the wire protocol value corresponding to this
     *         OFPQueuePropertyType
     */
    public short getTypeValue() {
        return this.type;
    }

    /**
     * @return return the OFPQueueProperty subclass corresponding to this
     *         OFPQueuePropertyType
     */
    public Class<? extends OFPQueueProperty> toClass() {
        return clazz;
    }

    /**
     * Returns the no-argument Constructor of the implementation class for this
     * OFPQueuePropertyType
     *
     * @return the constructor
     */
    public Constructor<? extends OFPQueueProperty> getConstructor() {
        return constructor;
    }

    /**
     * Returns a new instance of the OFPQueueProperty represented by this
     * OFPQueuePropertyType
     *
     * @return the new object
     */
    public OFPQueueProperty newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<OFPQueueProperty> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable
     *            the instantiable to set
     */
    public void setInstantiable(Instantiable<OFPQueueProperty> instantiable) {
        this.instantiable = instantiable;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
