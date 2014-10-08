package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.ofjava.cof.api.config.rev141008;

import org.opendaylight.openflowjava.cof.api.impl.CiscoExtensionCodecRegistratorImpl;

public class CiscoExtensionApiProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.ofjava.cof.api.config.rev141008.AbstractCiscoExtensionApiProviderModule {
    public CiscoExtensionApiProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public CiscoExtensionApiProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.ofjava.cof.api.config.rev141008.CiscoExtensionApiProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        return new CiscoExtensionCodecRegistratorImpl(getOpenflowSwitchConnectionProviderDependency());
    }

}
