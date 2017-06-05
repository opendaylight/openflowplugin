package org.opendaylight.openflowplugin.applications.reconciliation;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import java.util.List;
import java.util.Map;

/**
 * Created by eknnosd on 5/24/2017.
 */
public interface IReconciliationManager {
    public void registerService(IReconciliationTaskFactory object);
    public ListenableFuture<NodeId> startReconciliationTask(NodeId nodeId);
    public void cancelReconciliationTask(NodeId nodeId);
    Map<Integer,List<IReconciliationTaskFactory>> getRegisteredServices();

}
