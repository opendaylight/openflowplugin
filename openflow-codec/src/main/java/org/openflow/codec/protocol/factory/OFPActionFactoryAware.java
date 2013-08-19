package org.openflow.codec.protocol.factory;

/**
 * Objects implementing this interface are expected to be instantiated with an
 * instance of an OFPActionFactory
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFPActionFactoryAware {
    /**
     * Sets the OFPActionFactory
     *
     * @param actionFactory
     */
    public void setActionFactory(OFPActionFactory actionFactory);
}
