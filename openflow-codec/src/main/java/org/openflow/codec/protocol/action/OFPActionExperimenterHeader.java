/**
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com) - Jul 20, 2013
 */
package org.openflow.codec.protocol.action;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an ofp_action_experimenter_header
 */
public class OFPActionExperimenterHeader extends OFPAction {
    public static int MINIMUM_LENGTH = 8;

    protected int experimenter;

    public OFPActionExperimenterHeader() {
        super.setType(OFPActionType.EXPERIMENTER);
        super.setLength((short) MINIMUM_LENGTH);
    }

    /**
     * @return the experimenter
     */
    public int getExperimenter() {
        return experimenter;
    }

    /**
     * @param experimenter
     *            the experimenter to set
     */
    public void setExperimenter(int experimenter) {
        this.experimenter = experimenter;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.experimenter = data.getInt();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(this.experimenter);
    }

    @Override
    public int hashCode() {
        final int prime = 397;
        int result = super.hashCode();
        result = prime * result + experimenter;
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
        if (!(obj instanceof OFPActionExperimenterHeader)) {
            return false;
        }
        OFPActionExperimenterHeader other = (OFPActionExperimenterHeader) obj;
        if (experimenter != other.experimenter) {
            return false;
        }
        return true;
    }
}