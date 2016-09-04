package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.prioritytaskmgr.impl.rev160211;

import org.opendaylight.openflowplugin.applications.prioritytaskmgr.impl.PriorityTaskManagerImpl;

public class PriorityTaskManagerImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.prioritytaskmgr.impl.rev160211.AbstractPriorityTaskManagerImplModule {
    public PriorityTaskManagerImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public PriorityTaskManagerImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.prioritytaskmgr.impl.rev160211.PriorityTaskManagerImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        PriorityTaskManagerImpl priorityTaskManagerImpl = new PriorityTaskManagerImpl();
        getBrokerDependency().registerProvider(priorityTaskManagerImpl);
        return priorityTaskManagerImpl;
    }

}
