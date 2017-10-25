/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConnectionAdapter} interface contains couple of OF message handling approaches.
 * {@link AbstractConnectionAdapter} class contains direct RPC processing from OpenflowProtocolService
 * {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolService}
 */
abstract class AbstractConnectionAdapter implements ConnectionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractConnectionAdapter.class);

    /** after this time, RPC future response objects will be thrown away (in minutes) */
    private static final int RPC_RESPONSE_EXPIRATION = 1;

    private static final Exception QUEUE_FULL_EXCEPTION = new RejectedExecutionException("Output queue is full");

    /**
     * Default depth of write queue, e.g. we allow these many messages
     * to be queued up before blocking producers.
     */
    private static final int DEFAULT_QUEUE_DEPTH = 1024;

    protected static final RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>> REMOVAL_LISTENER = new RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>>() {
        @Override
        public void onRemoval(final RemovalNotification<RpcResponseKey, ResponseExpectedRpcListener<?>> notification) {
            if (!notification.getCause().equals(RemovalCause.EXPLICIT)) {
                notification.getValue().discard();
            }
        }
    };

    protected final Channel channel;
    protected final InetSocketAddress address;
    protected boolean disconnectOccured = false;
    protected final ChannelOutboundQueue output;

    /** expiring cache for future rpcResponses */
    protected Cache<RpcResponseKey, ResponseExpectedRpcListener<?>> responseCache;


    AbstractConnectionAdapter(@Nonnull final Channel channel, @Nullable final InetSocketAddress address) {
        this.channel = Preconditions.checkNotNull(channel);
        this.address = address;

        responseCache = CacheBuilder.newBuilder().concurrencyLevel(1)
                .expireAfterWrite(RPC_RESPONSE_EXPIRATION, TimeUnit.MINUTES).removalListener(REMOVAL_LISTENER).build();
        this.output = new ChannelOutboundQueue(channel, DEFAULT_QUEUE_DEPTH, address);
        channel.pipeline().addLast(output);
    }

    @Override
    public Future<Boolean> disconnect() {
        final ChannelFuture disconnectResult = channel.disconnect();
        responseCache.invalidateAll();
        disconnectOccured = true;

        return handleTransportChannelFuture(disconnectResult);
    }

    @Override
    public Future<RpcResult<BarrierOutput>> barrier(final BarrierInput input) {
        return sendToSwitchExpectRpcResultFuture(input, BarrierOutput.class, "barrier-input sending failed");
    }

    @Override
    public Future<RpcResult<EchoOutput>> echo(final EchoInput input) {
        return sendToSwitchExpectRpcResultFuture(input, EchoOutput.class, "echo-input sending failed");
    }

    @Override
    public Future<RpcResult<Void>> echoReply(final EchoReplyInput input) {
        return sendToSwitchFuture(input, "echo-reply sending failed");
    }

    @Override
    public Future<RpcResult<Void>> experimenter(final ExperimenterInput input) {
        return sendToSwitchFuture(input, "experimenter sending failed");
    }

    @Override
    public Future<RpcResult<Void>> flowMod(final FlowModInput input) {
        return sendToSwitchFuture(input, "flow-mod sending failed");
    }

    @Override
    public Future<RpcResult<GetConfigOutput>> getConfig(final GetConfigInput input) {
        return sendToSwitchExpectRpcResultFuture(input, GetConfigOutput.class, "get-config-input sending failed");
    }

    @Override
    public Future<RpcResult<GetFeaturesOutput>> getFeatures(final GetFeaturesInput input) {
        return sendToSwitchExpectRpcResultFuture(input, GetFeaturesOutput.class, "get-features-input sending failed");
    }

    @Override
    public Future<RpcResult<GetQueueConfigOutput>> getQueueConfig(final GetQueueConfigInput input) {
        return sendToSwitchExpectRpcResultFuture(input, GetQueueConfigOutput.class,
                "get-queue-config-input sending failed");
    }

    @Override
    public Future<RpcResult<Void>> groupMod(final GroupModInput input) {
        return sendToSwitchFuture(input, "group-mod-input sending failed");
    }

    @Override
    public Future<RpcResult<Void>> hello(final HelloInput input) {
        return sendToSwitchFuture(input, "hello-input sending failed");
    }

    @Override
    public Future<RpcResult<Void>> meterMod(final MeterModInput input) {
        return sendToSwitchFuture(input, "meter-mod-input sending failed");
    }

    @Override
    public Future<RpcResult<Void>> packetOut(final PacketOutInput input) {
        return sendToSwitchFuture(input, "packet-out-input sending failed");
    }

    @Override
    public Future<RpcResult<Void>> multipartRequest(final MultipartRequestInput input) {
        return sendToSwitchFuture(input, "multi-part-request sending failed");
    }

    @Override
    public Future<RpcResult<Void>> portMod(final PortModInput input) {
        return sendToSwitchFuture(input, "port-mod-input sending failed");
    }

    @Override
    public Future<RpcResult<RoleRequestOutput>> roleRequest(final RoleRequestInput input) {
        return sendToSwitchExpectRpcResultFuture(input, RoleRequestOutput.class,
                "role-request-config-input sending failed");
    }

    @Override
    public Future<RpcResult<Void>> setConfig(final SetConfigInput input) {
        return sendToSwitchFuture(input, "set-config-input sending failed");
    }

    @Override
    public Future<RpcResult<Void>> tableMod(final TableModInput input) {
        return sendToSwitchFuture(input, "table-mod-input sending failed");
    }

    @Override
    public Future<RpcResult<GetAsyncOutput>> getAsync(final GetAsyncInput input) {
        return sendToSwitchExpectRpcResultFuture(input, GetAsyncOutput.class, "get-async-input sending failed");
    }

    @Override
    public Future<RpcResult<Void>> setAsync(final SetAsyncInput input) {
        return sendToSwitchFuture(input, "set-async-input sending failed");
    }

    @Override
    public boolean isAlive() {
        return channel.isOpen();
    }

    @Override
    public boolean isAutoRead() {
        return channel.config().isAutoRead();
    }

    @Override
    public void setAutoRead(final boolean autoRead) {
        channel.config().setAutoRead(autoRead);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    /**
     * Used only for testing purposes
     * @param cache replacement
     */
    @VisibleForTesting
    void setResponseCache(final Cache<RpcResponseKey, ResponseExpectedRpcListener<?>> cache) {
        this.responseCache = cache;
    }

    /**
     * Return cached RpcListener or {@code null} if not cached
     * @return
     */
    protected ResponseExpectedRpcListener<?> findRpcResponse(final RpcResponseKey key) {
        return responseCache.getIfPresent(key);
    }

    /**
     * sends given message to switch, sending result or switch response will be reported via return value
     *
     * @param input message to send
     * @param responseClazz type of response
     * @param failureInfo describes, what type of message caused failure by sending
     * @return future object,
     *         <ul>
     *         <li>if send fails, {@link RpcResult} will contain errors and failed status</li>
     *         <li>else {@link RpcResult} will be stored in responseCache and wait for particular timeout (
     *         {@link ConnectionAdapterImpl#RPC_RESPONSE_EXPIRATION}),
     *         <ul>
     *         <li>either switch will manage to answer and then corresponding response message will be set into returned
     *         future</li>
     *         <li>or response in cache will expire and returned future will be cancelled</li>
     *         </ul>
     *         </li>
     *         </ul>
     */
    protected <IN extends OfHeader, OUT extends OfHeader> ListenableFuture<RpcResult<OUT>> sendToSwitchExpectRpcResultFuture(
            final IN input, final Class<OUT> responseClazz, final String failureInfo) {
        final RpcResponseKey key = new RpcResponseKey(input.getXid(), responseClazz.getName());
        final ResponseExpectedRpcListener<OUT> listener = new ResponseExpectedRpcListener<>(input, failureInfo,
                responseCache, key);
        return enqueueMessage(listener);
    }

    /**
     * sends given message to switch, sending result will be reported via return value
     *
     * @param input message to send
     * @param failureInfo describes, what type of message caused failure by sending
     * @return future object,
     *         <ul>
     *         <li>if send successful, {@link RpcResult} without errors and successful status will be returned,</li>
     *         <li>else {@link RpcResult} will contain errors and failed status</li>
     *         </ul>
     */
    protected ListenableFuture<RpcResult<Void>> sendToSwitchFuture(final DataObject input, final String failureInfo) {
        return enqueueMessage(new SimpleRpcListener(input, failureInfo));
    }

    private <T> ListenableFuture<RpcResult<T>> enqueueMessage(final AbstractRpcListener<T> promise) {
        LOG.debug("Submitting promise {}", promise);

        if (!output.enqueue(promise)) {
            LOG.debug("Message queue is full, rejecting execution");
            promise.failedRpc(QUEUE_FULL_EXCEPTION);
        } else {
            LOG.debug("Promise enqueued successfully");
        }

        return promise.getResult();
    }

    /**
     * @param resultFuture
     * @param failureInfo
     * @param errorSeverity
     * @param message
     * @return
     */
    private static SettableFuture<Boolean> handleTransportChannelFuture(final ChannelFuture resultFuture) {

        final SettableFuture<Boolean> transportResult = SettableFuture.create();

        resultFuture.addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super Void>>() {

            @Override
            public void operationComplete(final io.netty.util.concurrent.Future<? super Void> future) throws Exception {
                transportResult.set(future.isSuccess());
                if (!future.isSuccess()) {
                    transportResult.setException(future.cause());
                }
            }
        });
        return transportResult;
    }
}
