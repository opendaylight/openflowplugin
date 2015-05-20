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
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SalEchoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalEchoServiceImpl extends CommonService implements SalEchoService {

    private static final Logger LOG = LoggerFactory.getLogger(SalEchoServiceImpl.class);

    public SalEchoServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<SendEchoOutput>> sendEcho(final SendEchoInput sendEchoInput) {
        final RequestContext<SendEchoOutput> requestContext = getRequestContextStack().createRequestContext();
        if (requestContext == null) {
            getMessageSpy().spyMessage(null, MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
            return failedFuture();
        }



        LOG.trace("Hooking xid {} to device context - precaution.", requestContext.getXid().getValue());
        final Xid xid = requestContext.getXid();
        final EchoInputBuilder echoInputOFJavaBuilder = new EchoInputBuilder();
        echoInputOFJavaBuilder.setVersion(getVersion());
        echoInputOFJavaBuilder.setXid(requestContext.getXid().getValue());
        echoInputOFJavaBuilder.setData(sendEchoInput.getData());
        final EchoInput echoInputOFJava = echoInputOFJavaBuilder.build();

        LOG.debug("Echo with xid {} was sent from controller", xid);

        final OutboundQueue outboundQueue = getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider();
        outboundQueue.commitEntry(xid.getValue(), echoInputOFJava, new FutureCallback<OfHeader>() {

            RpcResultBuilder<SendEchoOutput> rpcResultBuilder;
            @Override
            public void onSuccess(final OfHeader ofHeader) {
                rpcResultBuilder = RpcResultBuilder.<SendEchoOutput>success();
                requestContext.setResult(rpcResultBuilder.build());
                RequestContextUtil.closeRequstContext(requestContext);

                getMessageSpy().spyMessage(echoInputOFJava.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                rpcResultBuilder = RpcResultBuilder.<SendEchoOutput>failed().withError(RpcError.ErrorType.APPLICATION, throwable.getMessage(), throwable);
                requestContext.setResult(rpcResultBuilder.build());
                RequestContextUtil.closeRequstContext(requestContext);

                getMessageSpy().spyMessage(echoInputOFJava.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
            }
        });
        return requestContext.getFuture();
    }
}
