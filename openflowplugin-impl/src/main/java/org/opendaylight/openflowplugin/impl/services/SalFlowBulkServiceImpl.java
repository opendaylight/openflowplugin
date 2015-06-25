/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150623.AddFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150623.AddFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150623.SalFlowBulkService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150623.add.flows.input.Flows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 23.6.2015.
 */
public class SalFlowBulkServiceImpl implements SalFlowBulkService {

    private static final Logger LOG = LoggerFactory.getLogger(SalFlowBulkServiceImpl.class);
    private final FlowService<AddFlowsOutput> flowAdd;

    public SalFlowBulkServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        flowAdd = new FlowService<>(requestContextStack, deviceContext, AddFlowsOutput.class);
    }

    @Override
    public Future<RpcResult<AddFlowsOutput>> addFlows(final AddFlowsInput input) {
        List<Future<RpcResult<AddFlowsOutput>>> particularResults = new ArrayList<>();
        InstanceIdentifier<?> iid = input.getNode().getValue();
        for (Flows flow : input.getFlows()) {
            particularResults.add(addFlow(flow, iid));
        }

        RpcResultBuilder<AddFlowsOutput> builder = RpcResultBuilder.success();
        return Futures.immediateFuture(builder.build());
    }

    private Future<RpcResult<AddFlowsOutput>> addFlow(final Flows flowRefs, InstanceIdentifier<?> instanceIdentifier) {
        final FlowId flowId;
        FlowKey flowKey = null;
        if (null != flowRefs) {
            flowKey = instanceIdentifier.firstKeyOf(Flow.class, FlowKey.class);
        }
        if (null != flowKey) {
            flowId = flowKey.getId();
        } else {
            flowId = FlowUtil.createAlienFlowId(flowRefs.getTableId());
        }

        final DeviceContext deviceContext = flowAdd.getDeviceContext();
        final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(flowRefs);
        final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(flowRefs.getTableId(), flowId);
        deviceContext.getDeviceFlowRegistry().store(flowRegistryKey, flowDescriptor);
        final ListenableFuture<RpcResult<AddFlowsOutput>> future = flowAdd.processFlowModInputBuilders(flowAdd.toFlowModInputs(flowRefs));
        Futures.addCallback(future, new FutureCallback<RpcResult<AddFlowsOutput>>() {
            @Override
            public void onSuccess(final RpcResult<AddFlowsOutput> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    LOG.debug("flow add finished without error, id={}", flowId.getValue());
                } else {
                    LOG.debug("flow add failed with error, id={}", flowId.getValue());
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                deviceContext.getDeviceFlowRegistry().markToBeremoved(flowRegistryKey);
                LOG.trace("Service call for adding flows failed, id={}.", flowId.getValue(), throwable);
            }
        });

        return future;
    }
}
