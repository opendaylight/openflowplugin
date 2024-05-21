/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.configuration;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.io.IOException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationServiceFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = ConfigurationServiceFactory.class)
public final class OSGiConfigurationServiceFactory extends ConfigurationServiceFactoryImpl {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiConfigurationServiceFactory.class);

    private final BundleContext bundleContext;

    @Activate
    public OSGiConfigurationServiceFactory(final BundleContext bundleContext) {
        this.bundleContext = requireNonNull(bundleContext);
    }

    @Override
    public ConfigurationService newInstance(final OpenflowProviderConfig providerConfig) {
        var cs = super.newInstance(providerConfig);
        update(cs);
        return cs;
    }

    private void update(final ConfigurationService configurationService) {
        LOG.info("Loading configuration from '{}' configuration file", OFConstants.CONFIG_FILE_ID);
        final var serviceReference = bundleContext.getServiceReference(ConfigurationAdmin.class);
        if (serviceReference == null) {
            LOG.debug("No Configuration Admin Service available");
            return;
        }
        final var configurationAdmin = bundleContext.getService(serviceReference);
        if (configurationAdmin == null) {
            LOG.debug("Failed to get Configuration Admin Service from {}", serviceReference);
            return;
        }

        try {
            final Configuration configuration;
            try {
                configuration = configurationAdmin.getConfiguration(OFConstants.CONFIG_FILE_ID);
            } catch (IOException e) {
                LOG.warn("Failed to load {} configuration file", OFConstants.CONFIG_FILE_ID, e);
                return;
            }

            final var properties = configuration.getProperties();
            if (properties != null) {
                configurationService.update(Maps.transformValues(FrameworkUtil.asMap(properties), Object::toString));
            }
        } finally {
            bundleContext.ungetService(serviceReference);
        }
    }
}
