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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class SalMeterServiceImpl extends CommonService implements SalMeterService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalMeterServiceImpl.class);

    public SalMeterServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<AddMeterOutput>> addMeter(final AddMeterInput input) {
        getDeviceContext().getDeviceMeterRegistry().store(input.getMeterId());
        return handleServiceCall(new Function<RequestContext<AddMeterOutput>, ListenableFuture<RpcResult<AddMeterOutput>>>() {
            @Override
            public ListenableFuture<RpcResult<AddMeterOutput>> apply(final RequestContext<AddMeterOutput> requestContext) {
                return convertAndSend(input, requestContext);
            }
        });
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> updateMeter(final UpdateMeterInput input) {
        return handleServiceCall(new Function<RequestContext<UpdateMeterOutput>, ListenableFuture<RpcResult<UpdateMeterOutput>>>() {
            @Override
            public ListenableFuture<RpcResult<UpdateMeterOutput>> apply(final RequestContext<UpdateMeterOutput> requestContext) {
                return convertAndSend(input.getUpdatedMeter(), requestContext);
            }
        });
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeMeter(final RemoveMeterInput input) {
        getDeviceContext().getDeviceMeterRegistry().markToBeremoved(input.getMeterId());
        return handleServiceCall(new Function<RequestContext<RemoveMeterOutput>, ListenableFuture<RpcResult<RemoveMeterOutput>>>() {
            @Override
            public ListenableFuture<RpcResult<RemoveMeterOutput>> apply(final RequestContext<RemoveMeterOutput> requestContext) {
                return convertAndSend(input, requestContext);
            }
        });
    }

    <T> ListenableFuture<RpcResult<T>> convertAndSend(final Meter iputMeter, final RequestContext<T> requestContext) {
        getMessageSpy().spyMessage(iputMeter.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);
        final OutboundQueue outboundQueue = getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider();

        final MeterModInputBuilder ofMeterModInput = MeterConvertor.toMeterModInput(iputMeter, getVersion());
        final Xid xid = requestContext.getXid();
        ofMeterModInput.setXid(xid.getValue());
        final SettableFuture<RpcResult<T>> settableFuture = SettableFuture.create();
        final MeterModInput meterModInput = ofMeterModInput.build();
        outboundQueue.commitEntry(xid.getValue(), meterModInput, new FutureCallback<OfHeader>() {
            @Override
            public void onSuccess(final OfHeader ofHeader) {
                RequestContextUtil.closeRequstContext(requestContext);
                getMessageSpy().spyMessage(meterModInput.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);

                settableFuture.set(RpcResultBuilder.<T>success().build());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                RpcResultBuilder<T> rpcResultBuilder = RpcResultBuilder.<T>failed().withError(RpcError.ErrorType.APPLICATION, throwable.getMessage(), throwable);
                RequestContextUtil.closeRequstContext(requestContext);
                getMessageSpy().spyMessage(meterModInput.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
                settableFuture.set(rpcResultBuilder.build());
            }
        });
        return settableFuture;
    }
}
