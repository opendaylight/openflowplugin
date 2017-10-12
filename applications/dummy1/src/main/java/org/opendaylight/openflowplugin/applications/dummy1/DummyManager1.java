package org.opendaylight.openflowplugin.applications.dummy1;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;

public interface DummyManager1 {
        void start();
        SalFlowService getFlowService();
}
