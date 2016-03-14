package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
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

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorGuardDecorator.class);

    private final SyncReactor delegate;
    private final SemaphoreKeeper<InstanceIdentifier<FlowCapableNode>> semaphoreKeeper;

    public SyncReactorGuardDecorator(SyncReactor delegate,
            SemaphoreKeeper<InstanceIdentifier<FlowCapableNode>> semaphoreKeeper) {
        this.delegate = delegate;
        this.semaphoreKeeper = semaphoreKeeper;
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("syncup {}", nodeId.getValue());



        final long stampBeforeGuard = System.nanoTime();
        final Semaphore guard = summonGuardAndAcquire(flowcapableNodePath);
        
        final long stampAfterGuard = System.nanoTime();
        if (LOG.isDebugEnabled()) {
            LOG.debug("syncup start {} waiting:{} guard:{} thread:{}", nodeId.getValue(),
                    formatNanos(stampAfterGuard - stampBeforeGuard),
                    guard, threadName());
        }


        final ListenableFuture<Boolean> endResult =
                delegate.syncup(flowcapableNodePath, configTree, operationalTree);

        Futures.addCallback(endResult, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable final Boolean result) {
                if (LOG.isDebugEnabled()) {
                    final long stampFinished = System.nanoTime();
                    LOG.debug("syncup finished {} took:{} rpc:{} wait:{} guard:{}, thread:{}", nodeId.getValue(),
                            formatNanos(stampFinished - stampBeforeGuard),
                            formatNanos(stampFinished - stampAfterGuard),
                            formatNanos(stampAfterGuard - stampBeforeGuard),
                            guard, threadName());
                }

                lockReleaseForNodeId(nodeId, guard);
            }

            @Override
            public void onFailure(final Throwable t) {
                if (LOG.isDebugEnabled()) {
                    final long stampFinished = System.nanoTime();
                    LOG.warn("syncup failed {} took:{} rpc:{} wait:{} guard:{} thread:{}", nodeId.getValue(),
                            formatNanos(stampFinished - stampBeforeGuard),
                            formatNanos(stampFinished - stampAfterGuard),
                            formatNanos(stampAfterGuard - stampBeforeGuard),
                            guard, threadName());
                }

                lockReleaseForNodeId(nodeId, guard);
            }
        });

        return endResult;
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
    protected Semaphore summonGuardAndAcquire(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath)
            throws InterruptedException {
        final Semaphore guard = Preconditions.checkNotNull(semaphoreKeeper.summonGuard(flowcapableNodePath),
                "no guard for " + flowcapableNodePath);

        if (LOG.isDebugEnabled()) {
            final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
            try {
                LOG.debug("syncup summon {} guard:{} thread:{}", nodeId.getValue(), guard, threadName());
            } catch (Exception e) {
                LOG.error("error logging guard after summon before aquiring {}", nodeId);
            }
        }

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

    static String threadName() {
        final Thread currentThread = Thread.currentThread();
        return currentThread.getName();
    }

}
