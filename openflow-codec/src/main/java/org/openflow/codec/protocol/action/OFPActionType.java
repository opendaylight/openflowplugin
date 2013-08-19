/**
 *
 */
package org.openflow.codec.protocol.action;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.openflow.codec.protocol.Instantiable;

/**
 * List of OpenFlow Action types and mappings to wire protocol value and derived
 * classes
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com)
 */
public enum OFPActionType implements Serializable {
    OUTPUT(0, OFPActionOutput.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionOutput();
        }
    }), COPY_TTL_OUT(11, OFPActionCopyTimeToLiveOut.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionCopyTimeToLiveOut();
        }
    }), COPY_TTL_IN(12, OFPActionCopyTimeToLiveIn.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionCopyTimeToLiveIn();
        }
    }), SET_MPLS_TTL(15, OFPActionMplsTimeToLive.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionMplsTimeToLive();
        }
    }), DEC_MPLS_TTL(16, OFPActionDecMplsTimeToLive.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionDecMplsTimeToLive();
        }
    }), PUSH_VLAN(17, OFPActionPushVLAN.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionPushVLAN();
        }
    }), POP_VLAN(18, OFPActionPopVLAN.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionPopVLAN();
        }
    }), PUSH_MPLS(19, OFPActionPushMpls.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionPushMpls();
        }
    }), POP_MPLS(20, OFPActionPopMpls.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionPopMpls();
        }
    }), SET_QUEUE(21, OFPActionSetQueue.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionSetQueue();
        }
    }), GROUP(22, OFPActionGroup.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionGroup();
        }
    }), SET_NW_TTL(23, OFPActionNetworkTimeToLive.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionNetworkTimeToLive();
        }
    }), DEC_NW_TTL(24, OFPActionDecNetworkTimeToLive.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionDecNetworkTimeToLive();
        }
    }), SET_FIELD(25, OFPActionSetField.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionSetField();
        }
    }), PUSH_PBB(26, OFPActionPushPbb.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionPushPbb();
        }
    }), POP_PBB(27, OFPActionPopPbb.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionPopPbb();
        }
    }),

    EXPERIMENTER(0xffff, OFPActionExperimenterHeader.class, new Instantiable<OFPAction>() {
        @Override
        public OFPAction instantiate() {
            return new OFPActionExperimenterHeader();
        }
    });

    protected static HashMap<Short, OFPActionType> mapping;

    protected Class<? extends OFPAction> clazz;
    protected Constructor<? extends OFPAction> constructor;
    protected Instantiable<OFPAction> instantiable;
    protected int minLen;
    protected short type;

    /**
     * Store some information about the OpenFlow Action type, including wire
     * protocol type number, length, and derrived class
     *
     * @param type
     *            Wire protocol number associated with this OFPType
     * @param clazz
     *            The Java class corresponding to this type of OpenFlow Action
     * @param instantiable
     *            the instantiable for the OFPAction this type represents
     */
    OFPActionType(int type, Class<? extends OFPAction> clazz, Instantiable<OFPAction> instantiable) {
        this.type = (short) type;
        this.clazz = clazz;
        this.instantiable = instantiable;
        try {
            this.constructor = clazz.getConstructor(new Class[] {});
        } catch (Exception e) {
            throw new RuntimeException("Failure getting constructor for class: " + clazz, e);
        }
        OFPActionType.addMapping(this.type, this);
    }

    /**
     * Adds a mapping from type value to OFPActionType enum
     *
     * @param i
     *            OpenFlow wire protocol Action type value
     * @param t
     *            type
     */
    static public void addMapping(short i, OFPActionType t) {
        if (mapping == null)
            mapping = new HashMap<Short, OFPActionType>();
        // bring higher mappings down to the edge of our map
        if (i < 0)
            i = (short) (30 + i);
        mapping.put(i, t);
    }

    /**
     * Given a wire protocol OpenFlow type number, return the OFPType associated
     * with it
     *
     * @param i
     *            wire protocol number
     * @return OFPType enum type
     */

    static public OFPActionType valueOf(short i) {
        if (i < 0)
            i = (short) (30 + i);
        return OFPActionType.mapping.get(i);
    }

    /**
     * @return Returns the wire protocol value corresponding to this
     *         OFPActionType
     */
    public short getTypeValue() {
        return this.type;
    }

    /**
     * @return return the OFPAction subclass corresponding to this OFPActionType
     */
    public Class<? extends OFPAction> toClass() {
        return clazz;
    }

    /**
     * Returns the no-argument Constructor of the implementation class for this
     * OFPActionType
     *
     * @return the constructor
     */
    public Constructor<? extends OFPAction> getConstructor() {
        return constructor;
    }

    /**
     * Returns a new instance of the OFPAction represented by this OFPActionType
     *
     * @return the new object
     */
    public OFPAction newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<OFPAction> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable
     *            the instantiable to set
     */
    public void setInstantiable(Instantiable<OFPAction> instantiable) {
        this.instantiable = instantiable;
    }
}
