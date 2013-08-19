/**
 *
 */
package org.openflow.codec.io;

import java.util.List;

import org.openflow.codec.protocol.OFPMessage;
import org.openflow.codec.protocol.factory.OFPMessageFactory;

/**
 * Interface for reading OFMessages from a buffered stream
 *
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 *
 */
public interface OFMessageInStream {
    /**
     * Read OF messages from the stream
     *
     * @return a list of OF Messages, empty if no complete messages are
     *         available, null if the stream has closed
     */
    public List<OFPMessage> read() throws java.io.IOException;

    /**
     * Read OF messages from the stream
     *
     * @param limit
     *            The maximum number of messages to read: 0 means all that are
     *            buffered
     * @return a list of OF Messages, empty if no complete messages are
     *         available, null if the stream has closed
     *
     */
    public List<OFPMessage> read(int limit) throws java.io.IOException;

    /**
     * Sets the OFPMessageFactory used to create messages on this stream
     *
     * @param factory
     */
    public void setMessageFactory(OFPMessageFactory factory);

    /**
     * Returns the OFPMessageFactory used to create messages on this stream
     *
     * @return
     */
    public OFPMessageFactory getMessageFactory();
}
