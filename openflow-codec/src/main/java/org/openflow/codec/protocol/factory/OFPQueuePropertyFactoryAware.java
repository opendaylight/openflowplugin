package org.openflow.codec.protocol.factory;

/**
 * Objects implementing this interface are expected to be instantiated with an
 * instance of an OFPQueuePropertyFactory
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFPQueuePropertyFactoryAware {
    /**
     * Sets the OFPQueuePropertyFactory
     *
     * @param queuePropertyFactory
     */
    public void setQueuePropertyFactory(OFPQueuePropertyFactory queuePropertyFactory);
}
