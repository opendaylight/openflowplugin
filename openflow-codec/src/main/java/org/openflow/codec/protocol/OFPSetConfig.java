package org.openflow.codec.protocol;

/**
 * Represents an OFPT_SET_CONFIG type message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPSetConfig extends OFPSwitchConfig {
    public OFPSetConfig() {
        super();
        this.type = OFPType.SET_CONFIG;
    }
}
