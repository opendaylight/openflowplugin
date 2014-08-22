/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import javax.net.ssl.SSLContext;

/**
 * Fixed-length message transfer buffer.
 *
 * @author Thomas Vachuska
 */
public class FixedLengthMessageBuffer extends MessageBuffer<FixedLengthMessage> {
    
    private static final String E_WRONG_LEN = "Illegal message length: ";
    
    private final int length;
    int growthCount;
    
    /**
     * Create a new buffer for transferring messages of the specified length.
     * 
     * @param length message length
     * @param ch backing channel
     * @param loop driver loop
     * @param sslContext optional TLS context
     */
    public FixedLengthMessageBuffer(int length, ByteChannel ch, 
                                    IOLoop<FixedLengthMessage, ?> loop,
                                    SSLContext sslContext) {
        super(ch, loop, sslContext);
        this.length = length;
    }

    @Override
    protected FixedLengthMessage get(ByteBuffer rb) {
        if (rb.remaining() < length)
            return null;
        FixedLengthMessage message = new FixedLengthMessage(length);
        rb.get(message.data());
        return message;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation enforces the message length against the buffer
     * supported length.
     * 
     * @throws IllegalArgumentException if message size does not match the
     *         supported buffer size
     */
    @Override
    protected void put(FixedLengthMessage message, ByteBuffer wb) {
        if (message.length() != length)
            throw new IllegalArgumentException(E_WRONG_LEN + message.length());
        wb.put(message.data());
    }
    
    @Override 
    protected double growthFactor() {
        growthCount++;
        return 2.0;
    }
    
    @Override
    public int maxAge() {
        return 250;
    }
    
}
