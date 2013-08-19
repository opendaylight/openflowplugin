package org.openflow.codec.protocol;

import java.lang.reflect.Constructor;

/**
 * List of OpenFlow types and mappings to wire protocol value and derived
 * classes
 *
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public enum OFPType {
    HELLO(0, OFPHello.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPHello();
        }
    }), ERROR(1, OFPErrorMsg.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPErrorMsg();
        }
    }), ECHO_REQUEST(2, OFPEchoRequest.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPEchoRequest();
        }
    }), ECHO_REPLY(3, OFPEchoReply.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPEchoReply();
        }
    }), EXPERIMENTER(4, OFPExperimenterHeader.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPExperimenterHeader();
        }
    }), FEATURES_REQUEST(5, OFPSwitchFeaturesRequest.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPSwitchFeaturesRequest();
        }
    }), FEATURES_REPLY(6, OFPSwitchFeaturesReply.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPSwitchFeaturesReply();
        }
    }), GET_CONFIG_REQUEST(7, OFPGetConfigRequest.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPGetConfigRequest();
        }
    }), GET_CONFIG_REPLY(8, OFPGetConfigReply.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPGetConfigReply();
        }
    }), SET_CONFIG(9, OFPSetConfig.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPSetConfig();
        }
    }), PACKET_IN(10, OFPPacketIn.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPPacketIn();
        }
    }), FLOW_REMOVED(11, OFPFlowRemoved.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPFlowRemoved();
        }
    }), PORT_STATUS(12, OFPPortStatus.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPPortStatus();
        }
    }), PACKET_OUT(13, OFPPacketOut.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPPacketOut();
        }
    }), FLOW_MOD(14, OFPFlowMod.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPFlowMod();
        }
    }), GROUP_MOD(15, OFPGroupMod.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPGroupMod();
        }
    }),

    PORT_MOD(16, OFPPortMod.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPPortMod();
        }
    }),

    TABLE_MOD(17, OFPTableMod.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPTableMod();
        }
    }),

    MULTIPART_REQUEST(18, OFPMultipartRequest.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPMultipartRequest();
        }
    }), MULTIPART_REPLY(19, OFPMultipartReply.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPMultipartReply();
        }
    }), BARRIER_REQUEST(20, OFPBarrierRequest.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPBarrierRequest();
        }
    }), BARRIER_REPLY(21, OFPBarrierReply.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPBarrierReply();
        }
    }), QUEUE_CONFIG_REQUEST(22, OFPMessage.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPQueueConfigRequest();
        }
    }), QUEUE_CONFIG_REPLY(23, OFPMessage.class, new Instantiable<OFPMessage>() {
        @Override
        public OFPMessage instantiate() {
            return new OFPQueueConfigReply();
        }
    });

    static OFPType[] mapping;

    protected Class<? extends OFPMessage> clazz;
    protected Constructor<? extends OFPMessage> constructor;
    protected Instantiable<OFPMessage> instantiable;
    protected byte type;

    /**
     * Store some information about the OpenFlow type, including wire protocol
     * type number, length, and derived class
     *
     * @param type
     *            Wire protocol number associated with this OFPType
     * @param requestClass
     *            The Java class corresponding to this type of OpenFlow message
     * @param instantiator
     *            An Instantiator<OFPMessage> implementation that creates an
     *            instance of the specified OFPMessage
     */
    OFPType(int type, Class<? extends OFPMessage> clazz, Instantiable<OFPMessage> instantiator) {
        this.type = (byte) type;
        this.clazz = clazz;
        this.instantiable = instantiator;
        try {
            this.constructor = clazz.getConstructor(new Class[] {});
        } catch (Exception e) {
            throw new RuntimeException("Failure getting constructor for class: " + clazz, e);
        }
        OFPType.addMapping(this.type, this);
    }

    /**
     * Adds a mapping from type value to OFPType enum
     *
     * @param i
     *            OpenFlow wire protocol type
     * @param t
     *            type
     */
    static public void addMapping(byte i, OFPType t) {
        if (mapping == null)
            mapping = new OFPType[32];
        OFPType.mapping[i] = t;
    }

    /**
     * Remove a mapping from type value to OFPType enum
     *
     * @param i
     *            OpenFlow wire protocol type
     */
    static public void removeMapping(byte i) {
        OFPType.mapping[i] = null;
    }

    /**
     * Given a wire protocol OpenFlow type number, return the OFPType associated
     * with it
     *
     * @param i
     *            wire protocol number
     * @return OFPType enum type
     */

    static public OFPType valueOf(Byte i) {
        return OFPType.mapping[i];
    }

    /**
     * @return Returns the wire protocol value corresponding to this OFPType
     */
    public byte getTypeValue() {
        return this.type;
    }

    /**
     * @return return the OFPMessage subclass corresponding to this OFPType
     */
    public Class<? extends OFPMessage> toClass() {
        return clazz;
    }

    /**
     * Returns the no-argument Constructor of the implementation class for this
     * OFPType
     *
     * @return the constructor
     */
    public Constructor<? extends OFPMessage> getConstructor() {
        return constructor;
    }

    /**
     * Returns a new instance of the OFPMessage represented by this OFPType
     *
     * @return the new object
     */
    public OFPMessage newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<OFPMessage> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable
     *            the instantiable to set
     */
    public void setInstantiable(Instantiable<OFPMessage> instantiable) {
        this.instantiable = instantiable;
    }
}
