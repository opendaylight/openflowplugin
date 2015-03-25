/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

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

public class CommonService {
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

    public CommonService() {
    }

    public CommonService(final RpcContext rpcContext) {
        this.rpcContext = rpcContext;

        this.deviceContext = rpcContext.getDeviceContext();
        final FeaturesReply features = this.deviceContext.getPrimaryConnectionContext().getFeatures();
        this.datapathId = features.getDatapathId();
        this.version = features.getVersion();
        this.primaryConnectionAdapter = deviceContext.getPrimaryConnectionContext().getConnectionAdapter();
    }

    protected long getWaitTime() {
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

}
