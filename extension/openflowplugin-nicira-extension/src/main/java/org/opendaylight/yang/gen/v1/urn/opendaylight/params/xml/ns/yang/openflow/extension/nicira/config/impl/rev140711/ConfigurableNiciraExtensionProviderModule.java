package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.extension.nicira.config.impl.rev140711;

import org.opendaylight.openflowplugin.extension.vendor.nicira.NiciraExtensionProvider;

public class ConfigurableNiciraExtensionProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.extension.nicira.config.impl.rev140711.AbstractConfigurableNiciraExtensionProviderModule {
    public ConfigurableNiciraExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public ConfigurableNiciraExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.extension.nicira.config.impl.rev140711.ConfigurableNiciraExtensionProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        NiciraExtensionProvider niciraExtensionProvider = new NiciraExtensionProvider();
        niciraExtensionProvider.setExtensionConverterRegistrator(
                getOpenflowPluginProviderDependency().getExtensionConverterRegistrator());
        
        niciraExtensionProvider.registerConverters();
        
        return niciraExtensionProvider;
    }

}
