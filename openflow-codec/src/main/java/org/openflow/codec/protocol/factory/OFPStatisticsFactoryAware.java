package org.openflow.codec.protocol.factory;

/**
 * Objects implementing this interface are expected to be instantiated with an
 * instance of an OFPStatisticsFactory
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFPStatisticsFactoryAware {
    /**
     * Sets the OFPStatisticsFactory
     *
     * @param statisticsFactory
     */
    public void setStatisticsFactory(OFPStatisticsFactory statisticsFactory);
}
