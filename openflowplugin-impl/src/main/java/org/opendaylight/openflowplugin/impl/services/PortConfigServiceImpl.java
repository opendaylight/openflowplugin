/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.port.config.rev150714.PortConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.port.config.rev150714.GetPortConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.port.config.rev150714.GetPortConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.port.config.rev150714.GetPortConfigOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCaseBuilder;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

public final class PortConfigServiceImpl implements PortConfigService {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PortConfigServiceImpl.class);
    private RequestContextStack requestContextStack;
    private DeviceContext deviceContext;
    private final short version;
    private Xid xid;

    public PortConfigServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        this.requestContextStack = requestContextStack;
        this.deviceContext = deviceContext;
        final FeaturesReply features = this.deviceContext.getPrimaryConnectionContext().getFeatures();
        this.version = features.getVersion();
        final RequestContext requestContext = requestContextStack.createRequestContext();
        xid = requestContext.getXid();
    }

    @Override
    public Future<RpcResult<GetPortConfigOutput>> getPortConfig(GetPortConfigInput input) {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        builder.setType(MultipartType.OFPMPPORTDESC);
        builder.setVersion(this.version);
        builder.setFlags(new MultipartRequestFlags(false));
        builder.setMultipartRequestBody(new MultipartRequestPortDescCaseBuilder()
                .build());
        builder.setXid(xid.getValue());
        final ListenableFuture<RpcResult<Void>> multipartFuture = 
                (ListenableFuture<RpcResult<Void>>)
                deviceContext.getPrimaryConnectionContext().getConnectionAdapter().multipartRequest(builder.build());
        final SettableFuture<RpcResult<GetPortConfigOutput>> finalFuture = SettableFuture.create();

        class CallBackImpl implements FutureCallback<RpcResult<Void>> {
            @Override
            public void onSuccess(final RpcResult<Void> result) {
                if (result.isSuccessful()) {
                    LOG.debug(
                        "OnSuccess, rpc result successful, multipart response for rpc update-table with xid {} obtained.",
                        xid.getValue());
                    final GetPortConfigOutputBuilder getPortConfigOutputBuilder = new GetPortConfigOutputBuilder();
                    getPortConfigOutputBuilder.setTransactionId(new TransactionId(BigInteger.valueOf(xid.getValue())));
                    finalFuture.set(RpcResultBuilder.success(getPortConfigOutputBuilder.build()).build());
                } else {
                    LOG.debug("OnSuccess, rpc result unsuccessful, multipart response for rpc update-table was unsuccessful.");
                    finalFuture.set(RpcResultBuilder.<GetPortConfigOutput>failed().withRpcErrors(result.getErrors())
                            .build());
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.debug("Failure multipart response for table features request. Exception: {}", t);
                finalFuture.set(RpcResultBuilder.<GetPortConfigOutput>failed()
                        .withError(ErrorType.RPC, "Future error", t).build());
            }
        }

        Futures.addCallback(multipartFuture, new CallBackImpl());

        return finalFuture;
    }

}
