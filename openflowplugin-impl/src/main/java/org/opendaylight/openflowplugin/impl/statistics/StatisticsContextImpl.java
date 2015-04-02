/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.impl.rpc.RequestContextImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowStatisticsServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 1.4.2015.
 */
public class StatisticsContextImpl implements StatisticsContext {

    private final List<RequestContext> requestContexts = new ArrayList();
    private final DeviceContext deviceContext;
    private final RequestContext requestContext;

    private final OpendaylightFlowStatisticsService flowStatisticsService;

    public StatisticsContextImpl(DeviceContext deviceContext, RequestContext requestContext) {
        this.deviceContext = deviceContext;
        this.requestContext = requestContext;
        flowStatisticsService = new OpendaylightFlowStatisticsServiceImpl(this, deviceContext);

    }

    private void pollFlowStatistics() {
        KeyedInstanceIdentifier<Node, NodeKey> nodeII = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(deviceContext.getPrimaryConnectionContext().getNodeId()));
        NodeRef nodeRef = new NodeRef(nodeII);
        final GetAllFlowsStatisticsFromAllFlowTablesInputBuilder builder =
                new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder();
        builder.setNode(nodeRef);
        Future<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> flowStatisticsResult = flowStatisticsService.getAllFlowsStatisticsFromAllFlowTables(builder.build());
        //TODO : process data from result
    }

    @Override
    public ListenableFuture<Void> gatherDynamicData() {
        //TODO : call all necessary statistics services to gather data and store them in Oper DS
        return null;
    }

    @Override
    public <T> void forgetRequestContext(final RequestContext<T> requestContext) {
        requestContexts.remove(requestContexts);
    }

    @Override
    public <T> SettableFuture<RpcResult<T>> storeOrFail(final RequestContext<T> data) {
        requestContexts.add(data);
        return data.getFuture();
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        return new RequestContextImpl<>(this);
    }
}
