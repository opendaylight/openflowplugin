/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractService<I, O> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractService.class);

    private final @NonNull Uint8 version;
    private final Uint64 datapathId;
    private final RequestContextStack requestContextStack;
    private final DeviceContext deviceContext;
    private final MessageSpy messageSpy;
    private EventIdentifier eventIdentifier;

    AbstractService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        final DeviceInfo deviceInfo = deviceContext.getDeviceInfo();

        this.requestContextStack = requestContextStack;
        this.deviceContext = deviceContext;
        datapathId = deviceInfo.getDatapathId();
        version = deviceInfo.getVersion();
        messageSpy = deviceContext.getMessageSpy();
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

    public final @NonNull Uint8 getVersion() {
        return version;
    }

    public final Uint64 getDatapathId() {
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

    public @NonNull ListenableFuture<RpcResult<O>> handleServiceCall(@NonNull final I input) {
        return handleServiceCall(input, null);
    }

    public @NonNull ListenableFuture<RpcResult<O>> handleServiceCall(@NonNull final I input,
            @Nullable final Function<OfHeader, Boolean> isComplete) {
        requireNonNull(input);

        final Class<?> requestType = input instanceof DataContainer
            ? ((DataContainer) input).implementedInterface()
            : input.getClass();

        getMessageSpy().spyMessage(requestType, MessageSpy.StatisticsGroup.TO_SWITCH_ENTERED);

        LOG.trace("Handling general service call");
        final RequestContext<O> requestContext = requestContextStack.createRequestContext();

        if (requestContext == null) {
            LOG.trace("Request context refused.");
            getMessageSpy().spyMessage(AbstractService.class, MessageSpy.StatisticsGroup.TO_SWITCH_DISREGARDED);
            return Futures.immediateFuture(RpcResultBuilder
                    .<O>failed()
                    .withError(ErrorType.APPLICATION, ErrorTag.ACCESS_DENIED, "Request quota exceeded")
                    .build());
        }

        if (requestContext.getXid() == null) {
            getMessageSpy().spyMessage(requestContext.getClass(),
                                       MessageSpy.StatisticsGroup.TO_SWITCH_RESERVATION_REJECTED);
            return RequestContextUtil
                    .closeRequestContextWithRpcError(requestContext, "Outbound queue wasn't able to reserve XID.");
        }

        getMessageSpy().spyMessage(requestContext.getClass(), MessageSpy.StatisticsGroup.TO_SWITCH_READY_FOR_SUBMIT);

        final Xid xid = requestContext.getXid();
        OfHeader request = null;
        try {
            request = buildRequest(xid, input);
            verify(xid.getValue().equals(request.getXid()),
                          "Expected XID %s got %s",
                          xid.getValue(),
                          request.getXid());
        } catch (ServiceException ex) {
            LOG.error("Failed to build request for {}, forfeiting request {}", input, xid.getValue(), ex);
            RequestContextUtil.closeRequestContextWithRpcError(requestContext,
                                                               "failed to build request input: " + ex.getMessage());
        } finally {
            final OutboundQueue outboundQueue =
                    getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider();

            final Uint32 queueXid = xid.getValue();
            if (isComplete != null) {
                outboundQueue.commitEntry(queueXid, request, createCallback(requestContext, requestType), isComplete);
            } else {
                outboundQueue.commitEntry(queueXid, request, createCallback(requestContext, requestType));
            }
        }

        return requestContext.getFuture();
    }
}
