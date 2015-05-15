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
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.callback.SuccessCallback;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SalEchoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
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


        final DeviceContext deviceContext = getDeviceContext();

        LOG.trace("Hooking xid {} to device context - precaution.", requestContext.getXid().getValue());
        deviceContext.hookRequestCtx(requestContext.getXid(), requestContext);

        final EchoInputBuilder echoInputOFJavaBuilder = new EchoInputBuilder();
        echoInputOFJavaBuilder.setVersion(getVersion());
        echoInputOFJavaBuilder.setXid(requestContext.getXid().getValue());
        echoInputOFJavaBuilder.setData(sendEchoInput.getData());
        final EchoInput echoInputOFJava = echoInputOFJavaBuilder.build();

        // FIXME: should be submitted via OutboundQueue
        final Future<RpcResult<EchoOutput>> rpcEchoOutputOFJava = getPrimaryConnectionAdapter()
                .echo(echoInputOFJava);
        LOG.debug("Echo with xid {} was sent from controller", requestContext.getXid());

        ListenableFuture<RpcResult<EchoOutput>> listenableRpcEchoOutputOFJava = JdkFutureAdapters
                .listenInPoolThread(rpcEchoOutputOFJava);

        // callback on OF JAVA future
        SuccessCallback<EchoOutput, SendEchoOutput> successCallback = new SuccessCallback<EchoOutput, SendEchoOutput>(
                deviceContext, requestContext, listenableRpcEchoOutputOFJava) {

            @Override
            public RpcResult<SendEchoOutput> transform(final RpcResult<EchoOutput> rpcResult) {
                EchoOutput echoOutputOFJava = rpcResult.getResult();
                SendEchoOutputBuilder sendEchoOutputBuilder = new SendEchoOutputBuilder();
                sendEchoOutputBuilder.setData(echoOutputOFJava.getData());

                LOG.debug("Echo with xid {} was received by controller.", rpcResult.getResult().getXid());
                return RpcResultBuilder.success(sendEchoOutputBuilder.build()).build();
            }
        };
        Futures.addCallback(listenableRpcEchoOutputOFJava, successCallback);

        return requestContext.getFuture();
    }
}
