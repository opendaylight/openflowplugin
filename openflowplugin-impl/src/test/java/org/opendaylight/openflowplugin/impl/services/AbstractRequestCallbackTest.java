/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.connection.DeviceRequestFailedException;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class AbstractRequestCallbackTest {

    private static final Class<?> DUMMY_REQUEST_TYPE = String.class;
    private static final String DUMMY_DEVICE_ID = "DEVICE ID";
    private static final String DUMMY_EVENT_NAME = "EVENT NAME";
    private static final EventIdentifier DUMMY_EVENT_IDENTIFIER = new EventIdentifier(DUMMY_EVENT_NAME, DUMMY_DEVICE_ID);
    private static final String DUMMY_EXCEPTION_DESCRIPTION = "dummy exception description";
    private static final Long DUMMY_XID = 100L;
    private static final String DUMMY_MESSAGE_ILLEGAL_STATE_EXCEPTION = "dummy illegal state exception";

    AbstractRequestContext dummyRequestContext;
    AbstractRequestCallback abstractRequestCallback;

    @Before
    public void initialization() {
        dummyRequestContext = new AbstractRequestContext(DUMMY_XID) {
            @Override
            public void close() {

            }
        };

        abstractRequestCallback = new AbstractRequestCallback(dummyRequestContext,
                DUMMY_REQUEST_TYPE, new MessageIntelligenceAgencyImpl(),
                DUMMY_EVENT_IDENTIFIER) {
            @Override
            public void onSuccess(Object o) {

            }

        };

    }

    @Test
    public void testOnFailureWithDeviceRequestFailedException() throws Exception {
        ErrorMessage dummyErrorMessage = new ErrorMessageBuilder().build();
        abstractRequestCallback.onFailure(new DeviceRequestFailedException(DUMMY_EXCEPTION_DESCRIPTION, dummyErrorMessage));
        final ListenableFuture futureResult = dummyRequestContext.getFuture();

        RpcError rpcError = provideRpcError(futureResult);
        assertEquals("Device reported error type null code null", rpcError.getMessage());
    }

    @Test
    public void testOnFailure() throws Exception {
        ErrorMessage dummyErrorMessage = new ErrorMessageBuilder().build();
        abstractRequestCallback.onFailure(new IllegalStateException(DUMMY_MESSAGE_ILLEGAL_STATE_EXCEPTION));
        final ListenableFuture futureResult = dummyRequestContext.getFuture();

        RpcError rpcError = provideRpcError(futureResult);
        assertEquals(DUMMY_MESSAGE_ILLEGAL_STATE_EXCEPTION, rpcError.getMessage());

    }

    private RpcError provideRpcError(ListenableFuture futureResult) throws InterruptedException, java.util.concurrent.ExecutionException {
        final Object result = futureResult.get();
        assertTrue(result instanceof RpcResult);
        RpcResult rpcResult = (RpcResult) result;
        final Collection errors = rpcResult.getErrors();
        assertEquals(1, errors.size());
        final Object error = errors.iterator().next();
        assertTrue(error instanceof RpcError);
        return (RpcError) error;
    }


}
