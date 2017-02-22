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
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.services.util.RequestContextUtil;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractService<I, O> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractService.class);

    private final short version;
    private final BigInteger datapathId;
    private final RequestContextStack requestContextStack;
    private final DeviceContext deviceContext;
    private final MessageSpy messageSpy;
    private EventIdentifier eventIdentifier;

    AbstractService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        final DeviceInfo deviceInfo = deviceContext.getDeviceInfo();

        this.requestContextStack = requestContextStack;
        this.deviceContext = deviceContext;
        this.datapathId = deviceInfo.getDatapathId();
        this.version = deviceInfo.getVersion();
        this.messageSpy = deviceContext.getMessageSpy();
    }

    public boolean canUseSingleLayerSerialization() {
        return deviceContext.canUseSingleLayerSerialization();
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

    @Deprecated
    public DeviceContext getDeviceContext() {
        return deviceContext;
    }

    public DeviceRegistry getDeviceRegistry() {
        return deviceContext;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceContext.getDeviceInfo();
    }

    public TxFacade getTxFacade() {
        return deviceContext;
    }

    public MessageSpy getMessageSpy() {
        return messageSpy;
    }

    protected abstract OfHeader buildRequest(Xid xid, I input) throws ServiceException;

    protected abstract FutureCallback<OfHeader> createCallback(RequestContext<O> context, Class<?> requestType);

    public ListenableFuture<RpcResult<O>> handleServiceCall(@Nonnull final I input) {
        return handleServiceCall(input, null);
    }

    public ListenableFuture<RpcResult<O>> handleServiceCall(@Nonnull final I input,
            @Nullable final Function<OfHeader, Boolean> isComplete) {
        Preconditions.checkNotNull(input);

        final Class<?> requestType = input instanceof DataContainer
            ? DataContainer.class.cast(input).getImplementedInterface()
            : input.getClass();

        getMessageSpy().spyMessage(requestType, MessageSpy.STATISTIC_GROUP.TO_SWITCH_ENTERED);

        LOG.trace("Handling general service call");
        final RequestContext<O> requestContext = requestContextStack.createRequestContext();

        if (Objects.isNull(requestContext)) {
            LOG.trace("Request context refused.");
            getMessageSpy().spyMessage(AbstractService.class, MessageSpy.STATISTIC_GROUP.TO_SWITCH_DISREGARDED);
            return Futures.immediateFuture(RpcResultBuilder
                    .<O>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "", "Request quota exceeded")
                    .build());
        }

        if (Objects.isNull(requestContext.getXid())) {
            getMessageSpy().spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_RESERVATION_REJECTED);
            return RequestContextUtil.closeRequestContextWithRpcError(requestContext, "Outbound queue wasn't able to reserve XID.");
        }

        getMessageSpy().spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_READY_FOR_SUBMIT);

        final Xid xid = requestContext.getXid();
        OfHeader request = null;
        try {
            request = buildRequest(xid, input);
            Verify.verify(xid.getValue().equals(request.getXid()), "Expected XID %s got %s", xid.getValue(), request.getXid());
        } catch (Exception ex) {
            LOG.error("Failed to build request for {}, forfeiting request {}", input, xid.getValue(), ex);
            RequestContextUtil.closeRequestContextWithRpcError(requestContext, "failed to build request input: " + ex.getMessage());
        } finally {
            final OutboundQueue outboundQueue = getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider();

            if (Objects.nonNull(isComplete)) {
                outboundQueue.commitEntry(xid.getValue(), request, createCallback(requestContext, requestType), isComplete);
            } else {
                outboundQueue.commitEntry(xid.getValue(), request, createCallback(requestContext, requestType));
            }
        }

        return requestContext.getFuture();
    }
}
