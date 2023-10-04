/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import com.google.common.util.concurrent.ListenableFuture;
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
import org.opendaylight.yangtools.yang.common.RpcResult;

public interface OpendaylightFlowStatistics {

    /**
     * Invoke {@code get-all-flows-statistics-from-all-flow-tables} RPC.
     *
     * <pre>
     *     <code>
     *         Fetch statistics of all the flow present in all the flow tables of the switch
     *     </code>
     * </pre>
     *
     *
     * @param input of {@code get-all-flows-statistics-from-all-flow-tables}
     * @return output of {@code get-all-flows-statistics-from-all-flow-tables}
     *
     */
    ListenableFuture<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> getAllFlowsStatisticsFromAllFlowTables(
            GetAllFlowsStatisticsFromAllFlowTablesInput input);

    /**
     * Invoke {@code get-all-flow-statistics-from-flow-table} RPC.
     *
     * <pre>
     *     <code>
     *         Fetch statistics of all the flow present in the specific flow table of the
     *         switch
     *     </code>
     * </pre>
     *
     *
     * @param input of {@code get-all-flow-statistics-from-flow-table}
     * @return output of {@code get-all-flow-statistics-from-flow-table}
     *
     */
    ListenableFuture<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> getAllFlowStatisticsFromFlowTable(
            GetAllFlowStatisticsFromFlowTableInput input);

    /**
     * Invoke {@code get-flow-statistics-from-flow-table} RPC.
     *
     * <pre>
     *     <code>
     *         Fetch statistics of the specific flow present in the specific flow table of the
     *         switch
     *     </code>
     * </pre>
     *
     *
     * @param input of {@code get-flow-statistics-from-flow-table}
     * @return output of {@code get-flow-statistics-from-flow-table}
     *
     */
    ListenableFuture<RpcResult<GetFlowStatisticsFromFlowTableOutput>> getFlowStatisticsFromFlowTable(
            GetFlowStatisticsFromFlowTableInput input);

    /**
     * Invoke {@code get-aggregate-flow-statistics-from-flow-table-for-all-flows} RPC.
     *
     * <pre>
     *     <code>
     *         Fetch aggregate statistics for all the flows present in the specific flow table
     *         of the switch
     *     </code>
     * </pre>
     *
     *
     * @param input of {@code get-aggregate-flow-statistics-from-flow-table-for-all-flows}
     * @return output of {@code get-aggregate-flow-statistics-from-flow-table-for-all-flows}
     *
     */
    ListenableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>>
        getAggregateFlowStatisticsFromFlowTableForAllFlows(
                GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input);

    /**
     * Invoke {@code get-aggregate-flow-statistics-from-flow-table-for-given-match} RPC.
     *
     * <pre>
     *     <code>
     *         Fetch aggregate statistics for flows filtered by - table (eventually all tables)
     *         - match - port - group - cookie This values are contained in flow (among
     *         others). TODO:: filter values should be modeled more exact - omitting unusable
     *         fields.
     *     </code>
     * </pre>
     *
     *
     * @param input of {@code get-aggregate-flow-statistics-from-flow-table-for-given-match}
     * @return output of {@code get-aggregate-flow-statistics-from-flow-table-for-given-match}
     *
     */
    ListenableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>>
        getAggregateFlowStatisticsFromFlowTableForGivenMatch(
                GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input);

}
