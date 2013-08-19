package org.openflow.codec.protocol.instruction;

import java.util.ArrayList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.action.OFPAction;
import org.openflow.codec.protocol.factory.OFPActionFactory;
import org.openflow.codec.protocol.factory.OFPActionFactoryAware;
import org.openflow.codec.util.U16;

/**
 * base class for instruction structure WRITE/APPLY/CLEAR_ACTIONS correspond to
 * struct ofp_instruction_actions
 *
 * @author AnilGujele
 *
 */
public abstract class OFPInstructionActions extends OFPInstruction implements OFPActionFactoryAware {

    private final static short MINIMUM_LENGTH = 8;
    private List<OFPAction> actions;
    private OFPActionFactory actionFactory;

    /**
     * constructor
     */
    public OFPInstructionActions() {
        actions = new ArrayList<OFPAction>();
        this.setLength(MINIMUM_LENGTH);

    }

    /**
     * read OFPInstruction from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        data.getInt(); // pad
        if (null == actionFactory) {
            throw new RuntimeException("OFPActionFactory is not set.");
        }
        int actionDataLength = U16.f(this.getLength()) - MINIMUM_LENGTH;
        // read actions
        actions = actionFactory.parseActions(data, actionDataLength);

    }

    /**
     * write OFPInstruction to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(0); // pad
        // write action
        for (OFPAction ofAction : actions) {
            ofAction.writeTo(data);
        }
    }

    /**
     * get actions in this instruction
     *
     * @return
     */
    public List<OFPAction> getActions() {
        return actions;
    }

    /**
     * set actions in this instructions.
     *
     * @param actions
     */

    public void setActions(List<OFPAction> actions) {
        this.actions = actions;
        updateLength();
    }

    /**
     * get the length of instruction actions
     *
     * @return
     */
    private void updateLength() {
        length = MINIMUM_LENGTH;
        for (OFPAction ofAction : actions) {
            length += ofAction.getLengthU();
        }
    }

    @Override
    public void setActionFactory(OFPActionFactory actionFactory) {
        this.actionFactory = actionFactory;

    }

    @Override
    public int hashCode() {
        final int prime = 742;
        int result = super.hashCode();
        result = prime * result + actions.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFPInstructionActions)) {
            return false;
        }
        OFPInstructionActions other = (OFPInstructionActions) obj;
        if (!this.actions.equals(other.actions)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public OFPInstructionActions clone() throws CloneNotSupportedException {
        OFPInstructionActions clone = (OFPInstructionActions) super.clone();
        clone.actions = new ArrayList<OFPAction>();
        for (OFPAction action : this.actions) {
            clone.actions.add(action.clone());
        }

        return clone;
    }

    /**
     * Returns a string representation of the instruction
     */
    public String toString() {
        return "OFPInstruction[" + "type=" + this.getOFInstructionType() + ", length=" + this.getLength()
                + ", actions=" + actions.toString() + "]";
    }

}
