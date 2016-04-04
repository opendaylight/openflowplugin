package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.bulk.o.matic.rev150608;

import org.opendaylight.openflowplugin.applications.bulk.o.matic.BulkOMaticProviderImpl;

public class BulkOMaticModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.bulk.o.matic.rev150608.AbstractBulkOMaticModule {
    public BulkOMaticModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public BulkOMaticModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.bulk.o.matic.rev150608.BulkOMaticModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        return new BulkOMaticProviderImpl(getRpcRegistryDependency(), getDataBrokerDependency());
    }
}
