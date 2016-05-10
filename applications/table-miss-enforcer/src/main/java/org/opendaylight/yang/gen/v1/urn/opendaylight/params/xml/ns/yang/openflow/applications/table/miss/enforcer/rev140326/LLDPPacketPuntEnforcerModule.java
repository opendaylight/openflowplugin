package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.table.miss.enforcer.rev140326;

import org.opendaylight.controller.sal.common.util.NoopAutoCloseable;

/**
 * @deprecated Replaced by blueprint wiring
 */
@Deprecated
public class LLDPPacketPuntEnforcerModule extends AbstractLLDPPacketPuntEnforcerModule {
    public LLDPPacketPuntEnforcerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LLDPPacketPuntEnforcerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.table.miss.enforcer.rev140326.LLDPPacketPuntEnforcerModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public AutoCloseable createInstance() {
        // LLDPPacketPuntEnforcer instance is created via blueprint so this in a no-op.
        return NoopAutoCloseable.INSTANCE;
    }
}
