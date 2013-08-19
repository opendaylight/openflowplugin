package org.openflow.codec.io;

import java.nio.ByteBuffer;

/**
 * Creates a new IDataBuffer by allocating new space. DataBuffers is responsible
 * to create buffer class that will used by test cases. currently it instantiate
 * ByteDataBuffer (java's ByteBuffer) but it can be updated to support other
 * data buffer in future.
 *
 * @author AnilGujele
 *
 */
public final class DataBuffers {
    /**
     * private constructor to protect instantiation
     */
    private DataBuffers() {

    }

    /**
     * get a new data buffer.
     *
     * @param capacity
     *            The new buffer's capacity, in bytes
     *
     * @return The new data buffer
     *
     * @throws IllegalArgumentException
     *             If the <tt>capacity</tt> is a negative integer
     */
    public static IDataBuffer allocate(int capacity) {

        ByteBuffer bytebuffer = ByteBuffer.allocateDirect(capacity);
        ByteDataBuffer dataBuffer = new ByteDataBuffer(bytebuffer);
        return dataBuffer;
    }
}
