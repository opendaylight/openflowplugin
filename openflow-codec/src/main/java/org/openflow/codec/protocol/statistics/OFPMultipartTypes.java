package org.openflow.codec.protocol.statistics;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.openflow.codec.protocol.Instantiable;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_multipart_types enum
 *
 * @author AnilGujele
 *
 */
public enum OFPMultipartTypes {
    DESC(0, OFPDescriptionStatistics.class, OFPDescriptionStatistics.class, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPDescriptionStatistics();
        }
    }, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPDescriptionStatistics();
        }
    }), FLOW(1, OFPFlowStatisticsRequest.class, OFPFlowStatisticsReply.class, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPFlowStatisticsRequest();
        }
    }, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPFlowStatisticsReply();
        }
    }), AGGREGATE(2, OFPAggregateStatisticsRequest.class, OFPAggregateStatisticsReply.class,
            new Instantiable<OFPStatistics>() {
                @Override
                public OFPStatistics instantiate() {
                    return new OFPAggregateStatisticsRequest();
                }
            }, new Instantiable<OFPStatistics>() {
                @Override
                public OFPStatistics instantiate() {
                    return new OFPAggregateStatisticsReply();
                }
            }), TABLE(3, OFPTableStatistics.class, OFPTableStatistics.class, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPTableStatistics();
        }
    }, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPTableStatistics();
        }
    }), PORT_STATS(4, OFPPortStatisticsRequest.class, OFPPortStatisticsReply.class, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPPortStatisticsRequest();
        }
    }, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPPortStatisticsReply();
        }
    }), QUEUE(5, OFPQueueStatisticsRequest.class, OFPQueueStatisticsReply.class, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPQueueStatisticsRequest();
        }
    }, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPQueueStatisticsReply();
        }
    }), TABLE_FEATURES(12, OFPTableFeatures.class, OFPTableFeatures.class, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPTableFeatures();
        }
    }, new Instantiable<OFPStatistics>() {
        @Override
        public OFPStatistics instantiate() {
            return new OFPTableFeatures();
        }
    }), PORT_DESC(13, OFPPortDescriptionStatistics.class, OFPPortDescriptionStatistics.class,
            new Instantiable<OFPStatistics>() {
                @Override
                public OFPStatistics instantiate() {
                    return new OFPPortDescriptionStatistics();
                }
            }, new Instantiable<OFPStatistics>() {
                @Override
                public OFPStatistics instantiate() {
                    return new OFPPortDescriptionStatistics();
                }
            }),

    EXPERIMENTER(0xffff, OFPExperimenterMultipartHeader.class, OFPExperimenterMultipartHeader.class,
            new Instantiable<OFPStatistics>() {
                @Override
                public OFPStatistics instantiate() {
                    return new OFPExperimenterMultipartHeader();
                }
            }, new Instantiable<OFPStatistics>() {
                @Override
                public OFPStatistics instantiate() {
                    return new OFPExperimenterMultipartHeader();
                }
            });

    private static Map<Integer, OFPMultipartTypes> requestMapping;
    private static Map<Integer, OFPMultipartTypes> replyMapping;

    private Class<? extends OFPStatistics> requestClass;
    private Constructor<? extends OFPStatistics> requestConstructor;
    private Instantiable<OFPStatistics> requestInstantiable;
    private Class<? extends OFPStatistics> replyClass;
    private Constructor<? extends OFPStatistics> replyConstructor;
    private Instantiable<OFPStatistics> replyInstantiable;
    private int type;

    /**
     * Store some information about the OpenFlow Statistic type, including wire
     * protocol type number, and derived class
     *
     * @param type
     *            Wire protocol number associated with this OFPMultipartType
     * @param requestClass
     *            The Multipart Java class to return when the containing OFPType
     *            is MULTIPART_REQUEST
     * @param replyClass
     *            The Multipart Java class to return when the containing OFPType
     *            is MULTIPART_REPLY
     */
    OFPMultipartTypes(int type, Class<? extends OFPStatistics> requestClass, Class<? extends OFPStatistics> replyClass,
            Instantiable<OFPStatistics> requestInstantiable, Instantiable<OFPStatistics> replyInstantiable) {
        this.type = type;
        this.requestClass = requestClass;
        try {
            this.requestConstructor = requestClass.getConstructor(new Class[] {});
        } catch (Exception e) {
            throw new RuntimeException("Failure getting constructor for class: " + requestClass, e);
        }

        this.replyClass = replyClass;
        try {
            this.replyConstructor = replyClass.getConstructor(new Class[] {});
        } catch (Exception e) {
            throw new RuntimeException("Failure getting constructor for class: " + replyClass, e);
        }
        this.requestInstantiable = requestInstantiable;
        this.replyInstantiable = replyInstantiable;
        OFPMultipartTypes.addMapping(this.type, OFPType.MULTIPART_REQUEST, this);
        OFPMultipartTypes.addMapping(this.type, OFPType.MULTIPART_REPLY, this);
    }

    /**
     * Adds a mapping from type value to OFStatisticsType enum
     *
     * @param i
     *            OpenFlow wire protocol type
     * @param t
     *            type of containing OFPMessage, only accepts MULTIPART_REQUEST
     *            or MULTIPART_REPLY
     * @param st
     *            type
     */
    public static void addMapping(int i, OFPType t, OFPMultipartTypes st) {

        if (t == OFPType.MULTIPART_REQUEST) {
            if (requestMapping == null)
                requestMapping = new HashMap<Integer, OFPMultipartTypes>();
            OFPMultipartTypes.requestMapping.put(i, st);
        } else if (t == OFPType.MULTIPART_REPLY) {
            if (replyMapping == null)
                replyMapping = new HashMap<Integer, OFPMultipartTypes>();
            OFPMultipartTypes.replyMapping.put(i, st);
        } else {
            throw new RuntimeException(t.toString() + " is an invalid OFPType");
        }
    }

    /**
     * Remove a mapping from type value to OFPMultipartType enum
     *
     * @param i
     *            OpenFlow wire protocol type
     * @param t
     *            type of containing OFPMessage, only accepts MULTIPART_REQUEST
     *            or MULTIPART_REPLY
     */
    public static void removeMapping(int i, OFPType t) {
        if (t == OFPType.MULTIPART_REQUEST) {
            requestMapping.remove(i);
        } else if (t == OFPType.MULTIPART_REPLY) {
            replyMapping.remove(i);
        } else {
            throw new RuntimeException(t.toString() + " is an invalid OFPType");
        }
    }

    /**
     * Given a wire protocol OpenFlow type number, return the OFPMultipartType
     * associated with it
     *
     * @param i
     *            wire protocol number
     * @param t
     *            type of containing OFPMessage, only accepts MULTIPART_REQUEST
     *            or MULTIPART_REPLY
     * @return OFPMultipartType enum type
     */
    public static OFPMultipartTypes valueOf(short i, OFPType t) {
        if (t == OFPType.MULTIPART_REQUEST) {
            return requestMapping.get(U16.f(i));
        } else if (t == OFPType.MULTIPART_REPLY) {
            return replyMapping.get(U16.f(i));
        } else {
            throw new RuntimeException(t.toString() + " is an invalid OFPType");
        }
    }

    /**
     * @return Returns the wire protocol value corresponding to this
     *         OFPMultipartType
     */
    public short getTypeValue() {
        return U16.t(this.type);
    }

    /**
     * @return Returns the wire protocol unsigned value corresponding to this
     *         OFPMultipartType
     */
    public int getTypeValueU() {
        return this.type;
    }

    /**
     * @param t
     *            type of containing OFPMessage, only accepts MULTIPART_REQUEST
     *            or MULTIPART_REPLY
     * @return return the OFPMessage subclass corresponding to this
     *         OFPMultipartType
     */
    public Class<? extends OFPStatistics> toClass(OFPType t) {
        if (t == OFPType.MULTIPART_REQUEST) {
            return requestClass;
        } else if (t == OFPType.MULTIPART_REPLY) {
            return replyClass;
        } else {
            throw new RuntimeException(t.toString() + " is an invalid OFPType");
        }
    }

    /**
     * Returns the no-argument Constructor of the implementation class for this
     * OFPMultipartType, either request or reply based on the supplied OFPType
     *
     * @param t
     * @return
     */
    public Constructor<? extends OFPStatistics> getConstructor(OFPType t) {
        if (t == OFPType.MULTIPART_REQUEST) {
            return requestConstructor;
        } else if (t == OFPType.MULTIPART_REPLY) {
            return replyConstructor;
        } else {
            throw new RuntimeException(t.toString() + " is an invalid OFPType");
        }
    }

    /**
     * @return the requestInstantiable
     */
    public Instantiable<OFPStatistics> getRequestInstantiable() {
        return requestInstantiable;
    }

    /**
     * @param requestInstantiable
     *            the requestInstantiable to set
     */
    public void setRequestInstantiable(Instantiable<OFPStatistics> requestInstantiable) {
        this.requestInstantiable = requestInstantiable;
    }

    /**
     * @return the replyInstantiable
     */
    public Instantiable<OFPStatistics> getReplyInstantiable() {
        return replyInstantiable;
    }

    /**
     * @param replyInstantiable
     *            the replyInstantiable to set
     */
    public void setReplyInstantiable(Instantiable<OFPStatistics> replyInstantiable) {
        this.replyInstantiable = replyInstantiable;
    }

    /**
     * Returns a new instance of the implementation class for this
     * OFPMultipartType, either request or reply based on the supplied OFPType
     *
     * @param t
     * @return
     */
    public OFPStatistics newInstance(OFPType t) {
        if (t == OFPType.MULTIPART_REQUEST) {
            return requestInstantiable.instantiate();
        } else if (t == OFPType.MULTIPART_REPLY) {
            return replyInstantiable.instantiate();
        } else {
            throw new RuntimeException(t.toString() + " is an invalid OFPType");
        }
    }
}
