package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.config.openflow.plugin.impl.rev150327;

import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.impl.OpenFlowPluginProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenFlowProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.config.openflow.plugin.impl.rev150327.AbstractOpenFlowProviderModule {

    private static Logger LOG = LoggerFactory.getLogger(OpenFlowProviderModule.class);

    public OpenFlowProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OpenFlowProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.config.openflow.plugin.impl.rev150327.OpenFlowProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("Initializing new OFP southbound.");
        OpenFlowPluginProvider openflowPluginProvider = new OpenFlowPluginProviderImpl();

        openflowPluginProvider.setSwitchConnectionProviders(getOpenflowSwitchConnectionProviderDependency());
        openflowPluginProvider.setRole(getRole());
        openflowPluginProvider.initialize();

        return openflowPluginProvider;
    }

}
