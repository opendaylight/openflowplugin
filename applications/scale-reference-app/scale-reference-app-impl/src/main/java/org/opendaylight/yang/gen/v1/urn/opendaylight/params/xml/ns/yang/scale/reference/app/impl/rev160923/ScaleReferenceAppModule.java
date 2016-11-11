package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.scale.reference.app.impl.rev160923;

import org.opendaylight.scale.ScaleReferenceApp;

public class ScaleReferenceAppModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.scale.reference.app.impl.rev160923.AbstractScaleReferenceAppModule {
    public ScaleReferenceAppModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public ScaleReferenceAppModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.scale.reference.app.impl.rev160923.ScaleReferenceAppModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        ScaleReferenceApp scaleReferenceApp = new ScaleReferenceApp(
                getDataBrokerDependency(), getRpcRegistryDependency(),
                getPrioritytaskmgrDependency(),
                getSouthboundmanagerDependency());
        return scaleReferenceApp;
    }
}
