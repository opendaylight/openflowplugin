package org.openflow.codec.protocol.factory;

import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.queue.OFPQueueProperty;
import org.openflow.codec.protocol.queue.OFPQueuePropertyType;

/**
 * The interface to factories used for retrieving OFPQueueProperty instances.
 * All methods are expected to be thread-safe.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFPQueuePropertyFactory {
    /**
     * Retrieves an OFPQueueProperty instance corresponding to the specified
     * OFPQueuePropertyType
     *
     * @param t
     *            the type of the OFPQueueProperty to be retrieved
     * @return an OFPQueueProperty instance
     */
    public OFPQueueProperty getQueueProperty(OFPQueuePropertyType t);

    /**
     * Attempts to parse and return all OFQueueProperties contained in the given
     * DataBuffer, beginning at the DataBuffer's position, and ending at
     * position+length.
     *
     * @param data
     *            the DataBuffer to parse for OpenFlow OFQueueProperties
     * @param length
     *            the number of Bytes to examine for OpenFlow OFQueueProperties
     * @return a list of OFPQueueProperty instances
     */
    public List<OFPQueueProperty> parseQueueProperties(IDataBuffer data, int length);

    /**
     * Attempts to parse and return all OFQueueProperties contained in the given
     * DataBuffer, beginning at the DataBuffer's position, and ending at
     * position+length.
     *
     * @param data
     *            the DataBuffer to parse for OpenFlow OFQueueProperties
     * @param length
     *            the number of Bytes to examine for OpenFlow OFQueueProperties
     * @param limit
     *            the maximum number of OFQueueProperties to return, 0 means no
     *            limit
     * @return a list of OFPQueueProperty instances
     */
    public List<OFPQueueProperty> parseQueueProperties(IDataBuffer data, int length, int limit);
}
