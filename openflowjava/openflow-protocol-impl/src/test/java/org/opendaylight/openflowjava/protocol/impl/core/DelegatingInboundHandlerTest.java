/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageConsumer;
import org.opendaylight.yangtools.binding.DataObject;

/**
 * Unit tests for DelegatingInboundHandler.
 *
 * @author jameshall
 */
@RunWith(MockitoJUnitRunner.class)
public class DelegatingInboundHandlerTest {

    @Mock ChannelHandlerContext mockChHndlrCtx;
    @Mock MessageConsumer mockMsgConsumer;
    @Mock DataObject mockDataObject ;

    DelegatingInboundHandler dih;

    /**
     * Sets up test environment.
     */
    @Before
    public void setUp() {
        dih = new DelegatingInboundHandler(mockMsgConsumer) ;
    }

    @Test
    public void testChannelReadSuccess()   {
        dih.channelRead(mockChHndlrCtx, mockDataObject) ;

        // Verify that the message buf was released...
        verify(mockMsgConsumer, times(1)).consume(mockDataObject);
    }

    @Test
    public void testChannelInactive()   {
        dih.channelInactive(mockChHndlrCtx);

        verify(mockMsgConsumer, times(1)).consume(any(DataObject.class));
    }

    @Test
    public void testChannelUnregistered()   {
        dih.channelUnregistered(mockChHndlrCtx);

        verify(mockMsgConsumer, times(1)).consume(any(DataObject.class));
    }
}
