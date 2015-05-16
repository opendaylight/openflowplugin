/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class NodeConfigServiceImpl extends CommonService implements NodeConfigService {


    public NodeConfigServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<SetConfigOutput>> setConfig(final SetConfigInput input) {
        final RequestContext<SetConfigOutput> requestContext = createRequestContext();
        if (requestContext == null) {
            return failedFuture();
        }

        SetConfigInputBuilder builder = new SetConfigInputBuilder();
        SwitchConfigFlag flag = SwitchConfigFlag.valueOf(input.getFlag());
        final Long reservedXid = getDeviceContext().getReservedXid();
        if (null == reservedXid) {
            return RequestContextUtil.closeRequestContextWithRpcError(requestContext, "Outbound queue wasn't able to reserve XID.");
        }

        final Xid xid = new Xid(reservedXid);
        builder.setXid(xid.getValue());
        builder.setFlags(flag);
        builder.setMissSendLen(input.getMissSearchLength());
        builder.setVersion(getVersion());
        final OutboundQueue outboundQueue = getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider();

        final SettableFuture<RpcResult<SetConfigOutput>> settableFuture = SettableFuture.create();
        outboundQueue.commitEntry(xid.getValue(), builder.build(), new FutureCallback<OfHeader>() {
            @Override
            public void onSuccess(final OfHeader ofHeader) {
                RequestContextUtil.closeRequstContext(requestContext);
                getDeviceContext().unhookRequestCtx(requestContext.getXid());
                getMessageSpy().spyMessage(FlowModInput.class, MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);

                settableFuture.set(RpcResultBuilder.<SetConfigOutput>success().build());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                RpcResultBuilder rpcResultBuilder = RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.APPLICATION, throwable.getMessage(), throwable);
                RequestContextUtil.closeRequstContext(requestContext);
                getDeviceContext().unhookRequestCtx(requestContext.getXid());
                settableFuture.set(rpcResultBuilder.build());
            }
        });
        return settableFuture;

    }
}
