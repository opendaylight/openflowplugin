package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.List;
import java.util.concurrent.Semaphore;

import javax.annotation.Nullable;

import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * NodeId level locking
 * 
 * @author joslezak
 */
public class SyncReactorGuardDecorator implements SyncReactor {

    private static final Logger LOG = LoggerFactory.getLogger(SimplifiedOperationalListener.class);

    private final SyncReactor delegate;
    private final SemaphoreKeeper<NodeId> semaphoreKeeper;

    public SyncReactorGuardDecorator(SyncReactor delegate, SemaphoreKeeper<NodeId> semaphoreKeeper) {
        this.delegate = delegate;
        this.semaphoreKeeper = semaphoreKeeper;
    }

    public ListenableFuture<RpcResult<Void>> syncup(InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            FlowCapableNode configTree, FlowCapableNode operationalTree) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);

        LOG.trace("syncup {}", nodeId.getValue());


        final long stampBeforeGuard = System.currentTimeMillis();
        final Semaphore guard = lockAcquireForNodeId(nodeId);
        final long stampAfterGuard = System.currentTimeMillis();
        
        LOG.debug("syncup start {} waiting:{}ms", nodeId.getValue(), stampAfterGuard - stampBeforeGuard);

        final ListenableFuture<RpcResult<Void>> endResult = delegate.syncup(flowcapableNodePath, configTree, operationalTree);

        @SuppressWarnings("unchecked")
        final ListenableFuture<List<RpcResult<Void>>> finalResult = Futures.allAsList(endResult);
        Futures.addCallback(finalResult, new FutureCallback<List<RpcResult<Void>>>() {
            @Override
            public void onSuccess(@Nullable final List<RpcResult<Void>> result) {
                final long stampFinished = System.currentTimeMillis();
                LOG.debug("syncup finished {} took:{}ms", nodeId, stampFinished - stampAfterGuard);
                
                lockReleaseForNodeId(nodeId, guard);
            }

            @Override
            public void onFailure(final Throwable t) {
                final long stampFinished = System.currentTimeMillis();
                LOG.warn("syncup failed {} took:{}ms" + nodeId, stampFinished - stampAfterGuard);
                
                lockReleaseForNodeId(nodeId, guard);
            }
        });
        
        return endResult;
    }
    
    /**
     * get guard and lock per node
     *
     * @param nodeId
     * @return
     */
    protected Semaphore lockAcquireForNodeId(final NodeId nodeId) throws InterruptedException {
        final Semaphore guard = Preconditions.checkNotNull(semaphoreKeeper.summonGuard(nodeId));
        guard.acquire();
        return guard;
    }

    /**
     * unlock per node
     *
     * @param nodeId
     * @param guard
     */
    protected void lockReleaseForNodeId(final NodeId nodeId,
                                        final Semaphore guard) {
        if (guard == null) {
            return;
        }
        guard.release();
    }

    public void setFlowForwarder(FlowForwarder flowForwarder) {
        delegate.setFlowForwarder(flowForwarder);
    }

    public void setTableForwarder(TableForwarder tableForwarder) {
        delegate.setTableForwarder(tableForwarder);
    }

    public void setMeterForwarder(MeterForwarder meterForwarder) {
        delegate.setMeterForwarder(meterForwarder);
    }

    public void setGroupForwarder(GroupForwarder groupForwarder) {
        delegate.setGroupForwarder(groupForwarder);
    }

    public void setTransactionService(FlowCapableTransactionService transactionService) {
        delegate.setTransactionService(transactionService);
    }
}
