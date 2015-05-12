/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.SettableFuture;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowHash;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowHashFactory;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 12.5.2015.
 */
public class SalFlowServiceNewImpl implements SalFlowService {

    private static final Logger LOG = LoggerFactory.getLogger(SalFlowServiceNewImpl.class);
    private final RequestContextStack requestContextStack;
    private final DeviceContext deviceContext;

    public SalFlowServiceNewImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        this.requestContextStack = requestContextStack;
        this.deviceContext = deviceContext;
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {

        final MessageSpy messageSpy = deviceContext.getMessageSpy();
        messageSpy.spyMessage(input.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_ENTERED);
        final FlowId flowId;

        if (null != input.getFlowRef()) {
            flowId = input.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId();
        } else {
            flowId = FlowUtil.createAlienFlowId(input.getTableId());
        }

        final FlowHash flowHash = FlowHashFactory.create(input, deviceContext.getPrimaryConnectionContext().getFeatures().getVersion());
        final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(input.getTableId(), flowId);

        final List<FlowModInputBuilder> ofFlowModInputs = FlowConvertor.toFlowModInputs(input,
                this.deviceContext.getPrimaryConnectionContext().getFeatures().getVersion(),
                this.deviceContext.getPrimaryConnectionContext().getFeatures().getDatapathId());

        if (ofFlowModInputs.size() == 1) {
            final FlowModInputBuilder flowModInputBuilder = ofFlowModInputs.get(0);
            final OutboundQueue outboundQueue = deviceContext.getPrimaryConnectionContext().getOutboundQueueProvider().getOutboundQueue();
            Long reservedXid = outboundQueue.reserveEntry();
            final RequestContext<RpcResult<AddFlowOutput>> requestContext = requestContextStack.createRequestContext();
            SettableFuture<RpcResult<RpcResult<AddFlowOutput>>> resultSettableFuture = requestContext.getFuture();
            if (resultSettableFuture.isDone()) {
                messageSpy.spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_DISREGARDED);
                return null;
            }

            synchronized (outboundQueue) {
                reservedXid = outboundQueue.reserveEntry();
            }
            final Xid xid = new Xid(reservedXid);
            requestContext.setXid(xid);
            LOG.trace("Hooking xid {} to device context - precaution.", requestContext.getXid().getValue());
            deviceContext.hookRequestCtx(requestContext.getXid(), requestContext);
            flowModInputBuilder.setXid(xid.getValue());
            final FlowModInput flowModInput = flowModInputBuilder.build();
            outboundQueue.commitEntry(reservedXid, flowModInput, new FutureCallback<OfHeader>() {
                @Override
                public void onSuccess(final OfHeader ofHeader) {
                    RequestContextUtil.closeRequstContext(requestContext);
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    RequestContextUtil.closeRequstContext(requestContext);
                }
            });


        }
        return null;

    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(final UpdateFlowInput input) {
        return null;
    }
}
