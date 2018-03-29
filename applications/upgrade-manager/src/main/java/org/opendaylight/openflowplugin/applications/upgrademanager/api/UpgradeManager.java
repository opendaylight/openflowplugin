package org.opendaylight.openflowplugin.applications.upgrademanager.api;

import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.upgrade.mode.rev180328.Mode;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface UpgradeManager extends ReconciliationNotificationListener, AutoCloseable {
        void start();

        BundleId getActiveBundle(InstanceIdentifier<FlowCapableNode> node);

        Mode getUpgradeMode();

}
