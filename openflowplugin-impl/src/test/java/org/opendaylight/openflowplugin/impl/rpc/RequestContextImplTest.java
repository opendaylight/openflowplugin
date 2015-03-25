/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.rpc;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;

@RunWith(MockitoJUnitRunner.class)
public class RequestContextImplTest {

    @Mock
    RpcContext rpcContext;

    RequestContext requestContext;

    @Before
    public void setup() {
        requestContext = new RequestContextImpl<>(rpcContext);
    }

    @Test
    public void testCreateRequestFuture() throws Exception {
        SettableFuture future = requestContext.getFuture();
        assertNotNull(future);
    }

    @Test
    public void testClose() throws Exception {
        requestContext.close();
        verify(rpcContext).forgetRequestContext(Matchers.any(RequestContext.class));
    }

}