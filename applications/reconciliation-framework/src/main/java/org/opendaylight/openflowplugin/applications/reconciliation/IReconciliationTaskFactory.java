package org.opendaylight.openflowplugin.applications.reconciliation;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import java.util.concurrent.Future;

/**
 * Created by eknnosd on 6/5/2017.
 */
public interface IReconciliationTaskFactory extends AutoCloseable{
        public Future createReconcileTask(NodeId nodeId);
        int getPriority();
        String getServiceName();
        DefaultAction getAction();
        void cancelReconcileTask(NodeId nodeId);
}
