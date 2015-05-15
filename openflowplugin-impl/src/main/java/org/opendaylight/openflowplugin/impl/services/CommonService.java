/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public abstract class CommonService {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CommonService.class);
    private static final long WAIT_TIME = 2000;
    private final static Future<RpcResult<Void>> ERROR_RPC_RESULT = Futures.immediateFuture(RpcResultBuilder
            .<Void>failed().withError(ErrorType.APPLICATION, "", "Request quota exceeded.").build());


    private static final BigInteger PRIMARY_CONNECTION = BigInteger.ZERO;

    private final short version;
    private final BigInteger datapathId;
    private final RequestContextStack requestContextStack;
    private final DeviceContext deviceContext;
    private final ConnectionAdapter primaryConnectionAdapter;
    private final MessageSpy messageSpy;


    public CommonService(final RequestContextStack requestContextStack, DeviceContext deviceContext) {
        this.requestContextStack = requestContextStack;
        this.deviceContext = deviceContext;
        final FeaturesReply features = this.deviceContext.getPrimaryConnectionContext().getFeatures();
        this.datapathId = features.getDatapathId();
        this.version = features.getVersion();
        this.primaryConnectionAdapter = deviceContext.getPrimaryConnectionContext().getConnectionAdapter();
        this.messageSpy = deviceContext.getMessageSpy();
    }

    public static BigInteger getPrimaryConnection() {
        return PRIMARY_CONNECTION;
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

    public ConnectionAdapter getPrimaryConnectionAdapter() {
        return primaryConnectionAdapter;
    }

    public MessageSpy getMessageSpy() {
        return messageSpy;
    }

    protected long provideWaitTime() {
        return WAIT_TIME;
    }


    protected ConnectionAdapter provideConnectionAdapter(final BigInteger connectionID) {
        if (connectionID == null) {
            return primaryConnectionAdapter;
        }
        if (connectionID.equals(PRIMARY_CONNECTION)) {
            return primaryConnectionAdapter;
        }

        final ConnectionContext auxiliaryConnectionContext =
                deviceContext.getAuxiliaryConnectiobContexts(connectionID);
        if (auxiliaryConnectionContext != null) {
            return auxiliaryConnectionContext.getConnectionAdapter();
        }

        return primaryConnectionAdapter;
    }

    /**
     * @param connectionID connection identifier
     * @param function     data sender
     * @param <T>          rpc result backend type
     * @param <F>          final rpc backend type
     * @return
     */
    public <T, F> ListenableFuture<RpcResult<T>> handleServiceCall(final BigInteger connectionID,
                                                                   final Function<DataCrate<T>, ListenableFuture<RpcResult<F>>> function) {
        DataCrateBuilder<T> dataCrateBuilder = DataCrateBuilder.<T>builder();
        return handleServiceCall(function, dataCrateBuilder);
    }

    public <T, F> ListenableFuture<RpcResult<T>> handleServiceCall(final Function<DataCrate<T>, ListenableFuture<RpcResult<F>>> function) {
        DataCrateBuilder<T> dataCrateBuilder = DataCrateBuilder.<T>builder();
        return handleServiceCall(function, dataCrateBuilder);
    }

    /**
     * @param <T>
     * @param <F>
     * @param function
     * @param dataCrateBuilder predefined data
     * @return
     */
    public final <T, F> ListenableFuture<RpcResult<T>> handleServiceCall(final Function<DataCrate<T>, ListenableFuture<RpcResult<F>>> function,
                                                                         final DataCrateBuilder<T> dataCrateBuilder) {

        LOG.trace("Handling general service call");
        final RequestContext<T> requestContext = requestContextStack.createRequestContext();
        final SettableFuture<RpcResult<T>> result = requestContextStack.storeOrFail(requestContext);
        if (result.isDone()) {
            messageSpy.spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_DISREGARDED);
            LOG.trace("Request context refused.");
            return result;
        }

        long reservedXid;
        final OutboundQueue outboundQueue = deviceContext.getPrimaryConnectionContext().getOutboundQueueProvider().getOutboundQueue();
        synchronized (outboundQueue) {
            reservedXid = outboundQueue.reserveEntry();
        }
        final Xid xid = new Xid(reservedXid);
        requestContext.setXid(xid);
        DataCrate<T> dataCrate = dataCrateBuilder.setRequestContext(requestContext)
                .build();
        final ListenableFuture<RpcResult<F>> resultFromOFLib;

        LOG.trace("Hooking xid {} to device context - precaution.", requestContext.getXid().getValue());
        deviceContext.hookRequestCtx(xid, requestContext);

        messageSpy.spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_READY_FOR_SUBMIT);
        synchronized (deviceContext) {
            resultFromOFLib = function.apply(dataCrate);
        }

        final OFJResult2RequestCtxFuture<T> OFJResult2RequestCtxFuture = new OFJResult2RequestCtxFuture<>(requestContext, deviceContext);
        OFJResult2RequestCtxFuture.processResultFromOfJava(resultFromOFLib);
        return result;

    }

}
