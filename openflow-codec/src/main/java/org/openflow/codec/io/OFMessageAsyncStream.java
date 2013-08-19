/**
 *
 */
package org.openflow.codec.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import org.openflow.codec.protocol.OFPMessage;
import org.openflow.codec.protocol.factory.OFPMessageFactory;

/**
 * Asynchronous OpenFlow message marshalling and unmarshalling stream wrapped
 * around an NIO SocketChannel
 *
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public class OFMessageAsyncStream implements OFMessageInStream, OFMessageOutStream {
    static public int defaultBufferSize = 1048576; // 1MB

    protected ByteBuffer inBuf, outBuf;
    protected OFPMessageFactory messageFactory;
    protected SocketChannel sock;
    protected int partialReadCount = 0;

    public OFMessageAsyncStream(SocketChannel sock, OFPMessageFactory messageFactory) throws IOException {
        inBuf = ByteBuffer.allocateDirect(OFMessageAsyncStream.defaultBufferSize);
        outBuf = ByteBuffer.allocateDirect(OFMessageAsyncStream.defaultBufferSize);
        this.sock = sock;
        this.messageFactory = messageFactory;
        this.sock.configureBlocking(false);
    }

    @Override
    public List<OFPMessage> read() throws IOException {
        return this.read(0);
    }

    @Override
    public List<OFPMessage> read(int limit) throws IOException {
        List<OFPMessage> l;
        int read = sock.read(inBuf);
        if (read == -1)
            return null;
        inBuf.flip();
        IDataBuffer buffer = new ByteDataBuffer(inBuf);
        l = messageFactory.parseMessages(buffer, limit);
        if (inBuf.hasRemaining())
            inBuf.compact();
        else
            inBuf.clear();
        return l;
    }

    protected void appendMessageToOutBuf(OFPMessage m) throws IOException {
        int msglen = m.getLengthU();
        if (outBuf.remaining() < msglen) {
            throw new IOException("Message length exceeds buffer capacity: " + msglen);
        }
        IDataBuffer buffer = new ByteDataBuffer(outBuf);
        m.writeTo(buffer);
    }

    /**
     * Buffers a single outgoing openflow message
     */
    @Override
    public void write(OFPMessage m) throws IOException {
        appendMessageToOutBuf(m);
    }

    /**
     * Buffers a list of OpenFlow messages
     */
    @Override
    public void write(List<OFPMessage> l) throws IOException {
        for (OFPMessage m : l) {
            appendMessageToOutBuf(m);
        }
    }

    /**
     * Flush buffered outgoing data. Keep flushing until needsFlush() returns
     * false. Each flush() corresponds to a SocketChannel.write(), so this is
     * designed for one flush() per select() event
     */
    public void flush() throws IOException {
        outBuf.flip(); // swap pointers; lim = pos; pos = 0;
        sock.write(outBuf); // write data starting at pos up to lim
        outBuf.compact();
    }

    /**
     * Is there outgoing buffered data that needs to be flush()'d?
     */
    public boolean needsFlush() {
        return outBuf.position() > 0;
    }

    /**
     * @return the messageFactory
     */
    public OFPMessageFactory getMessageFactory() {
        return messageFactory;
    }

    /**
     * @param messageFactory
     *            the messageFactory to set
     */
    public void setMessageFactory(OFPMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }
}
