package org.opendaylight.scale.impl;

import com.google.common.util.concurrent.Futures;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.IPriorityTaskManager;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.IOpenflowFacade;
import org.opendaylight.scale.Subsriber;
import org.opendaylight.scale.dataaccess.StorageWrapper;
import org.opendaylight.scale.inventory.NodeEventListener;
import org.opendaylight.scale.util.FlowUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.scale.ref.app.rev160923.AddSubscriberFilterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.scale.ref.app.rev160923.BulkAddSubscriberFilterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.scale.ref.app.rev160923.RemoveSubscriberFilterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.scale.ref.app.rev160923.ScaleRefAppModelService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
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
            Flow flow = FlowUtils.convertSubsToFlow(Long.toString(input.getScfId()), input.getSrcIpPrefix());
            ofFacade.modifyFlow(nodeId, flow, null, true);
        }
        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> bulkAddSubscriberFilter(BulkAddSubscriberFilterInput input) {
        // TODO: Implememt Error Handling
        // TODO: Use the Parallelism param
        Set<String> nodes = nodeEventListener.getDpnSet();
        LOG.info("Adding bulk subscribers for {} number of nodes.", nodes.size());

        for (int i = 0; i < input.getSubFilterCount().intValue(); i++) {
            SubscriberAdditionTask task = new SubscriberAdditionTask(String.valueOf(i), i, ofFacade, nodes);
            executorService.execute(task);
        }
        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
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
            Flow flow = FlowUtils.convertSubsToFlow(Long.toString(input.getSubProfileId()), input.getSrcIpPrefix());
            ofFacade.deleteFlow(nodeId, flow, null, true);
        }
        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
