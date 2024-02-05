/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
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
        flowStatisticsService = new OpendaylightFlowStatisticsServiceImpl(rqContextStack, deviceContext,
            ConvertorManagerFactory.createDefaultManager());

        var rqContext = new AbstractRequestContext<>(Uint32.valueOf(42)) {
            @Override
            public void close() {
                //NOOP
            }
        };
        //Mockito.when(rqContextStack.<Object>createRequestContext()).thenReturn(rqContext);
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetAggregateFlowStatisticsFromFlowTableForAllFlows() {
        flowStatisticsService.getAggregateFlowStatisticsFromFlowTableForAllFlows(
            new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId(Uint8.ONE))
                .build());
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetAllFlowStatisticsFromFlowTable() {
        flowStatisticsService.getAllFlowStatisticsFromFlowTable(new GetAllFlowStatisticsFromFlowTableInputBuilder()
            .setNode(createNodeRef("unitProt:123"))
            .setTableId(new TableId(Uint8.ONE))
            .build());
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetAllFlowsStatisticsFromAllFlowTables() {
        flowStatisticsService.getAllFlowsStatisticsFromAllFlowTables(
            new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder().setNode(createNodeRef("unitProt:123")).build());
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetFlowStatisticsFromFlowTable() {
        final var resultFuture = flowStatisticsService.getFlowStatisticsFromFlowTable(
            new GetFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setPriority(Uint16.valueOf(5))
                .build());
    }
}