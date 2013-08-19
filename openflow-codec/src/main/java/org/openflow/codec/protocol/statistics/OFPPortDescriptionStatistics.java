package org.openflow.codec.protocol.statistics;

import java.io.Serializable;

import org.openflow.codec.protocol.OFPPort;

/**
 * Represents struct ofp_port for OFPMP_PORT_DESCRIPTION
 *
 * @author AnilGujele
 *
 */
public class OFPPortDescriptionStatistics extends OFPPort implements OFPStatistics, Serializable {

    public OFPPortDescriptionStatistics() {

    }

}
