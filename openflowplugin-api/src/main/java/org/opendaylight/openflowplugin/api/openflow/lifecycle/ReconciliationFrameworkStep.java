package org.opendaylight.openflowplugin.api.openflow.lifecycle;

public interface ReconciliationFrameworkStep {
    /**
     * Allow to continue after reconciliation framework callback success.
     * @since 0.5.0 Nitrogen
     * @see org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService
     * @see OwnershipChangeListener#isReconciliationFrameworkRegistered()
     */
    void continueInitializationAfterReconciliation();
}