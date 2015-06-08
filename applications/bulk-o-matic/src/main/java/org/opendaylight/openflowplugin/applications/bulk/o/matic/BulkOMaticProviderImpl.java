/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.SalBulkFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mirehak on 6/8/15.
 */
public class BulkOMaticProviderImpl implements BulkOMaticProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BulkOMaticProviderImpl.class);

    private final BindingAwareBroker.RpcRegistration<SalBulkFlowService> serviceRpcRegistration;

    public BulkOMaticProviderImpl(RpcProviderRegistry rpcRegistry, DataBroker dataBroker) {
        LOG.info("creating bulk-o-matic");
        SalFlowService flowService = rpcRegistry.getRpcService(SalFlowService.class);
        SalBulkFlowService bulkOMaticService = new SalBulkFlowServiceImpl(flowService, dataBroker);
        serviceRpcRegistration = rpcRegistry.addRpcImplementation(SalBulkFlowService.class, bulkOMaticService);
    }

    @Override
    public void close() {
        LOG.info("destroying bulk-o-matic");
        serviceRpcRegistration.close();
    }
}
