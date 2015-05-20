/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowCapableTransactionServiceImpl extends CommonService implements FlowCapableTransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableTransactionServiceImpl.class);

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


        final BarrierInputBuilder barrierInputOFJavaBuilder = new BarrierInputBuilder();
        final Xid xid = requestContext.getXid();
        barrierInputOFJavaBuilder.setVersion(getVersion());
        barrierInputOFJavaBuilder.setXid(xid.getValue());

        LOG.trace("Hooking xid {} to device context - precaution.", requestContext.getXid().getValue());

        final OutboundQueue outboundQueue = getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider();
        final BarrierInput barrierInput = barrierInputOFJavaBuilder.build();
        outboundQueue.commitEntry(xid.getValue(), barrierInput, new FutureCallback<OfHeader>() {

            RpcResultBuilder<Void> rpcResultBuilder;
            @Override
            public void onSuccess(final OfHeader ofHeader) {
                rpcResultBuilder = RpcResultBuilder.<Void>success();
                requestContext.setResult(rpcResultBuilder.build());
                RequestContextUtil.closeRequstContext(requestContext);

                getMessageSpy().spyMessage(barrierInput.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);

            }

            @Override
            public void onFailure(final Throwable throwable) {
                rpcResultBuilder = RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.APPLICATION, throwable.getMessage(), throwable);
                requestContext.setResult(rpcResultBuilder.build());
                RequestContextUtil.closeRequstContext(requestContext);

                getMessageSpy().spyMessage(barrierInput.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
            }
        });
        return requestContext.getFuture();
    }
}
