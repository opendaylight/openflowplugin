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
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Test for {@link OpendaylightFlowStatisticsServiceImpl} - only delegated methods (failing)
 */
public class OpendaylightFlowStatisticsServiceImpl1Test extends AbstractStatsServiceTest {

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private AbstractRequestContext<Object> rqContext;

    private OpendaylightFlowStatisticsServiceImpl flowStatisticsService;

    public void setUp() {
        flowStatisticsService = new OpendaylightFlowStatisticsServiceImpl(rqContextStack, deviceContext);

        rqContext = new AbstractRequestContext<Object>(42L) {
            @Override
            public void close() {
                //NOOP
            }
        };
        Mockito.when(rqContextStack.<Object>createRequestContext()).thenReturn(rqContext);
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetAggregateFlowStatisticsFromFlowTableForAllFlows() throws Exception {
        GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder input = new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId((short) 1));

        flowStatisticsService.getAggregateFlowStatisticsFromFlowTableForAllFlows(input.build());
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetAllFlowStatisticsFromFlowTable() throws Exception {
        GetAllFlowStatisticsFromFlowTableInputBuilder input = new GetAllFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId((short) 1));

        flowStatisticsService.getAllFlowStatisticsFromFlowTable(input.build());
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetAllFlowsStatisticsFromAllFlowTables() throws Exception {
        GetAllFlowsStatisticsFromAllFlowTablesInputBuilder input = new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        flowStatisticsService.getAllFlowsStatisticsFromAllFlowTables(input.build());
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetFlowStatisticsFromFlowTable() throws Exception {
        GetFlowStatisticsFromFlowTableInputBuilder input = new GetFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setPriority(5);

        final Future<RpcResult<GetFlowStatisticsFromFlowTableOutput>> resultFuture
                = flowStatisticsService.getFlowStatisticsFromFlowTable(input.build());
    }
}