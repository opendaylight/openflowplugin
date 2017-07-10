package org.opendaylight.openflowplugin.applications.reconciliation;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by eknnosd on 5/24/2017.
 */
public interface IReconciliationManager {
        public void registerService(IReconciliationTaskFactory object);
        public Future<NodeId> startReconciliationTask(NodeId nodeId);
        public void cancelReconciliationTask(NodeId nodeId);
        Map<Integer,List<IReconciliationTaskFactory>> getRegisteredServices();

}
