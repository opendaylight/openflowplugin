package org.openflow.codec.protocol.statistics.table;

import java.util.ArrayList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.action.OFPAction;
import org.openflow.codec.protocol.factory.OFPActionFactory;
import org.openflow.codec.protocol.factory.OFPActionFactoryAware;
import org.openflow.codec.util.U16;

/**
 * Represents struct ofp_table_feature_prop_actions
 *
 * @author AnilGujele
 *
 */
public abstract class OFPTableFeaturePropActions extends OFPTableFeaturePropHeader implements OFPActionFactoryAware {
    private List<OFPAction> actionIds;
    private OFPActionFactory actionFactory;

    /**
     * constructor
     */
    public OFPTableFeaturePropActions() {
        super.setLength(MINIMUM_LENGTH);
        actionIds = new ArrayList<OFPAction>();
    }

    /**
     * get list of action id
     *
     * @return
     */
    public List<OFPAction> getActionIds() {
        return actionIds;
    }

    /**
     * set list of action id
     *
     * @param actionIds
     */
    public void setActionIds(List<OFPAction> actionIds) {
        this.actionIds = actionIds;
        updateLength();
    }

    /**
     * update the length
     *
     * @return
     */
    private void updateLength() {
        int length = this.getLength();
        for (OFPAction OFAction : actionIds) {
            length += OFAction.getLengthU();
        }
        this.setLength((short) length);

    }

    /**
     * read OFPTableFeaturePropActions from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        int actionIdsLength = U16.f(this.getLength()) - MINIMUM_LENGTH;
        // TODO : experimenter part where size can differ
        // It will be dependent on type of experimenter
        if (null == actionFactory) {
            throw new RuntimeException("OFPInstructionFactory is not set.");
        }
        actionIds = actionFactory.parseActions(data, actionIdsLength);
        /* Read the padding, if any */
        int paddingLength = ((this.getLength() % MULTIPLE_OF_EIGHT) == 0) ? 0
                : (MULTIPLE_OF_EIGHT - (this.getLength() % MULTIPLE_OF_EIGHT));
        data.position(data.position() + paddingLength);
    }

    /**
     * write OFPTableFeaturePropActions to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        // write action ids
        for (OFPAction ofAction : actionIds) {
            ofAction.writeTo(data);
        }
        /* Add padding if structure is not 8 byte aligned */
        int paddingLength = ((this.getLength() % MULTIPLE_OF_EIGHT) == 0) ? 0
                : (MULTIPLE_OF_EIGHT - (this.getLength() % MULTIPLE_OF_EIGHT));
        byte[] padding = new byte[paddingLength];
        data.put(padding);

    }

    @Override
    public int hashCode() {
        final int prime = 744;
        int result = super.hashCode();
        result = prime * result + actionIds.hashCode();
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
        if (!(obj instanceof OFPTableFeaturePropActions)) {
            return false;
        }
        OFPTableFeaturePropActions other = (OFPTableFeaturePropActions) obj;
        if (!this.actionIds.equals(other.actionIds)) {
            return false;
        }
        return true;
    }

    @Override
    public void setActionFactory(OFPActionFactory actionFactory) {
        this.actionFactory = actionFactory;

    }

}
