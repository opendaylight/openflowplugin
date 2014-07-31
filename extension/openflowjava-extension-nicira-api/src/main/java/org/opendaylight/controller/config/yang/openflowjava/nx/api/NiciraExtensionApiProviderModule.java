package org.opendaylight.controller.config.yang.openflowjava.nx.api;
public class NiciraExtensionApiProviderModule extends org.opendaylight.controller.config.yang.openflowjava.nx.api.AbstractNiciraExtensionApiProviderModule {
    public NiciraExtensionApiProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NiciraExtensionApiProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.openflowjava.nx.api.NiciraExtensionApiProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        // TODO: register service for NiciraExtensionCodecRegistrator
        throw new java.lang.UnsupportedOperationException();
    }

}
