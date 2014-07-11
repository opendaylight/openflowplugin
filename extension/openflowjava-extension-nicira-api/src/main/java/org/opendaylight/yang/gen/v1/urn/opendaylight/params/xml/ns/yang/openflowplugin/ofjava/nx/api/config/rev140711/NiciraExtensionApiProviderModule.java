package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.ofjava.nx.api.config.rev140711;

import org.opendaylight.openflowjava.nx.api.impl.NiciraExtensionCodecRegistratorImpl;

public class NiciraExtensionApiProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.ofjava.nx.api.config.rev140711.AbstractNiciraExtensionApiProviderModule {
    public NiciraExtensionApiProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NiciraExtensionApiProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.ofjava.nx.api.config.rev140711.NiciraExtensionApiProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        return new NiciraExtensionCodecRegistratorImpl(getOpenflowSwitchConnectionProviderDependency());
    }

}
