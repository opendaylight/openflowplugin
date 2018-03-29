package org.opendaylight.openflowplugin.applications.upgrademanager.api;

import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.Map;

public interface UpgradeManager extends ReconciliationNotificationListener, AutoCloseable {
        void start();

        BundleId getActiveBundle(InstanceIdentifier<FlowCapableNode> node);

}
