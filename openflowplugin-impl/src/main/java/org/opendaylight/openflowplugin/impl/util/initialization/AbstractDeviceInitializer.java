/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util.initialization;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public abstract class AbstractDeviceInitializer<T extends OfHeader> {

    private final DeviceContext deviceContext;

    public AbstractDeviceInitializer(final DeviceContext deviceContext) {
        this.deviceContext = deviceContext;
    }

    /**
     * Return true if multipart requests more replies
     * @param multipart multipart message
     * @return true if multipart requests more replies
     */
    protected abstract boolean isReqMore(final OfHeader multipart);

    protected ListenableFuture<RpcResult<List<T>>> getNodeStaticInfo(final MultipartType type) {
        final OutboundQueue queue = deviceContext.getPrimaryConnectionContext().getOutboundQueueProvider();
        final Long reserved = deviceContext.getDeviceInfo().reserveXidForDeviceMessage();

        final RequestContext<List<T>> requestContext = new AbstractRequestContext<List<T>>(
                reserved) {
            @Override
            public void close() {
                //NOOP
            }
        };

        final Xid xid = requestContext.getXid();

        if (Objects.isNull(xid)) {
            LOG.debug("Xid is not present, so cancelling node static info gathering.");
            return Futures.immediateCancelledFuture();
        }

        LOG.trace("Hooking xid {} to device context - precaution.", reserved);

        final MultiMsgCollector<T> multiMsgCollector = deviceContext.getMultiMsgCollector(requestContext);
        queue.commitEntry(xid.getValue(),
                MultipartRequestInputFactory.makeMultipartRequestInput(xid.getValue(), version, type),
                new FutureCallback<OfHeader>() {
                    @Override
                    public void onSuccess(final OfHeader ofHeader) {

                        if (ofHeader instanceof MultipartReply) {
                            final MultipartReply multipartReply = (MultipartReply) ofHeader;
                            multiMsgCollector.addMultipartMsg(multipartReply, multipartReply.getFlags().isOFPMPFREQMORE(), null);
                        } else if (ofHeader instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112
                                .MultipartReply) {
                            final org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112
                                    .MultipartReply multipartReply = (org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112
                                    .MultipartReply) ofHeader;
                            multiMsgCollector.addMultipartMsg(multipartReply, multipartReply.isRequestMore(), null);
                        } else if (null != ofHeader) {
                            LOG.info("Unexpected response type received {}.", ofHeader.getClass());
                        } else {
                            multiMsgCollector.endCollecting(null);
                            LOG.info("Response received is null.");
                        }
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        LOG.info("Fail response from OutboundQueue for multipart type {}.", type);
                        final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder
                                .<List<MultipartReply>>failed().build();
                        requestContext.setResult(rpcResult);
                        if (MultipartType.OFPMPTABLEFEATURES.equals(type)) {
                            makeEmptyTables(deviceContext, nodeII, deviceContext.getPrimaryConnectionContext()
                                    .getFeatures().getTables());
                        }
                        requestContext.close();
                    }
                });

        return requestContext.getFuture();
    }
}
