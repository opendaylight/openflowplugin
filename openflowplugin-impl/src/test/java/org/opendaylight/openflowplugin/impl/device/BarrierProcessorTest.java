/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.OutstandingMessageExtractor;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Created by mirehak on 4/5/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class BarrierProcessorTest {

    private static final long XID = 42L;
    @Mock
    private OutstandingMessageExtractor messageExtractor;
    @Mock
    private RequestContext<String> extractedReqCtx;

    private SettableFuture<RpcResult<String>> settableFuture;

    @Before
    public void setUp() throws Exception {
        settableFuture = SettableFuture.create();
        Mockito.when(messageExtractor.extractNextOutstandingMessage(Matchers.anyLong()))
                .thenReturn(extractedReqCtx, extractedReqCtx, null);
        Mockito.when(extractedReqCtx.getFuture()).thenReturn(settableFuture);
        Mockito.when(extractedReqCtx.getXid()).thenReturn(new Xid(41L), new Xid(42L));
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verifyNoMoreInteractions(messageExtractor);
    }

    @Test
    public void testProcessOutstandingRequests() throws Exception {
        BarrierProcessor.processOutstandingRequests(XID, messageExtractor);

        Mockito.verify(messageExtractor, Mockito.times(3)).extractNextOutstandingMessage(XID);
        Assert.assertTrue(settableFuture.isCancelled());
    }
}