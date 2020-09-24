/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.concurrent.Future;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link OpendaylightFlowStatisticsServiceImpl} - only delegated methods (failing).
 */
public class OpendaylightFlowStatisticsServiceImpl1Test extends AbstractStatsServiceTest {

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private OpendaylightFlowStatisticsServiceImpl flowStatisticsService;

    @Override
    public void setUp() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        flowStatisticsService =
                OpendaylightFlowStatisticsServiceImpl.createWithOook(rqContextStack, deviceContext, convertorManager);

        AbstractRequestContext<Object> rqContext = new AbstractRequestContext<>(Uint32.valueOf(42)) {
            @Override
            public void close() {
                //NOOP
            }
        };
        //Mockito.when(rqContextStack.<Object>createRequestContext()).thenReturn(rqContext);
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetAggregateFlowStatisticsFromFlowTableForAllFlows() {
        GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder input =
                new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId(Uint8.ONE));

        flowStatisticsService.getAggregateFlowStatisticsFromFlowTableForAllFlows(input.build());
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetAllFlowStatisticsFromFlowTable() {
        GetAllFlowStatisticsFromFlowTableInputBuilder input = new GetAllFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId(Uint8.ONE));

        flowStatisticsService.getAllFlowStatisticsFromFlowTable(input.build());
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetAllFlowsStatisticsFromAllFlowTables() {
        GetAllFlowsStatisticsFromAllFlowTablesInputBuilder input =
                new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        flowStatisticsService.getAllFlowsStatisticsFromAllFlowTables(input.build());
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetFlowStatisticsFromFlowTable() {
        GetFlowStatisticsFromFlowTableInputBuilder input = new GetFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setPriority(Uint16.valueOf(5));

        final Future<RpcResult<GetFlowStatisticsFromFlowTableOutput>> resultFuture
                = flowStatisticsService.getFlowStatisticsFromFlowTable(input.build());
    }
}