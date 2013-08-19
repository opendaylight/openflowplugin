package org.openflow.codec.protocol.statistics;

/**
 * OFPStatistics interface is extended to have operation to set the length.
 *
 * @see org.openflow.codec.protocol.statistics.OFPExperimenterMultipartHeader
 *
 * @author AnilGujele
 */
public interface OFPExtStatistics extends OFPStatistics {
    /**
     * set the length of Multipart in bytes
     *
     * @param length
     *            - the length
     */
    public void setLength(int length);

}
