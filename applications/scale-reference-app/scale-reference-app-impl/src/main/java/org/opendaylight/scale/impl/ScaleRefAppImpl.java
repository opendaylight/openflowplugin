package org.opendaylight.scale.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.IPriorityTaskManager;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.IOpenflowFacade;
import org.opendaylight.scale.Subsriber;
import org.opendaylight.scale.dataaccess.StorageWrapper;
import org.opendaylight.scale.inventory.NodeEventListener;
import org.opendaylight.scale.util.FlowUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.scale.ref.app.rev160923.*;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Created by evijayd on 9/26/2016.
 */
public class ScaleRefAppImpl implements ScaleRefAppModelService {

    private NodeEventListener nodeEventListener = null;
    private IPriorityTaskManager priorityTaskManager = null;
    private IOpenflowFacade ofFacade = null;
    private static Logger LOG = LoggerFactory.getLogger(ScaleRefAppImpl.class);
    private BulkModStatistics bulkModStatistics = null;

    private static ExecutorService executorService = ForkJoinPool.commonPool();

    public ScaleRefAppImpl(NodeEventListener nodeEventListener, IPriorityTaskManager priorityTaskManager, IOpenflowFacade ofFacade) {
        this.nodeEventListener = nodeEventListener;
        this.priorityTaskManager = priorityTaskManager;
        this.ofFacade = ofFacade;
    }

    @Override
    public Future<RpcResult<Void>> addSubscriberFilter(AddSubscriberFilterInput input) {
        // TODO: Implememt Error Handling
        StorageWrapper.getInstance().addSubscriber(Subsriber.create()
                .setId(input.getScfId())
                .setIpv4Prefix(input.getSrcIpPrefix())
                .setPortStart(input.getPortStart())
                .setPortEnd(input.getPortEnd())
                .setVni(input.getVni().intValue())
                .setPriority(input.getPriority())
                .setProfileId(input.getSubProfileId())
                .setDisplayName(input.getDisplayName()));
        Set<String> nodes = nodeEventListener.getDpnSet();
        LOG.info("Adding subscriber for {} number of nodes.", nodes.size());
        for (String node: nodes) {
            NodeId nodeId = new NodeId(node);
            Flow flow = FlowUtils.convertSubsToFlowIPPrefix(Long.toString(input.getScfId()), input.getSrcIpPrefix());
            ofFacade.modifyFlow(nodeId, flow, null, true);
        }
        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> bulkReadSubscriberFilter(BulkReadSubscriberFilterInput input) {
        long fetchSize = input.getFetchSize();
        StorageWrapper.getInstance().readAllSubscribers(fetchSize);

        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> bulkAddSubscriberFilter(BulkAddSubscriberFilterInput input) {
        // TODO: Implememt Error Handling
        // TODO: Use the Parallelism param
        Set<String> nodes = nodeEventListener.getDpnSet();
        LOG.info("Adding bulk subscribers for {} number of nodes.", nodes.size());

        bulkModStatistics = new BulkModStatistics();
        int ipIndex = 0;
        for (int i = input.getStartId().intValue(); i <= input.getEndId().intValue(); i++) {
            ipIndex++;
            bulkModStatistics.start(input.getEndId().intValue() - input.getStartId().intValue() + 1);
            SubscriberAdditionTask task = new SubscriberAdditionTask(String.valueOf(i), i,
                    ofFacade, nodes, bulkModStatistics,ipIndex);
            executorService.execute(task);
        }
        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<BulkModStatsOutput>> bulkModStats() {
        if (bulkModStatistics == null) {
            LOG.warn("Querying non-existent statistics.");
            RpcResultBuilder<BulkModStatsOutput> rpcResultBuilder = RpcResultBuilder.failed();
            return Futures.immediateFuture(rpcResultBuilder.build());
        }
        BulkModStatsOutputBuilder builder = new BulkModStatsOutputBuilder();
        long successCount = bulkModStatistics.getSuccessCount();
        long duration = (System.nanoTime() - bulkModStatistics.getStartTime())/(1000);
        long failureCount = bulkModStatistics.getFailureCount();
        long throughput = successCount/(duration/1000000);
        long meanLatency = duration/successCount;
        BulkModStatsOutput output = builder
                .setSuccessCount(successCount)
                .setFailureCount(failureCount)
                .setThroughput(throughput)
                .setMeanLatency(meanLatency)
                .build();
        LOG.info("BulkModStatistics: [successCount: {}, failureCount: {}, throughput: {}, meanLatency: {}",
                successCount, failureCount, throughput, meanLatency);
        RpcResultBuilder<BulkModStatsOutput> rpcResultBuilder = RpcResultBuilder.success(output);
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> removeSubscriberFilter(RemoveSubscriberFilterInput input) {
        // TODO: Implememt Error Handling
        StorageWrapper.getInstance().removeSubscriber(Subsriber.create()
                .setProfileId(input.getSubProfileId())
                .setIpv4Prefix(input.getSrcIpPrefix()));
        Set<String> nodes = nodeEventListener.getDpnSet();
        LOG.info("Removing subscriber for {} number of nodes.", nodes.size());
        for (String node: nodes) {
            NodeId nodeId = new NodeId(node);
            Flow flow = FlowUtils.convertSubsToFlowIPPrefix(Long.toString(input.getSubProfileId()), input.getSrcIpPrefix());
            ofFacade.deleteFlow(nodeId, flow, null, true);
        }
        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
