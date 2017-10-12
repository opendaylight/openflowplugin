package org.opendaylight.openflowplugin.applications.dummy1.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.dummy1.DummyManager1;
import org.opendaylight.openflowplugin.applications.dummy1.DummyReconciliation1;
import org.opendaylight.openflowplugin.applications.reconciliation.NotificationRegistration;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;

public class DummyManager1Impl implements DummyManager1 {

        private static final int FRM_RECONCILIATION_PRIORITY = Integer.getInteger("dummy1.reconciliation.priority", 1);
        private static final String SERVICE_NAME = "dummy1";

        private final DataBroker dataService;
        private NotificationRegistration reconciliationNotificationRegistration;
        private DummyReconciliation1 nodeListener;
        private final ReconciliationManager reconciliationManager;
        private final SalFlowService flowService;

        public DummyManager1Impl(DataBroker dataService, ReconciliationManager reconciliationManager, SalFlowService flowService) {
                this.dataService = dataService;
                this.reconciliationManager = reconciliationManager;
                this.flowService = flowService;

        }

        @Override
        public SalFlowService getFlowService() {
                return flowService;
        }

        @Override
        public void start() {
                this.nodeListener = new DummyReconciliation1Impl(this, dataService, SERVICE_NAME, FRM_RECONCILIATION_PRIORITY,
                        ResultState.DONOTHING);
                this.reconciliationNotificationRegistration = reconciliationManager.registerService(this.nodeListener);
        }

        @Override
        public void close() {
                if (this.reconciliationNotificationRegistration != null) {
                        try {
                                this.reconciliationNotificationRegistration.close();
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                        this.reconciliationNotificationRegistration = null;
                }
        }
}
