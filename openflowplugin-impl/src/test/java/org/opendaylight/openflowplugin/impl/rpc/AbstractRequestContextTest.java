/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.rpc;

import static org.junit.Assert.assertNotNull;
import java.util.concurrent.Future;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;

@RunWith(MockitoJUnitRunner.class)
public class AbstractRequestContextTest {

    @Mock
    RpcContext rpcContext;

    @Mock
    AbstractRequestContext<Object> requestContext;

    @Test
    public void testCreateRequestFuture() throws Exception {
        Future<?> future = requestContext.getFuture();
        assertNotNull(future);
    }
}