package org.opendaylight.openflowplugin.applications.dummy1.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.applications.bulk.o.matic.BulkOMaticUtils;
import org.opendaylight.openflowplugin.applications.dummy1.DummyManager1;
import org.opendaylight.openflowplugin.applications.dummy1.DummyReconciliation1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DummyReconciliation1Impl implements DummyReconciliation1 {

        private static final Logger LOG = LoggerFactory.getLogger(DummyReconciliation1Impl.class);

        // The number of nanoseconds to wait for a single group to be added.
        private static final long ADD_GROUP_TIMEOUT = TimeUnit.SECONDS.toNanos(3);

        // The maximum number of nanoseconds to wait for completion of add-group RPCs.
        private static final long MAX_ADD_GROUP_TIMEOUT = TimeUnit.SECONDS.toNanos(20);
        private static final String SEPARATOR = ":";
        private static final int THREAD_POOL_SIZE = 4;

        private final Map<DeviceInfo, ListenableFuture<Boolean>> futureMap = new HashMap<>();
        private final DataBroker dataBroker;
        private final DummyManager1 provider;
        private final String serviceName;
        private final int priority;
        private final ResultState resultState;

        private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        private static final AtomicLong BUNDLE_ID = new AtomicLong();
        private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);



        public DummyReconciliation1Impl(DummyManager1Impl provider, DataBroker dataBroker, String serviceName,
                int priority, ResultState resultState) {
                this.dataBroker = dataBroker;
                this.provider = provider;
                this.serviceName = serviceName;
                this.priority = priority;
                this.resultState = resultState;
        }

        @Override public ListenableFuture<Boolean> startReconciliation(DeviceInfo node) {
                InstanceIdentifier<FlowCapableNode> connectedNode = node.getNodeInstanceIdentifier()
                        .augmentation(FlowCapableNode.class);
                return futureMap.computeIfAbsent(node, future -> reconcileConfiguration(connectedNode));
        }

        @Override public ListenableFuture<Boolean> endReconciliation(DeviceInfo node) {
                futureMap.computeIfPresent(node, (key, future) -> future).cancel(true);
                futureMap.remove(node);
                return Futures.immediateFuture(true);
        }

        private ListenableFuture<Boolean> reconcileConfiguration(InstanceIdentifier<FlowCapableNode> connectedNode) {
                LOG.info("Triggering reconciliation for device {}", connectedNode.firstKeyOf(Node.class));
                ReconciliationTask reconciliationTask = new ReconciliationTask(connectedNode);
                return JdkFutureAdapters.listenInPoolThread(executor.submit(reconciliationTask));

        }

        private class ReconciliationTask implements Callable<Boolean> {

                InstanceIdentifier<FlowCapableNode> nodeIdentity;

                ReconciliationTask(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
                        nodeIdentity = nodeIdent;
                }

                @Override public Boolean call() {
                        String node = nodeIdentity.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
                        BigInteger dpnId = getDpnIdFromNodeName(node);

                        String flowId = Integer.toString(50001);
                        Match match = BulkOMaticUtils.getMatch(1);
                        short tableId = (short) 1;
                        InstanceIdentifier<Node> nodeIId = BulkOMaticUtils.getFlowCapableNodeId(dpnId.toString());
                        InstanceIdentifier<Table> tableIId = BulkOMaticUtils.getTableId(tableId, dpnId.toString());
                        InstanceIdentifier<Flow> flowIId = BulkOMaticUtils.getFlowId(tableIId, flowId);

                        Flow flow = BulkOMaticUtils.buildFlow(tableId, flowId, match);

                        AddFlowInputBuilder builder = new AddFlowInputBuilder(flow);
                        builder.setNode(new NodeRef(nodeIId));
                        builder.setFlowTable(new FlowTableRef(tableIId));
                        builder.setFlowRef(new FlowRef(flowIId));

                        AddFlowInput addFlowInput = builder.build();

                        LOG.debug("RPC invocation for adding flow-id {} with input {}", flowId, addFlowInput.toString());
                        provider.getFlowService().addFlow(addFlowInput);

                        return true;
                }
        }

        private BigInteger getDpnIdFromNodeName(String nodeName) {

                String dpId = nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
                return new BigInteger(dpId);
        }

        @Override public int getPriority() {
                return priority;
        }

        @Override public String getName() {
                return serviceName;
        }

        @Override public ResultState getResultState() {
                return resultState;
        }

        @Override public void close() throws Exception {

        }
}
