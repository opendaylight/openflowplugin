package org.opendaylight.openflowplugin.applications.reconciliation.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.reconciliation.IReconciliationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eknnosd on 5/24/2017.
 */
public class ReconciliationManager implements IReconciliationManager {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationManager.class);

    static List<ServiceableNode> serviceableNodes = new ArrayList<>();
    private final DataBroker dataService;

    public ReconciliationManager(DataBroker dataService) {
        this.dataService = dataService;
    }


    public void start() {
        LOG.info("{} start", getClass().getSimpleName());
    }

    @Override
    public void registerService(String serviceName, Object object) {
        LOG.info("Forwarding rules mgr registered");
        serviceableNodes.add(new ServiceableNode(serviceName, object));
        LOG.info("registered module {} and list is {}", object.getClass(), serviceableNodes );

    }

    public void doReconcile(FlowCapableNode node){


    }
}
