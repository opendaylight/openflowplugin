package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.southboundmanager.impl.rev160210;

import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.facade.OpenflowFacade;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.impl.TransactionTrackerFactoryImpl;

public class OpenflowFacadeImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.southboundmanager.impl.rev160210.AbstractOpenflowFacadeImplModule {
    public OpenflowFacadeImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OpenflowFacadeImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.southboundmanager.impl.rev160210.OpenflowFacadeImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        TransactionTrackerFactoryImpl transactionTrackerFactory = new TransactionTrackerFactoryImpl();
        OpenflowFacade openflowFacade = new OpenflowFacade(getRpcRegistryDependency(),
                getOwnershipServiceDependency(), getJobCoordinatorDependency(),
                transactionTrackerFactory, getPrioritytaskmgrDependency());
        getOsgiBrokerDependency().registerProvider(openflowFacade);
        return openflowFacade;
    }

}
