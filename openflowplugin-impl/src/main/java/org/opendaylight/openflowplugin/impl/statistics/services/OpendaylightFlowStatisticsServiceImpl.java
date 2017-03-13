/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.statistics.compatibility.Delegator;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerAggregateFlowMultipartService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerAggregateFlowMultipartService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 */
public class OpendaylightFlowStatisticsServiceImpl implements OpendaylightFlowStatisticsService, Delegator<OpendaylightFlowStatisticsService> {

    private final SingleLayerAggregateFlowMultipartService singleLayerService;
    private final MultiLayerAggregateFlowMultipartService multiLayerService;
    private OpendaylightFlowStatisticsService delegate;

    public static OpendaylightFlowStatisticsServiceImpl createWithOook(final RequestContextStack requestContextStack,
                                                              final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor) {
        return new OpendaylightFlowStatisticsServiceImpl(requestContextStack, deviceContext, deviceContext.oook(), convertorExecutor);
    }

    public OpendaylightFlowStatisticsServiceImpl(final RequestContextStack requestContextStack,
                                                 final DeviceContext deviceContext,
                                                 final TranslatorLibrary translatorLibrary,
                                                 final ConvertorExecutor convertorExecutor) {
        singleLayerService = new SingleLayerAggregateFlowMultipartService(requestContextStack, deviceContext);
        multiLayerService = new MultiLayerAggregateFlowMultipartService(requestContextStack, deviceContext,
            convertorExecutor, translatorLibrary);
    }

    @Override
    public void setDelegate(OpendaylightFlowStatisticsService delegate) {
        this.delegate = delegate;
    }

    /**
     * @deprecated provided for Be-release as backward compatibility relic
     */
    @Override
    @Deprecated
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> getAggregateFlowStatisticsFromFlowTableForAllFlows(
            final GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input) {
        if (delegate != null) {
            return delegate.getAggregateFlowStatisticsFromFlowTableForAllFlows(input);
        } else {
            throw new IllegalAccessError("no delegate available - service is currently out of order");
        }
    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> getAggregateFlowStatisticsFromFlowTableForGivenMatch(
            final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {
        return singleLayerService.canUseSingleLayerSerialization()
            ? singleLayerService.handleAndReply(input)
            : multiLayerService.handleAndReply(input);
    }

    /**
     * @deprecated provided for Be-release as backward compatibility relic
     */
    @Override
    @Deprecated
    public Future<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> getAllFlowStatisticsFromFlowTable(
            final GetAllFlowStatisticsFromFlowTableInput input) {
        if (delegate != null) {
            return delegate.getAllFlowStatisticsFromFlowTable(input);
        } else {
            throw new IllegalAccessError("no delegate available - service is currently out of order");
        }
    }

    /**
     * @deprecated provided for Be-release as backward compatibility relic
     */
    @Override
    @Deprecated
    public Future<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> getAllFlowsStatisticsFromAllFlowTables(
            final GetAllFlowsStatisticsFromAllFlowTablesInput input) {
        if (delegate != null) {
            return delegate.getAllFlowsStatisticsFromAllFlowTables(input);
        } else {
            throw new IllegalAccessError("no delegate available - service is currently out of order");
        }
    }

    /**
     * @deprecated provided for Be-release as backward compatibility relic
     */
    @Override
    @Deprecated
    public Future<RpcResult<GetFlowStatisticsFromFlowTableOutput>> getFlowStatisticsFromFlowTable(
            final GetFlowStatisticsFromFlowTableInput input) {
        if (delegate != null) {
            return delegate.getFlowStatisticsFromFlowTable(input);
        } else {
            throw new IllegalAccessError("no delegate available - service is currently out of order");
        }
    }
}
