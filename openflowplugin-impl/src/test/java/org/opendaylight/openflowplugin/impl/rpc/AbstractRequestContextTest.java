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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractRequestContextTest {
    private AbstractRequestContext<Object> requestContext;

    @Before
    public void setup() {
        requestContext = new AbstractRequestContext<Object>(1L) {
            @Override
            public void close() {
                // No-op
            }
        };
    }

    @Test
    public void testCreateRequestFuture() throws Exception {
        Future<?> future = requestContext.getFuture();
        assertNotNull(future);
    }
}