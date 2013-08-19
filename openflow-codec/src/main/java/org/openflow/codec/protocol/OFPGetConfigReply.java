package org.openflow.codec.protocol;

/**
 * Represents an OFPT_GET_CONFIG_REPLY type message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPGetConfigReply extends OFPSwitchConfig {
    public OFPGetConfigReply() {
        super();
        this.type = OFPType.GET_CONFIG_REPLY;
    }
}
