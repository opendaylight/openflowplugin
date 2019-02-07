/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import java.util.Set;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.ArbitratorReconcileService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class RpcProviderRegistryMock implements RpcConsumerRegistry, RpcProviderService {
    @Override
    public <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
            T implementation) {
        return null;
    }

    @Override
    public <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
            T implementation, Set<InstanceIdentifier<?>> paths) {
        return null;
    }

    @Override
    public <T extends RpcService> T getRpcService(Class<T> serviceInterface) {
        if (serviceInterface.equals(SalFlowService.class)) {
            return (T) new SalFlowServiceMock();
        } else if (serviceInterface.equals(SalGroupService.class)) {
            return (T) new SalGroupServiceMock();
        } else if (serviceInterface.equals(SalMeterService.class)) {
            return (T) new SalMeterServiceMock();
        } else if (serviceInterface.equals(SalTableService.class)) {
            return (T) new SalTableServiceMock();
        } else if (serviceInterface.equals(SalBundleService.class)) {
            return (T) new SalBundleServiceMock();
        } else if (serviceInterface.equals(ArbitratorReconcileService.class)) {
            return (T) new ArbitratorReconcileServiceMock();
        } else {
            return null;
        }
    }


}
