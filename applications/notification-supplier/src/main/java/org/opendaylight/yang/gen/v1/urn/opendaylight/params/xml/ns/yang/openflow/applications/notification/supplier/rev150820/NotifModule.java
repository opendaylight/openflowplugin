package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.notification.supplier.rev150820;

import org.opendaylight.openflowplugin.applications.notification.supplier.NotificationProviderImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.tools.NotificationProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.notification.supplier.rev150820.AbstractNotifModule {

    private static final Logger LOG = LoggerFactory.getLogger(NotifModule.class);
    public NotifModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NotifModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.notification.supplier.rev150820.NotifModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.debug("Initializing NotificationProviderImpl");
        NotificationProviderConfig.NotificationProviderConfigBuilder notificationProviderConfig = new NotificationProviderConfig.NotificationProviderConfigBuilder();
        NotificationSupplierSettings notificationSupplierSettings = getNotificationSupplierSettings();
        notificationProviderConfig.setFlowSupport(notificationSupplierSettings.getFlowSupport());
        notificationProviderConfig.setNodeConnectorStatSupport(notificationSupplierSettings.getNodeConnectorStatSupport());
        notificationProviderConfig.setFlowStatSupport(notificationSupplierSettings.getFlowStatSupport());
        notificationProviderConfig.setQueueStatSupport(notificationSupplierSettings.getQueueStatSupport());
        notificationProviderConfig.setMeterSupport(notificationSupplierSettings.getMeterSupport());
        notificationProviderConfig.setGroupSupport(notificationSupplierSettings.getGroupSupport());
        notificationProviderConfig.setMeterStatSupport(notificationSupplierSettings.getMeterStatSupport());
        notificationProviderConfig.setGroupStatSupport(notificationSupplierSettings.getGroupStatSupport());
        notificationProviderConfig.setFlowTableStatSupport(notificationSupplierSettings.getFlowTableStatSupport());
        NotificationProviderImpl notificationProvider = new NotificationProviderImpl(notificationProviderConfig.build(),
                getNotificationServiceDependency(),getDataBrokerDependency());
        return notificationProvider;
    }

}
