/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.config.yang.impl;

import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.flowprogrammer.FlowProgrammerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowProgrammerImplModule extends org.opendaylight.openflowplugin.flowprogrammer.config.yang.impl.AbstractFlowProgrammerImplModule {
    private static final Logger LOG = LoggerFactory.getLogger(FlowProgrammerImplModule.class);

    public FlowProgrammerImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public FlowProgrammerImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.openflowplugin.flowprogrammer.config.yang.impl.FlowProgrammerImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final FlowProgrammerImpl flowProgrammerImpl = new FlowProgrammerImpl();
        DataBroker dataBroker = getDataBrokerDependency();
        flowProgrammerImpl.setDataProvider(dataBroker);
        
        // close()
        final class AutoCloseableFlowProgrammer implements AutoCloseable {

            @Override
            public void close() {
                try
                {
                    flowProgrammerImpl.close();
                } catch (ExecutionException | InterruptedException e)
                {
                    LOG.error("\nFailed to close FlowProgrammerImpl  (instance {}) " +
                            "cleanly", this);
                }


                LOG.info("FlowProgrammerImpl (instance {}) torn down", this);
            }
        }

        AutoCloseable ret = new AutoCloseableFlowProgrammer();
        LOG.info("FlowProgrammerImpl (instance {}) initialized.", ret);
        return ret;
    }

}
