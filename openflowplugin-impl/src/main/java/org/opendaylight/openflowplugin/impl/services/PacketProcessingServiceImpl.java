/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.PacketOutConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class PacketProcessingServiceImpl extends CommonService implements PacketProcessingService {

    public PacketProcessingServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<Void>> transmitPacket(final TransmitPacketInput input) {
        getMessageSpy().spyMessage(input.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_ENTERED);

        return handleServiceCall(new Function<RequestContext<Void>, ListenableFuture<RpcResult<Void>>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RequestContext<Void> requestContext) {
                final Xid xid = requestContext.getXid();
                final PacketOutInput message = PacketOutConvertor.toPacketOutInput(input, getVersion(), xid.getValue(),
                        getDatapathId());

                final OutboundQueue outboundQueue = getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider();

                final SettableFuture<RpcResult<Void>> settableFuture = SettableFuture.create();

                outboundQueue.commitEntry(xid.getValue(), message, new FutureCallback<OfHeader>() {
                    @Override
                    public void onSuccess(final OfHeader ofHeader) {
                        if (ofHeader instanceof RpcResult) {
                            @SuppressWarnings("unchecked")
                            RpcResult<Void> rpcResult = (RpcResult<Void>) ofHeader;
                            if (!rpcResult.isSuccessful()) {
                                getMessageSpy().spyMessage(message.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);
                                settableFuture.set(rpcResult);
                            } else {
                                getMessageSpy().spyMessage(message.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
                                settableFuture.cancel(true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        getMessageSpy().spyMessage(message.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);

                        settableFuture.cancel(true);
                    }
                });
                return settableFuture;
            }
        });

    }
}
