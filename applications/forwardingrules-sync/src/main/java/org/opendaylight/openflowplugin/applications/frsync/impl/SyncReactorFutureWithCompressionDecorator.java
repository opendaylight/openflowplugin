package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.annotation.concurrent.GuardedBy;

import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Run delegate in Future but with State Compression
 * 
 * @author joslezak
 */
public class SyncReactorFutureWithCompressionDecorator extends SyncReactorFutureDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorFutureWithCompressionDecorator.class);

    @GuardedBy("beforeCompressionGuard")
    final Map<InstanceIdentifier<FlowCapableNode>, Pair<FlowCapableNode, FlowCapableNode>> beforeCompression =
            new HashMap<>();
    final Semaphore beforeCompressionGuard = new Semaphore(1, false);

    public SyncReactorFutureWithCompressionDecorator(SyncReactor delegate, ListeningExecutorService executorService) {
        super(delegate, executorService);
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("syncup {}", nodeId.getValue());

        final boolean newFutureNecessary = updateCompressionState(flowcapableNodePath, configTree, operationalTree);
        if (newFutureNecessary) {
            return super.syncup(flowcapableNodePath, configTree, operationalTree);
        } else {
            return Futures.immediateFuture(true);
        }
    }

    protected ListenableFuture<Boolean> doSyncupInFuture(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree)
                    throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("doSyncupInFuture {}", nodeId.getValue());

        final Pair<FlowCapableNode, FlowCapableNode> lastCompressionState =
                removeLastCompressionState(flowcapableNodePath);
        if (lastCompressionState == null) {
            return Futures.immediateFuture(true);
        } else {
            return super.doSyncupInFuture(flowcapableNodePath,
                    lastCompressionState.getLeft(), lastCompressionState.getRight());
        }
    }

    protected boolean updateCompressionState(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree) {
        try {
            try {
                beforeCompressionGuard.acquire();
            } catch (InterruptedException e) {
                return false;
            }
            
            final Pair<FlowCapableNode, FlowCapableNode> previous = beforeCompression.get(flowcapableNodePath);
            if (previous != null) {
                beforeCompression.put(flowcapableNodePath, Pair.of(configTree, previous.getRight()));
                return false;
            } else {
                beforeCompression.put(flowcapableNodePath, Pair.of(configTree, operationalTree));
                return true;
            }
        } finally {
            beforeCompressionGuard.release();
        }
    }

    protected Pair<FlowCapableNode, FlowCapableNode> removeLastCompressionState(
            final InstanceIdentifier<FlowCapableNode> flowcapableNodePath) {
        try {
            try {
                beforeCompressionGuard.acquire();
            } catch (InterruptedException e) {
                return null;
            }
            
            return beforeCompression.remove(flowcapableNodePath);
        } finally {
            beforeCompressionGuard.release();
        }
    }
}
