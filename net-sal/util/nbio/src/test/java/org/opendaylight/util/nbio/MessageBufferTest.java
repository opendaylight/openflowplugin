/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests of the message buffer operation.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class MessageBufferTest {

    private static final int LENGTH = 16;

    private static final FixedLengthMessage M1 = new FixedLengthMessage(LENGTH);
    private static final FixedLengthMessage M2 = new FixedLengthMessage(LENGTH);
    private static final FixedLengthMessage M3 = new FixedLengthMessage(LENGTH);
    private static final FixedLengthMessage M4 = new FixedLengthMessage(LENGTH);

    private static final int BIG_SIZE = 32 * 1024;
    private static final FixedLengthMessage BAM = new FixedLengthMessage(BIG_SIZE);

    private static enum WritePending { ON, OFF; public boolean on() { return this == ON; }};
    private static enum FlushRequired { ON, OFF; public boolean on() { return this == ON; }};

    private FakeIOLoop loop;
    private TestByteChannel channel;
    private FixedLengthMessageBuffer buffer;
    private TestKey key;

    // TODO: Add assertions regarding the buffer key.

    @Before
    public void setUp() throws IOException {
        loop = new FakeIOLoop();
        channel = new TestByteChannel();
        key = new TestKey(channel);
        buffer = loop.createBuffer(channel, null);
        buffer.setKey(key);
    }

    @After
    public void tearDown() {
        loop.cease();
        buffer.discard();
    }

    // Check state of the message buffer
    private void assertState(WritePending wp, FlushRequired fr,
                             int read, int written) {
        assertEquals(AM_NEQ, wp.on(), buffer.isWriteStillPending());
        assertEquals(AM_NEQ, fr.on(), buffer.requiresFlush());
        assertEquals(AM_NEQ, read, channel.readBytes);
        assertEquals(AM_NEQ, written, channel.writtenBytes);
    }

    @Test
    public void endOfStream() throws IOException {
        channel.close();
        List<FixedLengthMessage> messages = buffer.dequeue();
        assertNull(AM_HUH, messages);
    }

    @Test
    public void bufferGrowth() throws IOException {
        // Create a buffer for big messages.
        buffer = new FixedLengthMessageBuffer(BIG_SIZE, channel, loop, null);
        buffer.queue(BAM);
        assertEquals(AM_NEQ, 0, buffer.growthCount);
        buffer.queue(BAM);
        assertEquals(AM_NEQ, 0, buffer.growthCount);
        buffer.queue(BAM);
        assertEquals(AM_NEQ, 1, buffer.growthCount);
        buffer.queue(BAM);
        assertEquals(AM_NEQ, 1, buffer.growthCount);
        buffer.queue(BAM);
        assertEquals(AM_NEQ, 2, buffer.growthCount);
    }

    @Test
    public void discardBeforeKey() {
        // Create a buffer that does not yet have the key set and discard it.
        buffer = loop.createBuffer(channel, null);
        assertNull(AM_HUH, buffer.key());
        buffer.discard();
        // There is not key, so nothing to check; we just expect no problem.
    }

    @Test
    public void bufferedRead() throws IOException {
        channel.bytesToRead = LENGTH + 4;
        List<FixedLengthMessage> messages = buffer.dequeue();
        assertEquals(AM_NEQ, 1, messages.size());
        assertState(WritePending.OFF, FlushRequired.OFF, LENGTH + 4, 0);

        channel.bytesToRead = LENGTH - 4;
        messages = buffer.dequeue();
        assertEquals(AM_NEQ, 1, messages.size());
        assertState(WritePending.OFF, FlushRequired.OFF, LENGTH * 2, 0);
    }

    @Test
    public void bufferedWrite() throws IOException {
        assertState(WritePending.OFF, FlushRequired.OFF, 0, 0);

        // First write is immediate...
        buffer.queue(M1);
        assertState(WritePending.OFF, FlushRequired.OFF, 0, LENGTH);

        // Second and third get buffered...
        buffer.queue(M2);
        assertState(WritePending.OFF, FlushRequired.ON, 0, LENGTH);
        buffer.queue(M3);
        assertState(WritePending.OFF, FlushRequired.ON, 0, LENGTH);

        // Reset write, which will flush if needed; the next write is again buffered
        buffer.flushIfWriteNotPending();
        assertState(WritePending.OFF, FlushRequired.OFF, 0, LENGTH * 3);
        buffer.queue(M4);
        assertState(WritePending.OFF, FlushRequired.ON, 0, LENGTH * 3);

        // Select reset, which will flush if needed; the next write is again buffered
        buffer.flushIfPossible();
        assertState(WritePending.OFF, FlushRequired.OFF, 0, LENGTH * 4);
        buffer.queue(M1);
        assertState(WritePending.OFF, FlushRequired.ON, 0, LENGTH * 4);
        buffer.flush();
        assertState(WritePending.OFF, FlushRequired.ON, 0, LENGTH * 4);
    }

    @Test
    public void bufferedWriteList() throws IOException {
        assertState(WritePending.OFF, FlushRequired.OFF, 0, 0);

        // First write is immediate...
        List<FixedLengthMessage> messages = new ArrayList<FixedLengthMessage>();
        messages.add(M1);
        messages.add(M2);
        messages.add(M3);
        messages.add(M4);

        buffer.queue(messages);
        assertState(WritePending.OFF, FlushRequired.OFF, 0, LENGTH * 4);

        buffer.queue(messages);
        assertState(WritePending.OFF, FlushRequired.ON, 0, LENGTH * 4);

        buffer.flushIfPossible();
        assertState(WritePending.OFF, FlushRequired.OFF, 0, LENGTH * 8);
    }

    @Test
    public void bufferedPartialWrite() throws IOException {
        assertState(WritePending.OFF, FlushRequired.OFF, 0, 0);

        // First write is immediate...
        buffer.queue(M1);
        assertState(WritePending.OFF, FlushRequired.OFF, 0, LENGTH);

        // Tell test channel to accept only half.
        channel.bytesToWrite = LENGTH/2;

        // Second and third get buffered...
        buffer.queue(M2);
        assertState(WritePending.OFF, FlushRequired.ON, 0, LENGTH);
        buffer.flushIfPossible();
        assertState(WritePending.ON, FlushRequired.ON, 0, LENGTH + LENGTH/2);
    }

    @Test
    public void bufferedPartialWrite2() throws IOException {
        assertState(WritePending.OFF, FlushRequired.OFF, 0, 0);

        // First write is immediate...
        buffer.queue(M1);
        assertState(WritePending.OFF, FlushRequired.OFF, 0, LENGTH);

        // Tell test channel to accept only half.
        channel.bytesToWrite = LENGTH/2;

        // Second and third get buffered...
        buffer.queue(M2);
        assertState(WritePending.OFF, FlushRequired.ON, 0, LENGTH);
        buffer.flushIfWriteNotPending();
        assertState(WritePending.ON, FlushRequired.ON, 0, LENGTH + LENGTH/2);
    }

    @Test
    public void bufferedReadWrite() throws IOException {
        channel.bytesToRead = LENGTH + 4;
        List<FixedLengthMessage> messages = buffer.dequeue();
        assertEquals(AM_NEQ, 1, messages.size());
        assertState(WritePending.OFF, FlushRequired.OFF, LENGTH + 4, 0);

        buffer.queue(M1);
        assertState(WritePending.OFF, FlushRequired.OFF, LENGTH + 4, LENGTH);

        channel.bytesToRead = LENGTH - 4;
        messages = buffer.dequeue();
        assertEquals(AM_NEQ, 1, messages.size());
        assertState(WritePending.OFF, FlushRequired.OFF, LENGTH * 2, LENGTH);
    }

    // Fake IO driver loop
    private static class FakeIOLoop extends
            IOLoop<FixedLengthMessage, FixedLengthMessageBuffer> {

        public FakeIOLoop() throws IOException {
            super();
        }

        @Override
        protected FixedLengthMessageBuffer createBuffer(ByteChannel ch,
                                                        SSLContext sslContext) {
            return new FixedLengthMessageBuffer(LENGTH, ch, this, null);
        }

        @Override
        protected void processMessages(MessageBuffer<FixedLengthMessage> b,
                                       List<FixedLengthMessage> messages) {
        }

    }

    // Fake byte channel
    private static class TestByteChannel extends SelectableChannel implements ByteChannel {

        private static final int BUFFER_LENGTH = 1024;
        byte bytes[] = new byte[BUFFER_LENGTH];
        int bytesToWrite = BUFFER_LENGTH;
        int bytesToRead = BUFFER_LENGTH;
        int writtenBytes = 0;
        int readBytes = 0;

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int l = Math.min(dst.remaining(), bytesToRead);
            if (bytesToRead > 0) {
                readBytes += l;
                dst.put(bytes, 0, l);
            }
            return l;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            int l = Math.min(src.remaining(), bytesToWrite);
            writtenBytes += l;
            src.get(bytes, 0, l);
            return l;
        }

        @Override
        public Object blockingLock() {
            return null;
        }

        @Override
        public SelectableChannel configureBlocking(boolean arg0) throws IOException {
            return null;
        }

        @Override
        public boolean isBlocking() {
            return false;
        }

        @Override
        public boolean isRegistered() {
            return false;
        }

        @Override
        public SelectionKey keyFor(Selector arg0) {
            return null;
        }

        @Override
        public SelectorProvider provider() {
            return null;
        }

        @Override
        public SelectionKey register(Selector arg0, int arg1, Object arg2)
                throws ClosedChannelException {
            return null;
        }

        @Override
        public int validOps() {
            return 0;
        }

        @Override
        protected void implCloseChannel() throws IOException {
            bytesToRead = -1;
        }

    }

    private static class TestKey extends SelectionKey {

        private SelectableChannel channel;

        public TestKey(TestByteChannel channel) {
            this.channel = channel;
        }

        @Override
        public void cancel() {
        }

        @Override
        public SelectableChannel channel() {
            return channel;
        }

        @Override
        public int interestOps() {
            return 0;
        }

        @Override
        public SelectionKey interestOps(int ops) {
            return null;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public int readyOps() {
            return 0;
        }

        @Override
        public Selector selector() {
            return null;
        }
    }

}
