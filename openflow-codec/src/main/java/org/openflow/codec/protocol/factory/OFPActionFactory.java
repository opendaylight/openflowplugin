package org.openflow.codec.protocol.factory;

import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.action.OFPAction;
import org.openflow.codec.protocol.action.OFPActionType;

/**
 * The interface to factories used for retrieving OFPAction instances. All
 * methods are expected to be thread-safe.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFPActionFactory {
    /**
     * Retrieves an OFPAction instance corresponding to the specified
     * OFPActionType
     *
     * @param t
     *            the type of the OFPAction to be retrieved
     * @return an OFPAction instance
     */
    public OFPAction getAction(OFPActionType t);

    /**
     * Attempts to parse and return all OFActions contained in the given
     * DataBuffer, beginning at the DataBuffer's position, and ending at
     * position+length.
     *
     * @param data
     *            the DataBuffer to parse for OpenFlow actions
     * @param length
     *            the number of Bytes to examine for OpenFlow actions
     * @return a list of OFPAction instances
     */
    public List<OFPAction> parseActions(IDataBuffer data, int length);

    /**
     * Attempts to parse and return all OFActions contained in the given
     * DataBuffer, beginning at the DataBuffer's position, and ending at
     * position+length.
     *
     * @param data
     *            the DataBuffer to parse for OpenFlow actions
     * @param length
     *            the number of Bytes to examine for OpenFlow actions
     * @param limit
     *            the maximum number of messages to return, 0 means no limit
     * @return a list of OFPAction instances
     */
    public List<OFPAction> parseActions(IDataBuffer data, int length, int limit);
}
