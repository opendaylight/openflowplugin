/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.old.notification.supplier.rev150820;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.config.api.DependencyResolver;
import org.opendaylight.controller.config.api.ModuleIdentifier;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.tools.OldNotifProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generated module introducer for OldNotificationSupplier Module class.
 */
public class OldNotifModule extends AbstractOldNotifModule {

    static final Logger LOG = LoggerFactory.getLogger(OldNotifModule.class);

    private static final String LOAD_SETTINGS_XML_FAIL = "Load the xml ConfigSubsystem input value fail!";
    private static final boolean DEFAULT_ITEM_NOTIF_ALLOWED = true;
    private static final boolean DEFAULT_STAT_NOTIF_ALLOWED = false;

    /**
     * Module constructor
     *
     * @param identifier - {@link ModuleIdentifier}
     * @param dependencyResolver - {@link DependencyResolver}
     */
    public OldNotifModule(final ModuleIdentifier identifier, final DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    /**
     * Module constructor
     *
     * @param identifier - {@link ModuleIdentifier}
     * @param dependencyResolver - {@link DependencyResolver}
     * @param oldModule - {@link OldNotifModule}
     * @param oldInstance - {@link AutoCloseable}
     */
    public OldNotifModule(final ModuleIdentifier identifier, final DependencyResolver dependencyResolver,
                          final OldNotifModule oldModule, final AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("OldNotificationSupplier module initialization.");
        LOG.warn("OldNotificationSupplier module is marked like DEPRECATED. Modul could supplie old notification only for lithium release.");
        final OldNotifProviderConfig config = createConfig();
        return null;
    }

    /*
     * Help method for populating all ConfigSubsystem values to OldNotifProviderConfig immutable value holder.
     */
    private OldNotifProviderConfig createConfig() {
        final OldNotificationSupplierSettings settings = Preconditions.checkNotNull(getOldNotificationSupplierSettings());
        final OldNotifProviderConfig.OldNotifProviderConfigBuilder builder = OldNotifProviderConfig.builder();
        if (settings.getFlowSupport() != null) {
            builder.setFlowSupport(settings.getFlowSupport());
        } else {
            LOG.warn(LOAD_SETTINGS_XML_FAIL + " FlowSupport value is set to {} ", DEFAULT_ITEM_NOTIF_ALLOWED);
            builder.setFlowSupport(DEFAULT_ITEM_NOTIF_ALLOWED);
        }

        if (settings.getMeterSupport() != null) {
            builder.setMeterSupport(settings.getMeterSupport());
        } else {
            LOG.warn(LOAD_SETTINGS_XML_FAIL + " MeterSupport value is set to {} ", DEFAULT_ITEM_NOTIF_ALLOWED);
            builder.setMeterSupport(DEFAULT_ITEM_NOTIF_ALLOWED);
        }
        if (settings.getGroupSupport() != null) {
            builder.setGroupSupport(settings.getGroupSupport());
        } else {
            LOG.warn(LOAD_SETTINGS_XML_FAIL + " GroupSupport value is set to {} ", DEFAULT_ITEM_NOTIF_ALLOWED);
            builder.setGroupSupport(DEFAULT_ITEM_NOTIF_ALLOWED);
        }
        if (settings.getNodeConnectorStatSupport() != null) {
            builder.setNodeConnectorStatSupport(settings.getNodeConnectorStatSupport());
        } else {
            LOG.warn(LOAD_SETTINGS_XML_FAIL + " NodeConnectorStatSupport value is set to {} ", DEFAULT_STAT_NOTIF_ALLOWED);
            builder.setNodeConnectorStatSupport(DEFAULT_STAT_NOTIF_ALLOWED);
        }
        if (settings.getFlowTableStatSupport() != null) {
            builder.setFlowTableStatSupport(settings.getFlowTableStatSupport());
        } else {
            LOG.warn(LOAD_SETTINGS_XML_FAIL + " FlowTableStatSupport value is set to {} ", DEFAULT_STAT_NOTIF_ALLOWED);
            builder.setFlowTableStatSupport(DEFAULT_STAT_NOTIF_ALLOWED);
        }
        if (settings.getGroupStatSupport() != null) {
            builder.setGroupStatSupport(settings.getGroupStatSupport());
        } else {
            LOG.warn(LOAD_SETTINGS_XML_FAIL + " GroupStatSupport value is set to {} ", DEFAULT_STAT_NOTIF_ALLOWED);
            builder.setGroupStatSupport(DEFAULT_STAT_NOTIF_ALLOWED);
        }
        if (settings.getMeterStatSupport() != null) {
            builder.setMeterStatSupport(settings.getMeterStatSupport());
        } else {
            LOG.warn(LOAD_SETTINGS_XML_FAIL + " MeterStatSupport value is set to {} ", DEFAULT_STAT_NOTIF_ALLOWED);
            builder.setMeterStatSupport(DEFAULT_STAT_NOTIF_ALLOWED);
        }
        if (settings.getQueueStatSupport() != null) {
            builder.setQueueStatSupport(settings.getQueueStatSupport());
        } else {
            LOG.warn(LOAD_SETTINGS_XML_FAIL + " QueueStatSupport value is set to {} ", DEFAULT_STAT_NOTIF_ALLOWED);
            builder.setQueueStatSupport(DEFAULT_STAT_NOTIF_ALLOWED);
        }
        if (settings.getFlowStatSupport() != null) {
            builder.setFlowStatSupport(settings.getFlowStatSupport());
        } else {
            LOG.warn(LOAD_SETTINGS_XML_FAIL + " QueueStatSupport value is set to {} ", DEFAULT_STAT_NOTIF_ALLOWED);
            builder.setFlowStatSupport(DEFAULT_STAT_NOTIF_ALLOWED);
        }
        return builder.build();
    }
}
