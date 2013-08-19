package org.openflow.codec.protocol.instruction;

import java.util.HashMap;
import java.util.Map;

import org.openflow.codec.protocol.Instantiable;
import org.openflow.codec.util.U16;

/**
 * Represents struct ofp_instruction_type
 *
 * @author AnilGujele
 *
 */
public enum OFPInstructionType {
    /* Setup the next table in the lookup pipeline */
    GOTO_TABLE(1, new Instantiable<OFPInstruction>() {
        @Override
        public OFPInstruction instantiate() {
            return new OFPInstructionGoToTable();
        }
    }),
    /* Setup the metadata field for use later in pipeline */
    WRITE_METADATA(2, new Instantiable<OFPInstruction>() {
        @Override
        public OFPInstruction instantiate() {
            return new OFPInstructionWriteMetaData();
        }
    }),
    /* Write the action(s) onto the datapath action set */
    WRITE_ACTIONS(3, new Instantiable<OFPInstruction>() {
        @Override
        public OFPInstruction instantiate() {
            return new OFPInstructionWriteActions();
        }
    }),
    /* Applies the action(s) immediately */
    APPLY_ACTIONS(4, new Instantiable<OFPInstruction>() {
        @Override
        public OFPInstruction instantiate() {
            return new OFPInstructionApplyActions();
        }
    }),
    /* Clears all actions from the datapath action set */
    CLEAR_ACTIONS(5, new Instantiable<OFPInstruction>() {
        @Override
        public OFPInstruction instantiate() {
            return new OFPInstructionClearActions();
        }
    }),
    /* Apply meter (rate limiter) */
    METER(6, new Instantiable<OFPInstruction>() {
        @Override
        public OFPInstruction instantiate() {
            return new OFPInstructionMeter();
        }
    }),
    /* Experimenter instruction */
    EXPERIMENTER(0xFFFF, new Instantiable<OFPInstruction>() {
        @Override
        public OFPInstruction instantiate() {
            return new OFPInstructionExperimenter();
        }
    });

    private static Map<Integer, OFPInstructionType> mapping;

    private short type;

    private Instantiable<OFPInstruction> instantiable;

    /**
     *
     * @param type
     */
    OFPInstructionType(int type, Instantiable<OFPInstruction> instantiable) {
        this.setTypeValue((short) type);
        OFPInstructionType.addMapping(type, this);
        this.instantiable = instantiable;
    }

    /**
     * add mapping to store
     *
     * @param type
     * @param instructionType
     */
    private static void addMapping(int type, OFPInstructionType instructionType) {
        if (null == mapping) {
            mapping = new HashMap<Integer, OFPInstructionType>();
        }
        mapping.put(type, instructionType);
    }

    /**
     * get OFPInstructionType correspond to value type
     *
     * @param type
     * @return OFPInstructionType
     */
    public static OFPInstructionType valueOf(short type) {
        return mapping.get(U16.f(type));
    }

    /**
     * get instruction type value
     *
     * @return
     */
    public short getTypeValue() {
        return type;
    }

    /**
     * set instruction type value
     *
     * @param type
     */
    public void setTypeValue(short type) {
        this.type = type;
    }

    /**
     * Returns a new instance of the OFPAction represented by this OFPActionType
     *
     * @return the new object
     */
    public OFPInstruction newInstance() {
        return instantiable.instantiate();
    }

}
