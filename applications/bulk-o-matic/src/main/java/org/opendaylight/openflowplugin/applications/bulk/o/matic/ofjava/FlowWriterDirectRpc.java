/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic.ofjava;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.applications.bulk.o.matic.BulkOMaticUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowWriterDirectRpc {
    private static final Logger LOG = LoggerFactory.getLogger(FlowWriterDirectRpc.class);
    private static final long PAUSE_BETWEEN_BATCH_MILLIS = 40;

    private final DataBroker dataBroker;
    private final ExecutorService flowPusher;
    private final FlowDirectServiceImpl<AddFlowOutput> flowAddService;

    FlowWriterDirectRpc(final DataBroker dataBroker, final OutboundQueue outboundQueue, final ExecutorService flowPusher) {
        this.dataBroker = dataBroker;
        this.flowPusher = flowPusher;
        this.flowAddService = new FlowDirectServiceImpl<>(outboundQueue);
    }

    void addFlow(final String nodeId, final int flowsPerDpn, final int batchSize) {
        getAllNodes()
                .filter(node -> node.equals(nodeId))
                .findFirst()
                .ifPresent(node -> flowPusher.execute(new FlowRPCHandlerTask(node, flowsPerDpn, batchSize)));
    }

    void addFlows(final int flowsPerDpn, final int batchSize) {
        getAllNodes()
                .forEach(node -> flowPusher.execute(new FlowRPCHandlerTask(node, flowsPerDpn, batchSize)));
    }

    private Stream<String> getAllNodes() {
        try {
            return Optional
                    .ofNullable(dataBroker
                            .newReadOnlyTransaction()
                            .read(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Nodes.class))
                            .checkedGet()
                            .orNull())
                    .map(input -> Optional
                            .ofNullable(input.getNode())
                            .map(nodes -> nodes
                                    .stream()
                                    .map(node -> node.getId().getValue()))
                            .orElse(Stream.empty()))
                    .orElse(Stream.empty());
        } catch (ReadFailedException e) {
            LOG.error("Failed to read connected nodes {}", e);
        }

        return Stream.empty();
    }

    private Future<RpcResult<AddFlowOutput>> addFlow(final FlowModInputBuilder input) {
        return flowAddService.handleServiceCall(input);
    }

    private class FlowRPCHandlerTask implements Runnable {
        private final String dpId;
        private final int flowsPerDpn;
        private final int batchSize;

        FlowRPCHandlerTask(final String dpId, final int flowsPerDpn, final int batchSize) {
            this.dpId = dpId;
            this.flowsPerDpn = flowsPerDpn;
            this.batchSize = batchSize;
        }

        @Override
        public void run() {

            short tableId = (short) 1;
            int initFlowId = 500;

            for (int i = 1; i <= flowsPerDpn; i++) {

                String flowId = Integer.toString(initFlowId + i);

                LOG.debug("Framing AddFlowInput for flow-id {}", flowId);

                InstanceIdentifier<Node> nodeIId = BulkOMaticUtils.getFlowCapableNodeId(dpId);
                InstanceIdentifier<Table> tableIId = BulkOMaticUtils.getTableId(tableId, dpId);
                InstanceIdentifier<Flow> flowIId = BulkOMaticUtils.getFlowId(tableIId, flowId);

                LOG.debug("RPC invocation for adding flow-id {}", flowId);
                addFlow(DirectFlowUtil.buildFlow(tableId, DirectFlowUtil.buildMatch(i)));


                if (i % batchSize == 0) {
                    try {
                        LOG.info("Pausing for {} MILLISECONDS after batch of {} RPC invocations",
                                PAUSE_BETWEEN_BATCH_MILLIS, batchSize);

                        TimeUnit.MILLISECONDS.sleep(PAUSE_BETWEEN_BATCH_MILLIS);
                    } catch (InterruptedException iEx) {
                        LOG.error("Interrupted while pausing after batched push upto {}. Ex {}", i, iEx);
                    }
                }
            }
        }
    }
}
