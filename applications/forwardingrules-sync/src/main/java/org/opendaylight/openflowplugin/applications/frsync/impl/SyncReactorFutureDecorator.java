package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.concurrent.Callable;

import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                
                doSyncupInFuture(flowcapableNodePath, configTree, operationalTree);

                return true;// TODO forward doSyncup Future.get() ???
            }
        });

        return syncup;
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
