/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yangtools.yang.binding.DataObject;

import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

abstract class CommonService {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CommonService.class);
    private static final long WAIT_TIME = 2000;
    protected final static Future<RpcResult<Void>> ERROR_RPC_RESULT = Futures.immediateFuture(RpcResultBuilder
            .<Void> failed().withError(ErrorType.APPLICATION, "", "Request quota exceeded.").build());
    protected static final BigInteger PRIMARY_CONNECTION = new BigInteger("0");

    // protected OFRpcTaskContext rpcTaskContext;
    protected short version;
    protected BigInteger datapathId;
    protected RpcContext rpcContext;
    protected DeviceContext deviceContext;
    private ConnectionAdapter primaryConnectionAdapter;

    CommonService() {
    }

    public CommonService(final RpcContext rpcContext) {
        this.rpcContext = rpcContext;

        this.deviceContext = rpcContext.getDeviceContext();
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

        // TODO uncomment when getAuxiali.... will be merged to APIs
        // final ConnectionContext auxiliaryConnectionContext =
        // deviceContext.getAuxiliaryConnectionContext(connectionID);
        final ConnectionContext auxiliaryConnectionContext = null;
        if (auxiliaryConnectionContext != null) {
            return auxiliaryConnectionContext.getConnectionAdapter();
        }

        return primaryConnectionAdapter;
    }

    <T extends DataObject, F>  Future<RpcResult<T>> handleServiceCall(final BigInteger connectionID,
            final Function<BigInteger,Future<RpcResult<F>>> function) {
        LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");

        final RequestContext<T> requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<T>> result = rpcContext.storeOrFail(requestContext);

        if (!result.isDone()) {
            final Future<RpcResult<F>> resultFromOFLib = function.apply(connectionID);

            final RpcResultConvertor<T> rpcResultConvertor = new RpcResultConvertor<>(requestContext, deviceContext);
            rpcResultConvertor.processResultFromOfJava(resultFromOFLib);

        } else {
            RequestContextUtil.closeRequstContext(requestContext);
        }
        return result;
    }

}
