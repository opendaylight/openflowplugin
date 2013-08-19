package org.openflow.codec.protocol.factory;

/**
 * Objects implementing this interface are expected to be instantiated with an
 * instance of an OFPInstructionFactory
 *
 * @author AnilGujele
 */
public interface OFPInstructionFactoryAware {
    /**
     * Sets the OFPInstructionFactory
     *
     * @param instructionFactory
     */
    public void setInstructionFactory(OFPInstructionFactory instructionFactory);
}
