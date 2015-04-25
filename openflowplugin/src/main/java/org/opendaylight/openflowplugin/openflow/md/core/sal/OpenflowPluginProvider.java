/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.Collection;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageCountDumper;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageObservatory;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.openflow.md.core.MDController;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManager;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFRoleManager;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.statistics.MessageSpyCounterImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OFPlugin provider implementation
 */
public class OpenflowPluginProvider implements BindingAwareProvider, AutoCloseable, OpenFlowPluginExtensionRegistratorProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginProvider.class);

    private BindingAwareBroker broker;

    private BundleContext context;

    private Collection<SwitchConnectionProvider> switchConnectionProviders;

    private MDController mdController;

    private MessageObservatory<DataContainer> messageCountProvider;

    private SalRegistrationManager registrationManager;

    private ExtensionConverterManager extensionConverterManager;

    private OfpRole role;

    private OFRoleManager roleManager;

    /**
     * Initialization of services and msgSpy counter
     */
    public void initialization() {
        messageCountProvider = new MessageSpyCounterImpl();
        extensionConverterManager = new ExtensionConverterManagerImpl();
        roleManager = new OFRoleManager(OFSessionUtil.getSessionManager());
        this.registerProvider();
    }

    /**
     * @param switchConnectionProvider
     */
    public void setSwitchConnectionProviders(Collection<SwitchConnectionProvider> switchConnectionProvider) {
        this.switchConnectionProviders = switchConnectionProvider;
    }

    /**
     * @return osgi context
     */
    public BundleContext getContext() {
        return context;
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.debug("onSessionInitiated");
        registrationManager = new SalRegistrationManager();
        registrationManager.onSessionInitiated(session);
        mdController = new MDController();
        mdController.setSwitchConnectionProviders(switchConnectionProviders);
        mdController.setMessageSpyCounter(messageCountProvider);
        mdController.setExtensionConverterProvider(extensionConverterManager);
        mdController.init();
        mdController.start();
    }

    @Override
    public void close() {
        LOG.debug("close");
        mdController.stop();
        mdController = null;
        registrationManager.close();
        registrationManager = null;
    }

    /**
     * @return BA default broker
     */
    public BindingAwareBroker getBroker() {
        return broker;
    }

    /**
     * dependencymanager requirement
     *
     * @param broker
     */
    public void setBroker(BindingAwareBroker broker) {
        this.broker = broker;
    }

    /**
     * dependencymanager requirement
     *
     * @param brokerArg
     */
    public void unsetBroker(BindingAwareBroker brokerArg) {
        this.broker = null;
    }

    private boolean hasAllDependencies() {
        if (this.broker != null && this.switchConnectionProviders != null) {
            return true;
        }
        return false;
    }

    /**
     * register providers for md-sal
     */
    private void registerProvider() {
        if (hasAllDependencies()) {
            this.broker.registerProvider(this, context);
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
     * @param newRole
     */
    public void fireRoleChange(OfpRole newRole) {
        if (!role.equals(newRole)) {
            LOG.debug("my role was chaged from {} to {}", role, newRole);
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
}
