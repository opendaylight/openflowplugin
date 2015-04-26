package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.config.openflow.test.droptest.rev150327;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.droptestkaraf.DropTestProviderImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropTestProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.config.openflow.test.droptest.rev150327.AbstractDropTestProviderModule {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestProviderModule.class);
    public DropTestProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public DropTestProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.config.openflow.test.droptest.rev150327.DropTestProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.debug("Starting drop-test provider module.");
        DataBroker dataBroker = getDataBrokerDependency();
        NotificationProviderService notificationProviderService = getNotificationServiceDependency();
        SalFlowService salFlowService = getRpcRegistryDependency().getRpcService(SalFlowService.class);
        DropTestProviderImpl dropTestProvider = new DropTestProviderImpl(dataBroker, notificationProviderService, salFlowService);
        LOG.info("Drop-test provider module initialized.");
        return dropTestProvider;
    }

}
