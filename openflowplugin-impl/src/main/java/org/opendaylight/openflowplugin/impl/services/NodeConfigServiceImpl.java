/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class NodeConfigServiceImpl extends CommonService implements NodeConfigService {

    // FIXME: should be only in CommonService
    private final DeviceContext deviceContext;

    public NodeConfigServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
        this.deviceContext = deviceContext;
    }

    @Override
    public Future<RpcResult<SetConfigOutput>> setConfig(final SetConfigInput input) {
        final RequestContext<SetConfigOutput> requestContext = createRequestContext();
        if (requestContext == null) {
            return failedFuture();
        }

        SetConfigInputBuilder builder = new SetConfigInputBuilder();
        SwitchConfigFlag flag = SwitchConfigFlag.valueOf(input.getFlag());
        final Long reserverXid = deviceContext.getReservedXid();
        if (null == reserverXid) {
            return RequestContextUtil.closeRequestContextWithRpcError(requestContext, "Outbound queue wasn't able to reserve XID.");
        }

        final Xid xid = new Xid(reserverXid);
        builder.setXid(xid.getValue());
        builder.setFlags(flag);
        builder.setMissSendLen(input.getMissSearchLength());
        builder.setVersion(getVersion());
        ListenableFuture<RpcResult<Void>> futureResultFromOfLib;
        synchronized (deviceContext) {
            futureResultFromOfLib = JdkFutureAdapters.listenInPoolThread(deviceContext.getPrimaryConnectionContext().getConnectionAdapter().setConfig(builder.build()));
        }
        OFJResult2RequestCtxFuture<SetConfigOutput> OFJResult2RequestCtxFuture = new OFJResult2RequestCtxFuture<>(requestContext, deviceContext);
        OFJResult2RequestCtxFuture.processResultFromOfJava(futureResultFromOfLib);
        return requestContext.getFuture();
    }
}
