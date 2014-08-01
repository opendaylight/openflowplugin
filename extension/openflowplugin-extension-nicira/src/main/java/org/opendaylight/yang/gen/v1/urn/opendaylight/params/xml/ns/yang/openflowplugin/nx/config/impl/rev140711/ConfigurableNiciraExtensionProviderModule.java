package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.nx.config.impl.rev140711;

import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.vendor.nicira.NiciraExtensionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurableNiciraExtensionProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.nx.config.impl.rev140711.AbstractConfigurableNiciraExtensionProviderModule {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ConfigurableNiciraExtensionProviderModule.class);
    public ConfigurableNiciraExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public ConfigurableNiciraExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.nx.config.impl.rev140711.ConfigurableNiciraExtensionProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOGGER.error("OpenflowPlugin EXTENSION NICIRA CREATE INSTANCE");
        // TODO:implement
        NiciraExtensionProvider provider = new NiciraExtensionProvider();
        ExtensionConverterRegistrator registrator = getOpenflowPluginProviderDependency().getExtensionConverterRegistrator();
        if (registrator == null) {
            LOGGER.error("REGISTRATOR IS NULL");
        } else {
            LOGGER.error("REGISTRATOR IS NOT NULL");
            provider.setExtensionConverterRegistrator(registrator);
        }
        return provider;
    }

}
