/**
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.pipeline.manager.rev150925;

import org.opendaylight.openflowplugin.applications.pipeline_manager.*;

public class PipelineManagerModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.pipeline.manager.rev150925.AbstractPipelineManagerModule {
    public PipelineManagerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public PipelineManagerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.pipeline.manager.rev150925.PipelineManagerModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // NOOP
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        org.opendaylight.openflowplugin.applications.pipeline_manager.PipelineManager pipelineManager =
                new PipelineManagerImpl(getDataBrokerDependency());
        return pipelineManager;
    }

}
