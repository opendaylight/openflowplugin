package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.inventory.manager.impl.rev150530;

import org.opendaylight.controller.sal.common.util.NoopAutoCloseable;

/**
 * @deprecated Replaced by blueprint wiring
 */
@Deprecated
public class InventoryManagerImplModule extends AbstractInventoryManagerImplModule {
    public InventoryManagerImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public InventoryManagerImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.inventory.manager.impl.rev150530.InventoryManagerImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public AutoCloseable createInstance() {
        // InventoryActivator instance is created via blueprint so this in a no-op.
        return NoopAutoCloseable.INSTANCE;
    }
}
