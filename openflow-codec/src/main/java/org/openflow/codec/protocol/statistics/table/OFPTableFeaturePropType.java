package org.openflow.codec.protocol.statistics.table;

import java.util.HashMap;
import java.util.Map;

import org.openflow.codec.protocol.Instantiable;
import org.openflow.codec.util.U16;

public enum OFPTableFeaturePropType {

    INSTRUCTIONS(0, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropInstructions();
        }
    }), INSTRUCTIONS_MISS(1, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropInstructionsMiss();
        }
    }), NEXT_TABLES(2, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropNextTables();
        }
    }), NEXT_TABLES_MISS(3, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropNextTablesMiss();
        }
    }), WRITE_ACTIONS(4, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropWriteActions();
        }
    }), WRITE_ACTIONS_MISS(5, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropWriteActionsMiss();
        }
    }), APPLY_ACTIONS(6, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropApplyActions();
        }
    }), APPLY_ACTIONS_MISS(7, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropApplyActionsMiss();
        }
    }), MATCH(8, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropMatch();
        }
    }), WILDCARDS(10, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropWildcards();
        }
    }), WRITE_SETFIELD(12, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropWriteSetField();
        }
    }), WRITE_SETFIELD_MISS(13, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropWriteSetFieldMiss();
        }
    }), APPLY_SETFIELD(14, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropApplySetField();
        }
    }), APPLY_SETFIELD_MISS(15, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropApplySetFieldMiss();
        }
    }),

    EXPERIMENTER(0xFFFE, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropExperimenter();
        }
    }),

    EXPERIMENTER_MISS(0xFFFF, new Instantiable<OFPTableFeaturePropHeader>() {
        @Override
        public OFPTableFeaturePropHeader instantiate() {
            return new OFPTableFeaturePropExperimenterMiss();
        }
    });

    private static Map<Integer, OFPTableFeaturePropType> mapping;

    private short type;

    private Instantiable<OFPTableFeaturePropHeader> instantiable;

    /**
     *
     * @param type
     */
    OFPTableFeaturePropType(int type, Instantiable<OFPTableFeaturePropHeader> instantiable) {
        this.setTypeValue((short) type);
        OFPTableFeaturePropType.addMapping(type, this);
        this.instantiable = instantiable;
    }

    /**
     * add mapping to store
     *
     * @param type
     * @param TableFeatureType
     */
    private static void addMapping(int type, OFPTableFeaturePropType TableFeatureType) {
        if (null == mapping) {
            mapping = new HashMap<Integer, OFPTableFeaturePropType>();
        }
        mapping.put(type, TableFeatureType);
    }

    /**
     * get OFTableFeatureType correspond to value type
     *
     * @param type
     * @return OFTableFeatureType
     */
    public static OFPTableFeaturePropType valueOf(short type) {
        return mapping.get(U16.f(type));
    }

    /**
     * get TableFeatureProp type value
     *
     * @return
     */
    public short getTypeValue() {
        return type;
    }

    /**
     * set TableFeatureProp type value
     *
     * @param type
     */
    public void setTypeValue(short type) {
        this.type = type;
    }

    /**
     * Returns a new instance of the OFPTableFeaturePropHeader represented by
     * this OFPTableFeaturePropType
     *
     * @return the new object
     */
    public OFPTableFeaturePropHeader newInstance() {
        return instantiable.instantiate();
    }

}
