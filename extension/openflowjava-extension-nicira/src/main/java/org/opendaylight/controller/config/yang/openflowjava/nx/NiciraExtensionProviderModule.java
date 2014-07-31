package org.opendaylight.controller.config.yang.openflowjava.nx;

import org.opendaylight.openflowjava.nx.NiciraExtensionsRegistrator;

public class NiciraExtensionProviderModule extends org.opendaylight.controller.config.yang.openflowjava.nx.AbstractNiciraExtensionProviderModule {
    public NiciraExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NiciraExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.openflowjava.nx.NiciraExtensionProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        NiciraExtensionsRegistrator niciraExtensionsRegistrator = new NiciraExtensionsRegistrator(getNiciraExtensionCodecRegistratorDependency());
        niciraExtensionsRegistrator.registerNiciraExtensions();
        return niciraExtensionsRegistrator;
    }

}
