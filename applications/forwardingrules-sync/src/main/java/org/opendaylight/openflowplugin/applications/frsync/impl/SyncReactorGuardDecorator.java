package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * NodeId level locking
 * 
 * @author joslezak
 */
public class SyncReactorGuardDecorator implements SyncReactor {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorGuardDecorator.class);

    private final SyncReactor delegate;
    private final SemaphoreKeeper<InstanceIdentifier<FlowCapableNode>> semaphoreKeeper;

    private ListeningExecutorService executorService;

    public SyncReactorGuardDecorator(SyncReactor delegate,
            SemaphoreKeeper<InstanceIdentifier<FlowCapableNode>> semaphoreKeeper,
            ListeningExecutorService executorService) {
        this.delegate = delegate;
        this.semaphoreKeeper = semaphoreKeeper;
        this.executorService = executorService;
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("syncup {}", nodeId.getValue());

        final Semaphore guard = summonGuard(flowcapableNodePath);

        final ListenableFuture<Boolean> syncup = executorService.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                final long stampBeforeGuard = System.nanoTime();
                acquire(guard);
                final long stampAfterGuard = System.nanoTime();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("syncup start {} waiting:{} guard:{}", nodeId.getValue(),
                            formatNanos(stampAfterGuard - stampBeforeGuard),
                            guard);
                }

                final ListenableFuture<Boolean> endResult =
                        delegate.syncup(flowcapableNodePath, configTree, operationalTree);

                Futures.addCallback(endResult, new FutureCallback<Boolean>() {
                    @Override
                    public void onSuccess(@Nullable final Boolean result) {
                        if (LOG.isDebugEnabled()) {
                            final long stampFinished = System.nanoTime();
                            LOG.debug("syncup finished {} took:{} rpc:{} wait:{} guard:{}", nodeId.getValue(),
                                    formatNanos(stampFinished - stampBeforeGuard),
                                    formatNanos(stampFinished - stampAfterGuard),
                                    formatNanos(stampAfterGuard - stampBeforeGuard),
                                    guard);
                        }

                        lockReleaseForNodeId(nodeId, guard);
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        if (LOG.isDebugEnabled()) {
                            final long stampFinished = System.nanoTime();
                            LOG.warn("syncup failed {} took:{} rpc:{} wait:{} guard:{}", nodeId.getValue(),
                                    formatNanos(stampFinished - stampBeforeGuard),
                                    formatNanos(stampFinished - stampAfterGuard),
                                    formatNanos(stampAfterGuard - stampBeforeGuard),
                                    guard);
                        }

                        lockReleaseForNodeId(nodeId, guard);
                    }
                });

                return null;
            }


        });

        return syncup;
    }

    protected String formatNanos(long nanos) {
        return "'" + TimeUnit.NANOSECONDS.toMillis(nanos) + " ms'";
        // return "'" + NumberFormat.getNumberInstance().format(nanos) + " sec'";
    }

    /**
     * get guard
     *
     * @param flowcapableNodePath
     * @return
     */
    protected Semaphore summonGuard(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath)
            throws InterruptedException {
        return Preconditions.checkNotNull(semaphoreKeeper.summonGuard(flowcapableNodePath), 
                "no guard for " + flowcapableNodePath);
    }

    private void acquire(final Semaphore guard) throws InterruptedException {
        if (guard == null) {
            return;
        }
        guard.acquire();
    };

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
