package org.openflow.codec.protocol.statistics;

import org.openflow.codec.io.IDataBuffer;

/**
 * The base class for all OpenFlow statistics.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
public interface OFPStatistics {
    /**
     * Returns the wire length of this message in bytes
     *
     * @return the length
     */
    public int getLength();

    /**
     * Read this message off the wire from the specified DataBuffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data);

    /**
     * Write this message's binary format to the specified DataBuffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data);
}
