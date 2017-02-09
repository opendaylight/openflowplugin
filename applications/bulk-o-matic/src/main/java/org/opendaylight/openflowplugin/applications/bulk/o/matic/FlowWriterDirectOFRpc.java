/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import com.google.common.base.Optional;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowWriterDirectOFRpc {

    private static final Logger LOG = LoggerFactory.getLogger(FlowWriterDirectOFRpc.class);
    private final DataBroker dataBroker;
    private final SalFlowService flowService;
    private final ExecutorService flowPusher;
    private static final long PAUSE_BETWEEN_BATCH_MILLIS = 40;

    public FlowWriterDirectOFRpc(final DataBroker dataBroker,
                                 final SalFlowService salFlowService,
                                 final ExecutorService flowPusher) {
        this.dataBroker = dataBroker;
        this.flowService = salFlowService;
        this.flowPusher = flowPusher;
    }


    public void rpcFlowAdd(String dpId, int flowsPerDpn, int batchSize){
        if (!getAllNodes().isEmpty() && getAllNodes().contains(dpId)) {
            FlowRPCHandlerTask addFlowRpcTask = new FlowRPCHandlerTask(dpId, flowsPerDpn, batchSize);
            flowPusher.execute(addFlowRpcTask);
        }
    }

    public void rpcFlowAddAll(int flowsPerDpn, int batchSize){
        Set<String> nodeIdSet = getAllNodes();
        if (nodeIdSet.isEmpty()){
            LOG.warn("No nodes seen on OPERATIONAL DS. Aborting !!!!");
        }
        else{
            for (String dpId : nodeIdSet){
                LOG.info("Starting FlowRPCTaskHandler for switch id {}", dpId);
                FlowRPCHandlerTask addFlowRpcTask = new FlowRPCHandlerTask(dpId, flowsPerDpn, batchSize);
                flowPusher.execute(addFlowRpcTask);
            }
        }
    }

    private Set<String> getAllNodes(){

        Set<String> nodeIds = new HashSet<>();
        InstanceIdentifier<Nodes> nodes = InstanceIdentifier.create(Nodes.class);
        ReadOnlyTransaction rTx = dataBroker.newReadOnlyTransaction();

        try {
            Optional<Nodes> nodesDataNode = rTx.read(LogicalDatastoreType.OPERATIONAL, nodes).checkedGet();
            if (nodesDataNode.isPresent()){
                List<Node> nodesCollection = nodesDataNode.get().getNode();
                if (nodesCollection != null && !nodesCollection.isEmpty()) {
                    for (Node node : nodesCollection) {
                        LOG.info("Switch with ID {} discovered !!", node.getId().getValue());
                        nodeIds.add(node.getId().getValue());
                    }
                }
                else{
                    return Collections.emptySet();
                }
            }
            else{
                return Collections.emptySet();
            }
        }
        catch(ReadFailedException rdFailedException){
            LOG.error("Failed to read connected nodes {}", rdFailedException);
        }
        return nodeIds;
    }

    public class FlowRPCHandlerTask implements Runnable {
        private final String dpId;
        private final int flowsPerDpn;
        private final int batchSize;

        public FlowRPCHandlerTask(final String dpId,
                                  final int flowsPerDpn,
                                  final int batchSize){
            this.dpId = dpId;
            this.flowsPerDpn = flowsPerDpn;
            this.batchSize = batchSize;
        }

        @Override
        public void run() {

            short tableId = (short)1;
            int initFlowId = 500;

            for (int i=1; i<= flowsPerDpn; i++){

                String flowId = Integer.toString(initFlowId + i);

                LOG.debug("Framing AddFlowInput for flow-id {}", flowId);

                Match match = BulkOMaticUtils.getMatch(i);
                InstanceIdentifier<Node> nodeIId = BulkOMaticUtils.getFlowCapableNodeId(dpId);
                InstanceIdentifier<Table> tableIId = BulkOMaticUtils.getTableId(tableId, dpId);
                InstanceIdentifier<Flow> flowIId = BulkOMaticUtils.getFlowId(tableIId, flowId);

                Flow flow = BulkOMaticUtils.buildFlow(tableId, flowId, match);

                AddFlowInputBuilder builder = new AddFlowInputBuilder(flow);
                builder.setNode(new NodeRef(nodeIId));
                builder.setFlowTable(new FlowTableRef(tableIId));
                builder.setFlowRef(new FlowRef(flowIId));

                AddFlowInput addFlowInput = builder.build();

                LOG.debug("RPC invocation for adding flow-id {} with input {}", flowId,
                        addFlowInput.toString());
                flowService.addFlow(addFlowInput);

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
