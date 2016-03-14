package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Run delegate in Future
 * 
 * @author joslezak
 */
public class SyncReactorFutureDecorator implements SyncReactor {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorFutureDecorator.class);

    private final SyncReactor delegate;
    private final ListeningExecutorService executorService;

    public static final String FRM_RPC_CLIENT_PREFIX = "FRM-RPC-client-";

    public SyncReactorFutureDecorator(SyncReactor delegate, ListeningExecutorService executorService) {
        this.delegate = delegate;
        this.executorService = executorService;
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("syncup {}", nodeId.getValue());

        final ListenableFuture<Boolean> syncup = executorService.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                final String oldThreadName = updateThreadName(nodeId);
                
                final ListenableFuture<Boolean> endResult = doSyncupInFuture(flowcapableNodePath, configTree, operationalTree);

                Futures.addCallback(endResult, new FutureCallback<Boolean>() {
                    @Override
                    public void onSuccess(@Nullable final Boolean result) {
                        updateThreadName(oldThreadName);
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        updateThreadName(oldThreadName);
                    }
                });

                return true;// TODO forward doSyncup Future???
            }
        });

        return syncup;
    }

    protected String updateThreadName(NodeId nodeId) {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        try {
            currentThread.setName(oldName + "@" + nodeId.getValue());
        } catch (Exception e) {
            LOG.error("failed updating threadName");
        }
        return oldName;
    }
    
    protected String updateThreadName(String name) {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        try {
            currentThread.setName(name);
        } catch (Exception e) {
            LOG.error("failed updating threadName");
        }
        return oldName;
    }

    protected ListenableFuture<Boolean> doSyncupInFuture(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree)
                    throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("doSyncupInFuture {}", nodeId.getValue());
        
        // TODO final ListenableFuture<Boolean> endResult =
        return delegate.syncup(flowcapableNodePath, configTree, operationalTree);
    }
}
