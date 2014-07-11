package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.ofjava.nx.config.rev140711;

import org.opendaylight.openflowjava.nx.NiciraExtensionsRegistrator;

public class NiciraExtensionProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.ofjava.nx.config.rev140711.AbstractNiciraExtensionProviderModule {
    public NiciraExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NiciraExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.ofjava.nx.config.rev140711.NiciraExtensionProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        NiciraExtensionsRegistrator registrator = new NiciraExtensionsRegistrator(getNiciraExtensionCodecRegistratorDependency());
        registrator.registerNiciraExtensions();
        return registrator;
    }

}
