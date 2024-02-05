/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerAggregateFlowMultipartService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerAggregateFlowMultipartService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class GetAggregateFlowStatisticsFromFlowTableForGivenMatchImpl
        implements GetAggregateFlowStatisticsFromFlowTableForGivenMatch {
    private final SingleLayerAggregateFlowMultipartService single;
    private final MultiLayerAggregateFlowMultipartService multi;

    public GetAggregateFlowStatisticsFromFlowTableForGivenMatchImpl(final RequestContextStack requestContextStack,
            final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor) {
        single = new SingleLayerAggregateFlowMultipartService(requestContextStack, deviceContext);
        multi = new MultiLayerAggregateFlowMultipartService(requestContextStack, deviceContext,
            convertorExecutor, deviceContext.oook());
    }

    @Override
    public ListenableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> invoke(
            final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {
        return single.canUseSingleLayerSerialization() ? single.handleAndReply(input) : multi.handleAndReply(input);
    }
}
