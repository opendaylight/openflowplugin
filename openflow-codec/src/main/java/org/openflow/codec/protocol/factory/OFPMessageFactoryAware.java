/**
 *
 */
package org.openflow.codec.protocol.factory;

/**
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 *
 */
public interface OFPMessageFactoryAware {

    /**
     * Sets the message factory for this object
     *
     * @param factory
     */
    void setMessageFactory(OFPMessageFactory factory);
}
