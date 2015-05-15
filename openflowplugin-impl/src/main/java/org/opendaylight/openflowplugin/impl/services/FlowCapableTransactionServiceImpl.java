/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.callback.SuccessCallback;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class FlowCapableTransactionServiceImpl extends CommonService implements FlowCapableTransactionService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FlowCapableTransactionServiceImpl.class);

    public FlowCapableTransactionServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<Void>> sendBarrier(final SendBarrierInput input) {
        final RequestContext<Void> requestContext = getRequestContextStack().createRequestContext();
        if (requestContext == null) {
            getMessageSpy().spyMessage(null, MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
            return failedFuture();
        }

        final DeviceContext deviceContext = getDeviceContext();
        final Long reservedXid = deviceContext.getReservedXid();
        if (null == reservedXid) {
            return RequestContextUtil.closeRequestContextWithRpcError(requestContext, "Outbound queue wasn't able to reserve XID.");
        }
        final Xid xid = new Xid(reservedXid);
        requestContext.setXid(xid);

        final BarrierInputBuilder barrierInputOFJavaBuilder = new BarrierInputBuilder();
        barrierInputOFJavaBuilder.setVersion(getVersion());
        barrierInputOFJavaBuilder.setXid(xid.getValue());

        LOG.trace("Hooking xid {} to device context - precaution.", requestContext.getXid().getValue());
        deviceContext.hookRequestCtx(requestContext.getXid(), requestContext);

        final BarrierInput barrierInputOFJava = barrierInputOFJavaBuilder.build();

        // FIXME: should be submitted through OutboundQueue
        final Future<RpcResult<BarrierOutput>> barrierOutputOFJava = getPrimaryConnectionAdapter()
                .barrier(barrierInputOFJava);
        LOG.debug("Barrier with xid {} was sent from controller.", xid);

        ListenableFuture<RpcResult<BarrierOutput>> listenableBarrierOutputOFJava = JdkFutureAdapters
                .listenInPoolThread(barrierOutputOFJava);

        // callback on OF JAVA future
        SuccessCallback<BarrierOutput, Void> successCallback = new SuccessCallback<BarrierOutput, Void>(
                deviceContext, requestContext, listenableBarrierOutputOFJava) {

            @Override
            public RpcResult<Void> transform(final RpcResult<BarrierOutput> rpcResult) {
                //no transformation, because output for request context is Void
                LOG.debug("Barrier reply with xid {} was obtained by controller.", rpcResult.getResult().getXid());
                return RpcResultBuilder.<Void>success().build();
            }
        };
        Futures.addCallback(listenableBarrierOutputOFJava, successCallback);

        return requestContext.getFuture();
    }
}
