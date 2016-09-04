package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.jobcoordinator.impl.rev160225;

import org.opendaylight.openflowplugin.applications.jobcoordinator.impl.JobCoordinator;

public class JobCoordinatorImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.jobcoordinator.impl.rev160225.AbstractJobCoordinatorImplModule {
    public JobCoordinatorImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public JobCoordinatorImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.jobcoordinator.impl.rev160225.JobCoordinatorImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        JobCoordinator jobCoordinator = new JobCoordinator();
        getOsgiBrokerDependency().registerProvider(jobCoordinator);
        return jobCoordinator;
    }

}
