package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.cof.impl.config.rev141010;

import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.vendor.cisco.CiscoExtensionProvider;

public class ConfigurableCiscoExtensionProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.cof.impl.config.rev141010.AbstractConfigurableCiscoExtensionProviderModule {
    public ConfigurableCiscoExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public ConfigurableCiscoExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.cof.impl.config.rev141010.ConfigurableCiscoExtensionProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        CiscoExtensionProvider provider = new CiscoExtensionProvider();
        ExtensionConverterRegistrator registrator = getOpenflowPluginProviderDependency().getExtensionConverterRegistrator();
        provider.setExtensionConverterRegistrator(registrator);
        provider.registerConverters();
        return provider;
    }

}
