/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * The Flow direct statistics service.
 */
public abstract class AbstractGetFlowStatistics<T extends OfHeader>
        extends AbstractDirectStatisticsService<GetFlowStatisticsInput, GetFlowStatisticsOutput, T>
        implements GetFlowStatistics {
    protected AbstractGetFlowStatistics(final RequestContextStack requestContextStack,
            final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor,
            final MultipartWriterProvider statisticsWriterProvider) {
        super(MultipartType.OFPMPFLOW, requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);
    }

    @Override
    public final ListenableFuture<RpcResult<GetFlowStatisticsOutput>> invoke(final GetFlowStatisticsInput input) {
        return handleAndReply(input);
    }

    /**
     * Get flow ID from #{@link org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry} or
     * create alien ID.
     * @param flowStatistics flow statistics
     * @return generated flow ID
     */
    protected FlowId generateFlowId(final FlowAndStatisticsMapList flowStatistics) {
        final FlowStatisticsDataBuilder flowStatisticsDataBld = new FlowStatisticsDataBuilder()
                .setFlowStatistics(new FlowStatisticsBuilder(flowStatistics).build());

        final FlowBuilder flowBuilder = new FlowBuilder(flowStatistics)
            .withKey(FlowRegistryKeyFactory.DUMMY_FLOW_KEY)
            .addAugmentation(flowStatisticsDataBld.build());

        final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(getVersion(), flowBuilder.build());

        getDeviceRegistry().getDeviceFlowRegistry().store(flowRegistryKey);
        return getDeviceRegistry().getDeviceFlowRegistry().retrieveDescriptor(flowRegistryKey).getFlowId();
    }

}
