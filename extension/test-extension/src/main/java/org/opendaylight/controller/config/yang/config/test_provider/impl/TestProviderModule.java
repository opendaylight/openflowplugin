/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.test_provider.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.openflowplugin.extension.test.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev130819.TestService;

public class TestProviderModule extends org.opendaylight.controller.config.yang.config.test_provider.impl.AbstractTestProviderModule {
    public TestProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public TestProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.test_provider.impl.TestProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        Test extTest = new Test();
        SalFlowService flowService = getRpcRegistryDependency().getRpcService(SalFlowService.class);
        extTest.setFlowService(flowService);

        final RpcRegistration<TestService> registry = getRpcRegistryDependency().addRpcImplementation(TestService.class, extTest);

        return new AutoCloseable() {

            @Override
            public void close() throws Exception {
                registry.close();
            }
        };
    }

}
