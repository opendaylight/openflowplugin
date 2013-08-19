/**
 *
 */
package org.openflow.codec.protocol.action;

import java.io.Serializable;

/**
 * List of OpenFlow Group types and mappings to wire protocol value and derived
 * classes
 *
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com)
 */
public enum OFPGroupType implements Serializable {

    OFPGT_ALL((short) 0), /* All (multicast/broadcast) group. */
    OFPGT_SELECT((short) 1), /* Select group. */
    OFPGT_INDIRECT((short) 2), /* Indirect group. */
    OFPGT_FF((short) 3); /* Fast failover group. */

    protected short value;

    private OFPGroupType(short value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public short getValue() {
        return value;
    }

}
