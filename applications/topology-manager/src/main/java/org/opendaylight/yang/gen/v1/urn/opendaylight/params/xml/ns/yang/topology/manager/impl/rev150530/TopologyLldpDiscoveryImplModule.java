package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.manager.impl.rev150530;

import org.opendaylight.openflowplugin.applications.topology.manager.FlowCapableTopologyProvider;

public class TopologyLldpDiscoveryImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.manager.impl.rev150530.AbstractTopologyLldpDiscoveryImplModule {
    public TopologyLldpDiscoveryImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public TopologyLldpDiscoveryImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.manager.impl.rev150530.TopologyLldpDiscoveryImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        FlowCapableTopologyProvider provider = new FlowCapableTopologyProvider();
        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
