/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;

@RunWith(MockitoJUnitRunner.class)
public class RequestContextUtilTest {
    @Mock
    private RequestContext<Void> requestContext;

    @After
    public void tearDown() {
        verifyNoMoreInteractions(requestContext);
    }

    @Test
    public void closeRequestContextWithRpcError() {
        final String errorMessage = "Test error";
        RequestContextUtil.closeRequestContextWithRpcError(
                requestContext,
                errorMessage);

        verify(requestContext).setResult(any());
        verify(requestContext).getFuture();
        verify(requestContext).close();
    }

    @Test
    public void closeRequestContext() {
        doThrow(new IllegalStateException()).when(requestContext).close();
        RequestContextUtil.closeRequestContext(requestContext);
        verify(requestContext).close();
    }

}