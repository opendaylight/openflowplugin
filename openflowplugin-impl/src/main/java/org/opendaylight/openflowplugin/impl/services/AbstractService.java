/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractService<I, O> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractService.class);
    private static final long WAIT_TIME = 2000;
    private static final BigInteger PRIMARY_CONNECTION = BigInteger.ZERO;

    private final short version;
    private final BigInteger datapathId;
    private final RequestContextStack requestContextStack;
    private final DeviceContext deviceContext;
    private final ConnectionAdapter primaryConnectionAdapter;
    private final MessageSpy messageSpy;
    private EventIdentifier eventIdentifier;

    public AbstractService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        this.requestContextStack = requestContextStack;
        this.deviceContext = deviceContext;
        final FeaturesReply features = this.deviceContext.getPrimaryConnectionContext().getFeatures();
        this.datapathId = features.getDatapathId();
        this.version = features.getVersion();
        this.primaryConnectionAdapter = deviceContext.getPrimaryConnectionContext().getConnectionAdapter();
        this.messageSpy = deviceContext.getMessageSpy();
    }

    public EventIdentifier getEventIdentifier() {
        return eventIdentifier;
    }

    public void setEventIdentifier(final EventIdentifier eventIdentifier) {
        this.eventIdentifier = eventIdentifier;
    }

    public short getVersion() {
        return version;
    }

    public BigInteger getDatapathId() {
        return datapathId;
    }

    public RequestContextStack getRequestContextStack() {
        return requestContextStack;
    }

    public DeviceContext getDeviceContext() {
        return deviceContext;
    }

    public MessageSpy getMessageSpy() {
        return messageSpy;
    }

    protected abstract OfHeader buildRequest(Xid xid, I input) throws Exception;

    protected abstract FutureCallback<OfHeader> createCallback(RequestContext<O> context, Class<?> requestType);

    public final ListenableFuture<RpcResult<O>> handleServiceCall(@Nonnull final I input) {
        Preconditions.checkNotNull(input);

        final Class<?> requestType;
        if (input instanceof DataContainer) {
            requestType = ((DataContainer) input).getImplementedInterface();
        } else {
            requestType = input.getClass();
        }
        getMessageSpy().spyMessage(requestType, MessageSpy.STATISTIC_GROUP.TO_SWITCH_ENTERED);

        LOG.trace("Handling general service call");
        final RequestContext<O> requestContext = requestContextStack.createRequestContext();
        if (requestContext == null) {
            LOG.trace("Request context refused.");
            deviceContext.getMessageSpy().spyMessage(AbstractService.class, MessageSpy.STATISTIC_GROUP.TO_SWITCH_DISREGARDED);
            return failedFuture();
        }

        if (requestContext.getXid() == null) {
            deviceContext.getMessageSpy().spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_RESERVATION_REJECTED);
            return RequestContextUtil.closeRequestContextWithRpcError(requestContext, "Outbound queue wasn't able to reserve XID.");
        }

        messageSpy.spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_READY_FOR_SUBMIT);

        final Xid xid = requestContext.getXid();
        OfHeader request = null;
        try {
            request = buildRequest(xid, input);
            Verify.verify(xid.getValue().equals(request.getXid()), "Expected XID %s got %s", xid.getValue(), request.getXid());
        } catch (Exception e) {
            LOG.error("Failed to build request for {}, forfeiting request {}", input, xid.getValue(), e);
            RequestContextUtil.closeRequestContextWithRpcError(requestContext, "failed to build request input: " + e.getMessage());
        } finally {
            final OutboundQueue outboundQueue = getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider();
            outboundQueue.commitEntry(xid.getValue(), request, createCallback(requestContext, requestType));
        }

        return requestContext.getFuture();
    }

    protected static <T> ListenableFuture<RpcResult<T>> failedFuture() {
        final RpcResult<T> rpcResult = RpcResultBuilder.<T>failed()
                .withError(RpcError.ErrorType.APPLICATION, "", "Request quota exceeded").build();
        return Futures.immediateFuture(rpcResult);
    }
}
