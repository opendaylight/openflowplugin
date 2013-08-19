package org.openflow.codec.protocol.factory;

import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPMessage;
import org.openflow.codec.protocol.OFPType;

/**
 * The interface to factories used for retrieving OFPMessage instances. All
 * methods are expected to be thread-safe.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFPMessageFactory {
    /**
     * Retrieves an OFPMessage instance corresponding to the specified OFPType
     *
     * @param t
     *            the type of the OFPMessage to be retrieved
     * @return an OFPMessage instance
     */
    public OFPMessage getMessage(OFPType t);

    /**
     * Attempts to parse and return all OFMessages contained in the given
     * DataBuffer, beginning at the DataBuffer's position, and ending at the
     * DataBuffer's limit.
     *
     * @param data
     *            the DataBuffer to parse for an OpenFlow message
     * @return a list of OFPMessage instances
     */
    public List<OFPMessage> parseMessages(IDataBuffer data);

    /**
     * Attempts to parse and return all OFMessages contained in the given
     * DataBuffer, beginning at the DataBuffer's position, and ending at the
     * DataBuffer's limit.
     *
     * @param data
     *            the DataBuffer to parse for an OpenFlow message
     * @param limit
     *            the maximum number of messages to return, 0 means no limit
     * @return a list of OFPMessage instances
     */
    public List<OFPMessage> parseMessages(IDataBuffer data, int limit);

    /**
     * Retrieves an OFPActionFactory
     *
     * @return an OFPActionFactory
     */
    public OFPActionFactory getActionFactory();
}
