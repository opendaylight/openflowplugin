package org.opendaylight.openflowplugin.applications.reconciliation;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by eknnosd on 6/5/2017.
 */
public interface IReconciliationTaskFactory {
    public Future createReconcileTask(NodeId nodeId);
    void test(InstanceIdentifier<FlowCapableNode> nodeIdentity);
    int getpriority();
    String getServiceName();
    void cancelReconcileTask(NodeId nodeId);
}
