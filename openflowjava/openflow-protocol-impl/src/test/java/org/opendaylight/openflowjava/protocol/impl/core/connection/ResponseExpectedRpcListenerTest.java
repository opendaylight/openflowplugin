/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.junit.Assert.fail;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * @author michal.polkorab
 *
 */
public class ResponseExpectedRpcListenerTest {

    private static final RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>> REMOVAL_LISTENER =
            new RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>>() {
        @Override
        public void onRemoval(
                final RemovalNotification<RpcResponseKey, ResponseExpectedRpcListener<?>> notification) {
            notification.getValue().discard();
        }
    };
    private static final int RPC_RESPONSE_EXPIRATION = 1;
    private final Cache<RpcResponseKey, ResponseExpectedRpcListener<?>> responseCache  = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .expireAfterWrite(RPC_RESPONSE_EXPIRATION, TimeUnit.MINUTES)
            .removalListener(REMOVAL_LISTENER).build();

    /**
     * Test object creation
     */
    @Test(expected=NullPointerException.class)
    public void testCreation() {
        RpcResponseKey key = new RpcResponseKey(12345L, BarrierOutput.class.getName());
        new ResponseExpectedRpcListener<>("MESSAGE", "Failed to send message", null, key);
    }

    /**
     * Test object creation
     */
    @Test(expected=NullPointerException.class)
    public void testCreation2() {
        new ResponseExpectedRpcListener<>("MESSAGE", "Failed to send message", responseCache, null);
    }

    /**
     * Test object creation
     */
    @Test
    public void testDiscard() {
        RpcResponseKey key = new RpcResponseKey(12345L, BarrierOutput.class.getName());
        ResponseExpectedRpcListener<OfHeader> listener =
                new ResponseExpectedRpcListener<>("MESSAGE", "Failed to send message", responseCache, key);
        listener.discard();
        RpcError rpcError = AbstractRpcListener.buildRpcError("Failed to send message",
                "check switch connection", new TimeoutException("Request timed out"));
        SettableFuture<RpcResult<?>> result = SettableFuture.create();
        result.set(RpcResultBuilder.failed().withRpcError(rpcError).build());
        try {
            Assert.assertEquals("Wrong result", result.get().getErrors().iterator().next().getMessage(),
                    listener.getResult().get().getErrors().iterator().next().getMessage());
            Assert.assertEquals("Wrong result", result.get().getResult(), listener.getResult().get().getResult());
            Assert.assertEquals("Wrong result", result.get().isSuccessful(), listener.getResult().get().isSuccessful());
        } catch (InterruptedException | ExecutionException e) {
            fail("Problem accessing result");
        }
    }

    /**
     * Test object creation
     */
    @Test
    public void testCompleted() {
        RpcResponseKey key = new RpcResponseKey(12345L, BarrierOutput.class.getName());
        ResponseExpectedRpcListener<OfHeader> listener =
                new ResponseExpectedRpcListener<>("MESSAGE", "Failed to send message", responseCache, key);
        BarrierInputBuilder barrierBuilder = new BarrierInputBuilder();
        BarrierInput barrierInput = barrierBuilder.build();
        listener.completed(barrierInput);
        SettableFuture<RpcResult<?>> result = SettableFuture.create();
        result.set(RpcResultBuilder.success(barrierInput).build());
        try {
            Assert.assertEquals("Wrong result", result.get().getErrors(), listener.getResult().get().getErrors());
            Assert.assertEquals("Wrong result", result.get().getResult(), listener.getResult().get().getResult());
            Assert.assertEquals("Wrong result", result.get().isSuccessful(), listener.getResult().get().isSuccessful());
        } catch (InterruptedException | ExecutionException e) {
            fail("Problem accessing result");
        }
    }

    /**
     * Test object creation
     */
    @Test
    public void testOperationSuccessful() {
        RpcResponseKey key = new RpcResponseKey(12345L, BarrierOutput.class.getName());
        ResponseExpectedRpcListener<OfHeader> listener =
                new ResponseExpectedRpcListener<>("MESSAGE", "Failed to send message", responseCache, key);
        listener.operationSuccessful();
        ResponseExpectedRpcListener<?> present = responseCache.getIfPresent(key);
        Assert.assertEquals(present, listener);
    }
}