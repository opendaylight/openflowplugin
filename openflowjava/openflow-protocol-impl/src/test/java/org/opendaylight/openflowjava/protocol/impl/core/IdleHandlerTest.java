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
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;

/**
 * Unit tests for IdleHandler.
 *
 * @author jameshall
 */
public class IdleHandlerTest {

    @Mock ChannelHandlerContext mockChHndlrCtx ;

    IdleHandler idleHandler ;

    /**
     * Sets up test environment.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        idleHandler = new IdleHandler(60L, TimeUnit.MINUTES);
    }

    /**
     * Test message passing on channel read.
     */
    @Test
    public void testChannelRead() throws Exception {
        idleHandler.channelRead(mockChHndlrCtx, new Object());

        // Verify that a read was fired for the next handler ...
        verify(mockChHndlrCtx, times(1)).fireChannelRead(Mockito.<SwitchIdleEvent>any()) ;
    }

    /**
     * Test channel read timeout.
     */
    @Test
    public void testReadTimedOut() throws Exception {
        idleHandler.readTimedOut(mockChHndlrCtx);

        // Verify a read was fired for the next handler to process ...
        verify(mockChHndlrCtx, times(1)).fireChannelRead(any(SwitchIdleEvent.class)) ;
    }

    /**
     * Test only one timeout notification.
     */
    @Test
    public void testReadTimedOutNoOpNotFirst() throws Exception {
        idleHandler.readTimedOut(mockChHndlrCtx);
        idleHandler.readTimedOut(mockChHndlrCtx);

        // Verify that only one notification was sent to the next handler ...
        verify(mockChHndlrCtx, times(1)).fireChannelRead(any(Object.class)) ;
    }

    /**
     * Test two timeout notifications.
     */
    @Test
    public void testReadTimedOutTwice() throws Exception {
        idleHandler.readTimedOut(mockChHndlrCtx);
        verify(mockChHndlrCtx, times(1)).fireChannelRead(any(Object.class));

        idleHandler.channelRead(mockChHndlrCtx, new String());
        verify(mockChHndlrCtx, times(2)).fireChannelRead(any(Object.class));

        idleHandler.readTimedOut(mockChHndlrCtx);
        verify(mockChHndlrCtx, times(3)).fireChannelRead(any(Object.class));
    }
}
