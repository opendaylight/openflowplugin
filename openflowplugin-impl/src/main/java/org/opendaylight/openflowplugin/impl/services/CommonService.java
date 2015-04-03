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
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import java.math.BigInteger;
import java.util.concurrent.Future;

public abstract class CommonService {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CommonService.class);
    private static final long WAIT_TIME = 2000;
    protected final static Future<RpcResult<Void>> ERROR_RPC_RESULT = Futures.immediateFuture(RpcResultBuilder
            .<Void>failed().withError(ErrorType.APPLICATION, "", "Request quota exceeded.").build());
    protected static final BigInteger PRIMARY_CONNECTION = new BigInteger("0");

    // protected OFRpcTaskContext rpcTaskContext;
    public short version;
    public BigInteger datapathId;
    public RequestContextStack requestContextStack;
    public DeviceContext deviceContext;
    public ConnectionAdapter primaryConnectionAdapter;

    public CommonService() {
    }

    public CommonService(final RequestContextStack requestContextStack, DeviceContext deviceContext) {
        this.requestContextStack = requestContextStack;

        this.deviceContext = deviceContext;
        final FeaturesReply features = this.deviceContext.getPrimaryConnectionContext().getFeatures();
        this.datapathId = features.getDatapathId();
        this.version = features.getVersion();
        this.primaryConnectionAdapter = deviceContext.getPrimaryConnectionContext().getConnectionAdapter();
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

    public <T, F> Future<RpcResult<T>> handleServiceCall(final BigInteger connectionID,
                                                                            final Function<DataCrate<T>, Future<RpcResult<F>>> function) {
        LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");

        final RequestContext<T> requestContext = requestContextStack.createRequestContext();
        final SettableFuture<RpcResult<T>> result = requestContextStack.storeOrFail(requestContext);
        final DataCrate<T> dataCrate = DataCrateBuilder.<T>builder().setiDConnection(connectionID)
                .setRequestContext(requestContext).build();
        if (!result.isDone()) {
            final Future<RpcResult<F>> resultFromOFLib = function.apply(dataCrate);

            final OFJResult2RequestCtxFuture<T> OFJResult2RequestCtxFuture = new OFJResult2RequestCtxFuture<>(requestContext, deviceContext);
            OFJResult2RequestCtxFuture.processResultFromOfJava(resultFromOFLib);

        } else {
            RequestContextUtil.closeRequstContext(requestContext);
        }
        return result;
    }

}
