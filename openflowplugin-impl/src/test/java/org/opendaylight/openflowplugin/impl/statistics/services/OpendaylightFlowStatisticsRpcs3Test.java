/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.collect.ClassToInstanceMap;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link OpendaylightFlowStatisticsRpcs} - only delegated methods.
 */
public class OpendaylightFlowStatisticsRpcs3Test extends AbstractStatsServiceTest {

    @Mock
    private OpendaylightFlowStatisticsRpcs flowStatisticsDelegate;

    private OpendaylightFlowStatisticsRpcs flowStatisticsRpcs;
    private ClassToInstanceMap<Rpc<?, ?>> rpcMap;
    private ClassToInstanceMap<Rpc<?, ?>> rpcDelegateMap;

    @Override
    public void setUp() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        flowStatisticsRpcs =
                OpendaylightFlowStatisticsRpcs.createWithOook(rqContextStack, deviceContext, convertorManager);
        flowStatisticsRpcs.setDelegate(flowStatisticsDelegate);
        rpcMap = flowStatisticsRpcs.getRpcClassToInstanceMap();
        rpcDelegateMap = Mockito.verify(flowStatisticsDelegate).getRpcClassToInstanceMap();
    }

    @Test
    public void testGetAggregateFlowStatisticsFromFlowTableForAllFlows() {
        GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input =
                new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId(Uint8.ONE))
                .build();

        rpcMap.getInstance(GetAggregateFlowStatisticsFromFlowTableForAllFlows.class).invoke(input);
        rpcDelegateMap.getInstance(GetAggregateFlowStatisticsFromFlowTableForAllFlows.class).invoke(input);
    }

    @Test
    public void testGetAllFlowStatisticsFromFlowTable() {
        GetAllFlowStatisticsFromFlowTableInput input = new GetAllFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId(Uint8.ONE))
                .build();

        rpcMap.getInstance(GetAllFlowStatisticsFromFlowTable.class).invoke(input);
        rpcDelegateMap.getInstance(GetAllFlowStatisticsFromFlowTable.class).invoke(input);
    }

    @Test
    public void testGetAllFlowsStatisticsFromAllFlowTables() {
        GetAllFlowsStatisticsFromAllFlowTablesInput input = new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .build();

        rpcMap.getInstance(GetAllFlowsStatisticsFromAllFlowTables.class).invoke(input);
        rpcDelegateMap.getInstance(GetAllFlowsStatisticsFromAllFlowTables.class).invoke(input);
    }

    @Test
    public void testGetFlowStatisticsFromFlowTable() {
        GetFlowStatisticsFromFlowTableInput input = new GetFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setPriority(Uint16.valueOf(5))
                .build();

        rpcMap.getInstance(GetFlowStatisticsFromFlowTable.class).invoke(input);
        rpcDelegateMap.getInstance(GetFlowStatisticsFromFlowTable.class).invoke(input);
    }
}
