package org.openflow.codec.protocol.factory;

import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.protocol.statistics.OFPMultipartTypes;
import org.openflow.codec.protocol.statistics.OFPStatistics;

/**
 * The interface to factories used for retrieving OFPStatistics instances. All
 * methods are expected to be thread-safe.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFPStatisticsFactory {
    /**
     * Retrieves an OFPStatistics instance corresponding to the specified
     * OFStatisticsType
     *
     * @param t
     *            the type of the containing OFPMessage, only accepts statistics
     *            request or reply
     * @param st
     *            the type of the OFPStatistics to be retrieved
     * @return an OFPStatistics instance
     */
    public OFPStatistics getStatistics(OFPType t, OFPMultipartTypes st);

    /**
     * Attempts to parse and return all OFPStatistics contained in the given
     * DataBuffer, beginning at the DataBuffer's position, and ending at
     * position+length.
     *
     * @param t
     *            the type of the containing OFPMessage, only accepts statistics
     *            request or reply
     * @param st
     *            the type of the OFPStatistics to be retrieved
     * @param data
     *            the DataBuffer to parse for OpenFlow Statistics
     * @param length
     *            the number of Bytes to examine for OpenFlow Statistics
     * @return a list of OFPStatistics instances
     */
    public List<OFPStatistics> parseStatistics(OFPType t, OFPMultipartTypes st, IDataBuffer data, int length);

    /**
     * Attempts to parse and return all OFPStatistics contained in the given
     * DataBuffer, beginning at the DataBuffer's position, and ending at
     * position+length.
     *
     * @param t
     *            the type of the containing OFPMessage, only accepts statistics
     *            request or reply
     * @param st
     *            the type of the OFPStatistics to be retrieved
     * @param data
     *            the DataBuffer to parse for OpenFlow Statistics
     * @param length
     *            the number of Bytes to examine for OpenFlow Statistics
     * @param limit
     *            the maximum number of messages to return, 0 means no limit
     * @return a list of OFPStatistics instances
     */
    public List<OFPStatistics> parseStatistics(OFPType t, OFPMultipartTypes st, IDataBuffer data, int length, int limit);
}
