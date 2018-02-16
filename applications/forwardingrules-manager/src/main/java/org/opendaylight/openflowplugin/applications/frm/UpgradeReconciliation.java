package org.opendaylight.openflowplugin.applications.frm;

import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.Map;

public interface UpgradeReconciliation extends ReconciliationNotificationListener, AutoCloseable{
        Map<InstanceIdentifier<FlowCapableNode>, BundleId> getBundleIdMap();

}
