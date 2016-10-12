/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.FlowAddType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowDirectInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowRawInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowWriterDirectOFRpc {
    private static final Logger LOG = LoggerFactory.getLogger(FlowWriterDirectOFRpc.class);
    private static final long PAUSE_BETWEEN_BATCH_MILLIS = 40;
    private static final short TABLE_ID = (short)1;
    private static final int INIT_FLOW_ID = 500;

    private final DataBroker dataBroker;
    private final SalFlowService flowService;
    private final ExecutorService flowPusher;
    private final ConvertorExecutor convertorExecutor;

    FlowWriterDirectOFRpc(final DataBroker dataBroker,
                          final SalFlowService salFlowService,
                          final ExecutorService flowPusher) {
        this.dataBroker = dataBroker;
        this.flowService = salFlowService;
        this.flowPusher = flowPusher;
        this.convertorExecutor = ConvertorManagerFactory.createDefaultManager();
    }


    void rpcFlowAdd(final FlowAddType type, final String nodeId, final int flowsPerNode, final int batchSize){
        fetchAllNodes()
                .filter(node -> node.equals(nodeId))
                .findFirst()
                .ifPresent(pushFlow(type, flowsPerNode, batchSize));
    }

    void rpcFlowAddAll(final FlowAddType type, final int flowsPerNode, final int batchSize){
        fetchAllNodes().forEach(pushFlow(type, flowsPerNode, batchSize));
    }

    private Consumer<String> pushFlow(final FlowAddType type, final int flowsPerNode, final int batchSize) {
        return node -> flowPusher.execute(new FlowRPCHandlerTask(type, node, flowsPerNode, batchSize));
    }

    private Stream<String> fetchAllNodes() {
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
                                    .map(node -> {
                                        LOG.info("Switch with id={} discovered", node.getId().getValue());
                                        return node.getId().getValue();
                                    }))
                            .orElse(Stream.empty()))
                    .orElse(Stream.empty());
        } catch (ReadFailedException e) {
            LOG.error("Failed to read connected nodes {}", e);
        }

        return Stream.empty();
    }

    private class FlowRPCHandlerTask implements Runnable {
        private final Stream<Runnable> jobs;
        private final String dpId;
        private final int batchSize;
        private final InstanceIdentifier<Node> nodeIId;
        private final FlowAddType type;

        FlowRPCHandlerTask(final FlowAddType type,
                           final String dpId,
                           final int flowsPerDpn,
                           final int batchSize){
            this.type = type;
            this.dpId = dpId;
            this.batchSize = batchSize;
            this.nodeIId = BulkOMaticUtils.getFlowCapableNodeId(dpId);
            this.jobs = createJobs(dpId, flowsPerDpn);
        }

        private String typeToString() {
            switch(type) {
                case DIRECT: return "DIRECT";
                case RAW: return "RAW";
                case NORMAL:
                default: return "NORMAL";
            }
        }

        private Stream<Runnable> createJobs(final String nodeId, final int flowsPerNode) {
            switch (type) {
                case DIRECT:
                    return prepareFlows(nodeId, flowsPerNode)
                            .map(flow -> {
                                final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_3);
                                data.setDatapathId(InventoryDataServiceUtil.dataPathIdFromNodeId(new NodeId(nodeId)));
                                final Optional<List<FlowModInputBuilder>> directFlow = convertorExecutor.convert(flow, data);

                                return directFlow.map(flowModInputBuilders -> flowModInputBuilders
                                        .stream()
                                        .findFirst()
                                        .map(flowModInputBuilder ->
                                                new AddFlowDirectInputBuilder(flowModInputBuilder.build())
                                                        .setNode(flow.getNode())
                                                        .setFlowTable(flow.getFlowTable())
                                                        .setFlowRef(flow.getFlowRef())
                                                        .build()))
                                        .orElse(Optional.empty());
                            })
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(addFlowDirectInput -> (Runnable) () -> flowService.addFlowDirect(addFlowDirectInput));
                case RAW:
                    return prepareFlows(nodeId, flowsPerNode)
                            .map(addFlowInput -> new AddFlowRawInputBuilder((NodeFlow)addFlowInput)
                                    .setFlowRef(addFlowInput.getFlowRef())
                                    .build())
                            .map(addFlowRawInput -> (Runnable) () -> flowService.addFlowRaw(addFlowRawInput));
                case NORMAL:
                default:
                    return prepareFlows(nodeId, flowsPerNode)
                            .map(addFlowInput -> (Runnable) () -> flowService.addFlow(addFlowInput));
            }
        }

        private Stream<AddFlowInput> prepareFlows(final String nodeId, final int flowsPerNode) {
            final InstanceIdentifier<Table> tableIId = BulkOMaticUtils.getTableId(TABLE_ID, nodeId);

            return IntStream.range(0, flowsPerNode).mapToObj(i -> {
                final String flowId = Integer.toString(INIT_FLOW_ID + i);

                return new AddFlowInputBuilder(BulkOMaticUtils.buildFlow(TABLE_ID, flowId, BulkOMaticUtils.getMatch(i)))
                        .setNode(new NodeRef(nodeIId))
                        .setFlowTable(new FlowTableRef(tableIId))
                        .setFlowRef(new FlowRef(BulkOMaticUtils.getFlowId(tableIId, flowId)))
                        .build();
            });
        }

        @Override
        public void run() {
            LOG.info("Starting FlowRPCTaskHandler " + typeToString() + " for switch id={}", dpId);
            final int[] i = {0};

            jobs.forEach(runnable -> {
                runnable.run();

                if (i[0] % batchSize == 0) {
                    try {
                        LOG.info("Pausing for {} MILLISECONDS after batch of {} RPC invocations",
                                PAUSE_BETWEEN_BATCH_MILLIS, batchSize);

                        TimeUnit.MILLISECONDS.sleep(PAUSE_BETWEEN_BATCH_MILLIS);
                    } catch (InterruptedException iEx) {
                        LOG.error("Interrupted while pausing after batched push up to {}. Ex {}", i[0], iEx);
                    }
                }

                i[0]++;
            });
        }
    }
}
