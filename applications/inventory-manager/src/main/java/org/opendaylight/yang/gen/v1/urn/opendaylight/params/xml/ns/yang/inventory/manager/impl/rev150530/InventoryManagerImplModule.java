package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.inventory.manager.impl.rev150530;

import org.opendaylight.openflowplugin.applications.inventory.manager.InventoryActivator;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;

public class InventoryManagerImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.inventory.manager.impl.rev150530.AbstractInventoryManagerImplModule {
    public InventoryManagerImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public InventoryManagerImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.inventory.manager.impl.rev150530.InventoryManagerImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        InventoryActivator provider = new InventoryActivator();
        provider.setOwnershipService(getOwnershipServiceDependency());
        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
