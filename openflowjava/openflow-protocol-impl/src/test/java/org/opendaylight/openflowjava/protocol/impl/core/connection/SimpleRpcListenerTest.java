/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * UNit tests for SimpleRpcListener.
 *
 * @author michal.polkorab
 */
public class SimpleRpcListenerTest {

    @Mock Future<Void> future;

    /**
     * Initializes mocks.
     */
    @Before
    public void startUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test SimpleRpcListener creation.
     */
    @Test
    public void test() {
        SimpleRpcListener<?> listener = new SimpleRpcListener<>("MESSAGE", "Failed to send message");
        Assert.assertEquals("Wrong message", "MESSAGE", listener.takeMessage());
        Assert.assertEquals("Wrong message", listener, listener.takeListener());
    }

    /**
     * Test rpc success.
     */
    @Test
    public void testSuccessfulRpc() {
        SimpleRpcListener<?> listener = new SimpleRpcListener<>("MESSAGE", "Failed to send message");
        listener.operationSuccessful();
        SettableFuture<RpcResult<?>> result = SettableFuture.create();
        result.set(RpcResultBuilder.success((Void)null).build());
        try {
            Assert.assertEquals("Wrong result", result.get().getErrors(), listener.getResult().get().getErrors());
            Assert.assertEquals("Wrong result", result.get().getResult(), listener.getResult().get().getResult());
            Assert.assertEquals("Wrong result", result.get().isSuccessful(), listener.getResult().get().isSuccessful());
        } catch (InterruptedException | ExecutionException e) {
            fail("Problem accessing result");
        }
    }

    /**
     * Test rpc success.
     */
    @Test
    public void testOperationComplete() {
        when(future.isSuccess()).thenReturn(false);
        SimpleRpcListener<?> listener = new SimpleRpcListener<>("MESSAGE", "Failed to send message");
        listener.operationComplete(future);
        verify(future, times(1)).cause();
        try {
            Assert.assertEquals("Wrong result", 1, listener.getResult().get().getErrors().size());
        } catch (InterruptedException | ExecutionException e) {
            Assert.fail();
        }
    }

    /**
     * Test rpc success.
     */
    @Test
    public void testOperationComplete2() {
        when(future.isSuccess()).thenReturn(true);
        SimpleRpcListener<?> listener = new SimpleRpcListener<>("MESSAGE", "Failed to send message");
        listener.operationComplete(future);
        verify(future, times(0)).cause();
        try {
            Assert.assertEquals("Wrong result", 0, listener.getResult().get().getErrors().size());
            Assert.assertEquals("Wrong result", true, listener.getResult().get().isSuccessful());
        } catch (InterruptedException | ExecutionException e) {
            Assert.fail();
        }
    }
}
