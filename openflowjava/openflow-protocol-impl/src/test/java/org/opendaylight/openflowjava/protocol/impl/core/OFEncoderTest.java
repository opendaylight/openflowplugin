/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageListenerWrapper;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OFEncoder.
 *
 * @author jameshall
 */
@RunWith(MockitoJUnitRunner.class)
public class OFEncoderTest {

    @Mock ChannelHandlerContext mockChHndlrCtx ;
    @Mock SerializationFactory mockSerializationFactory ;
    @Mock MessageListenerWrapper wrapper;
    @Mock OfHeader mockMsg ;
    @Mock ByteBuf mockOut ;
    @Mock Future<Void> future;
    @Mock GenericFutureListener<Future<Void>> listener;

    OFEncoder ofEncoder = new OFEncoder() ;

    /**
     * Sets up test environment.
     */
    @Before
    public void setUp() {
        ofEncoder = new OFEncoder() ;
        ofEncoder.setSerializationFactory(mockSerializationFactory);
    }

    /**
     * Test successful write (no clear).
     */
    @Test
    public void testEncodeSuccess() throws Exception {
        when(wrapper.getMsg()).thenReturn(mockMsg);
        when(wrapper.getMsg().getVersion()).thenReturn(Uint8.valueOf(EncodeConstants.OF13_VERSION_ID));

        ofEncoder.encode(mockChHndlrCtx, wrapper, mockOut);

        // Verify that the channel was flushed after the ByteBuf was retained.
        verify(mockOut, times(0)).clear();
    }

    /**
     * Test Bytebuf clearing after serialization failure.
     */
    @Test
    public void testEncodeSerializationException() throws Exception {
        when(wrapper.getMsg()).thenReturn(mockMsg);
        when(wrapper.getListener()).thenReturn(listener);
        when(wrapper.getMsg().getVersion()).thenReturn(Uint8.valueOf(EncodeConstants.OF13_VERSION_ID));
        doThrow(new IllegalArgumentException()).when(mockSerializationFactory).messageToBuffer(any(Uint8.class),
                any(ByteBuf.class), any(OfHeader.class));

        ofEncoder.encode(mockChHndlrCtx, wrapper, mockOut);

        // Verify that the output message buf was cleared...
        verify(mockOut, times(1)).clear();
    }

    /**
     * Test no action on empty bytebuf.
     */
    @Test
    public void testEncodeSerializesNoBytes() throws Exception {
        when(wrapper.getMsg()).thenReturn(mockMsg);
        when(wrapper.getMsg().getVersion()).thenReturn(Uint8.valueOf(EncodeConstants.OF13_VERSION_ID));

        ofEncoder.encode(mockChHndlrCtx, wrapper, mockOut);

        // Verify that the output message buf was cleared...
        verify(mockOut, times(0)).clear();
        verify(mockChHndlrCtx, times(0)).writeAndFlush(mockOut);
        verify(mockOut, times(0)).retain();
    }
}
