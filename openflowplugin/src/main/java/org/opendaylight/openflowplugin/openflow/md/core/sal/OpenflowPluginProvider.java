/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageCountDumper;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageObservatory;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterManager;
import org.opendaylight.openflowplugin.openflow.md.core.MDController;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.role.OfEntityManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFRoleManager;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.statistics.MessageSpyCounterImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OFPlugin provider implementation
 */
public class OpenflowPluginProvider implements AutoCloseable, OpenFlowPluginExtensionRegistratorProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginProvider.class);

    private static final boolean SKIP_TABLE_FEATURES = false;

    private Collection<SwitchConnectionProvider> switchConnectionProviders;

    private MDController mdController;

    private MessageObservatory<DataContainer> messageCountProvider;

    private SalRegistrationManager registrationManager;

    private ExtensionConverterManager extensionConverterManager;

    private OfpRole role;
    private Boolean skipTableFeatures;

    private OFRoleManager roleManager;
    private OfEntityManager entManager;
    private DataBroker dataBroker;
    private NotificationProviderService notificationService;
    private RpcProviderRegistry rpcRegistry;
    private EntityOwnershipService entityOwnershipService;

    private OpenflowPluginConfig openflowPluginConfig;



    /**
     * Initialization of services and msgSpy counter
     */
    public void initialization() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();

        messageCountProvider = new MessageSpyCounterImpl();
        extensionConverterManager = new ExtensionConverterManagerImpl();
        roleManager = new OFRoleManager(OFSessionUtil.getSessionManager());
        openflowPluginConfig = readConfig(skipTableFeatures);
        entManager = new OfEntityManager(entityOwnershipService,getOpenflowPluginConfig());
        entManager.setDataBroker(dataBroker);
        entManager.init();

        LOG.debug("dependencies gathered..");
        registrationManager = new SalRegistrationManager(convertorManager);
        registrationManager.setDataService(dataBroker);
        registrationManager.setPublishService(notificationService);
        registrationManager.setRpcProviderRegistry(rpcRegistry);
        registrationManager.setOfEntityManager(entManager);
        registrationManager.init();

        mdController = new MDController(convertorManager);
        mdController.setSwitchConnectionProviders(switchConnectionProviders);
        mdController.setMessageSpyCounter(messageCountProvider);
        mdController.setExtensionConverterProvider(extensionConverterManager);
        mdController.init();
        mdController.start();
    }

    /**
     * @param switchConnectionProvider switch connection provider
     */
    public void setSwitchConnectionProviders(Collection<SwitchConnectionProvider> switchConnectionProvider) {
        this.switchConnectionProviders = switchConnectionProvider;
    }

    @Override
    public void close() {
        LOG.debug("close");

        if(mdController != null) {
            mdController.stop();
            mdController = null;
        }

        if(registrationManager != null) {
            registrationManager.close();
            registrationManager = null;
        }
    }

    public MessageCountDumper getMessageCountDumper() {
        return messageCountProvider;
    }

    /**
     * @return the extensionConverterRegistry
     */
    @Override
    public ExtensionConverterRegistrator getExtensionConverterRegistrator() {
        return extensionConverterManager;
    }

    /**
     * @param role of instance
     */
    public void setRole(OfpRole role) {
        this.role = role;
    }

    /**
     * @param newRole new controller role
     */
    public void fireRoleChange(OfpRole newRole) {
        if (!role.equals(newRole)) {
            LOG.debug("Controller role was changed from {} to {}", role, newRole);
            role = newRole;
            switch (role) {
                case BECOMEMASTER:
                    //TODO: implement appropriate action
                    roleManager.manageRoleChange(role);
                    break;
                case BECOMESLAVE:
                    //TODO: implement appropriate action
                    roleManager.manageRoleChange(role);
                    break;
                case NOCHANGE:
                    //TODO: implement appropriate action
                    roleManager.manageRoleChange(role);
                    break;
                default:
                    LOG.warn("role not supported: {}", role);
                    break;
            }
        }
    }

    private OpenflowPluginConfig readConfig(Boolean skipTableFeatures){

        final OpenflowPluginConfig.OpenflowPluginConfigBuilder openflowCfgBuilder = OpenflowPluginConfig.builder();

        if(skipTableFeatures !=null){
            openflowCfgBuilder.setSkipTableFeatures(skipTableFeatures.booleanValue());
        } else{
            LOG.warn("Could not load XML configuration file via ConfigSubsystem! Fallback to default config value(s)");
            openflowCfgBuilder.setSkipTableFeatures(SKIP_TABLE_FEATURES);
        }

        return openflowCfgBuilder.build();
    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setNotificationService(NotificationProviderService notificationService) {
        this.notificationService = notificationService;
    }

    public void setRpcRegistry(RpcProviderRegistry rpcRegistry) {
        this.rpcRegistry = rpcRegistry;
    }

    public void setEntityOwnershipService(EntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = entityOwnershipService;
    }

    public void setSkipTableFeatures(Boolean skipTableFeatures) {
        this.skipTableFeatures = skipTableFeatures;
    }

    @VisibleForTesting
    public OpenflowPluginConfig getOpenflowPluginConfig() {
        return openflowPluginConfig;
    }

    @VisibleForTesting
    protected RpcProviderRegistry getRpcRegistry() {
        return rpcRegistry;
    }

    @VisibleForTesting
    protected NotificationProviderService getNotificationService() {
        return notificationService;
    }

    @VisibleForTesting
    protected DataBroker getDataBroker() {
        return dataBroker;
    }
}
