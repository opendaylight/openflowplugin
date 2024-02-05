/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link OpendaylightFlowStatisticsServiceImpl} - only delegated methods.
 */
public class OpendaylightFlowStatisticsServiceImpl3Test extends AbstractStatsServiceTest {
    @Mock
    private OpendaylightFlowStatisticsService flowStatisticsDelegate;

    private OpendaylightFlowStatisticsServiceImpl flowStatisticsService;

    @Override
    public void setUp() {
        flowStatisticsService = new OpendaylightFlowStatisticsServiceImpl(rqContextStack, deviceContext,
            ConvertorManagerFactory.createDefaultManager());
        flowStatisticsService.setDelegate(flowStatisticsDelegate);
    }

    @Test
    public void testGetAggregateFlowStatisticsFromFlowTableForAllFlows() {
        GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input =
                new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId(Uint8.ONE))
                .build();

        flowStatisticsService.getAggregateFlowStatisticsFromFlowTableForAllFlows(input);
        Mockito.verify(flowStatisticsDelegate).getAggregateFlowStatisticsFromFlowTableForAllFlows(input);
    }

    @Test
    public void testGetAllFlowStatisticsFromFlowTable() {
        GetAllFlowStatisticsFromFlowTableInput input = new GetAllFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId(Uint8.ONE))
                .build();

        flowStatisticsService.getAllFlowStatisticsFromFlowTable(input);
        Mockito.verify(flowStatisticsDelegate).getAllFlowStatisticsFromFlowTable(input);
    }

    @Test
    public void testGetAllFlowsStatisticsFromAllFlowTables() {
        GetAllFlowsStatisticsFromAllFlowTablesInput input = new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .build();

        flowStatisticsService.getAllFlowsStatisticsFromAllFlowTables(input);
        Mockito.verify(flowStatisticsDelegate).getAllFlowsStatisticsFromAllFlowTables(input);
    }

    @Test
    public void testGetFlowStatisticsFromFlowTable() {
        GetFlowStatisticsFromFlowTableInput input = new GetFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setPriority(Uint16.valueOf(5))
                .build();

        flowStatisticsService.getFlowStatisticsFromFlowTable(input);
        Mockito.verify(flowStatisticsDelegate).getFlowStatisticsFromFlowTable(input);
    }
}
