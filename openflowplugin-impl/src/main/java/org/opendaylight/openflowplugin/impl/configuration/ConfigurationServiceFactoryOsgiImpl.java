/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.configuration;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// NOT @Singleton @Service - we do not want this OSGi specific implementation to be auto-discovered in a standalone env
public class ConfigurationServiceFactoryOsgiImpl extends ConfigurationServiceFactoryImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationServiceFactoryOsgiImpl.class);

    private final BundleContext bundleContext;

    public ConfigurationServiceFactoryOsgiImpl(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public ConfigurationService newInstance(final OpenflowProviderConfig providerConfig) {
        ConfigurationService cs = super.newInstance(providerConfig);
        update(cs);
        return cs;
    }

    private void update(final ConfigurationService configurationService) {
        LOG.info("Loading configuration from '{}' configuration file", OFConstants.CONFIG_FILE_ID);
        final ServiceReference<ConfigurationAdmin> serviceReference =
            bundleContext.getServiceReference(ConfigurationAdmin.class);
        if (serviceReference == null) {
            return;
        }
        final ConfigurationAdmin configurationAdmin = bundleContext.getService(serviceReference);
        if (configurationAdmin == null) {
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

            final Dictionary<String, Object> properties = configuration.getProperties();
            if (properties != null) {
                final Enumeration<String> keys = properties.keys();
                final Map<String, String> mapProperties = new HashMap<>(properties.size());

                while (keys.hasMoreElements()) {
                    final String key = keys.nextElement();
                    final String value = properties.get(key).toString();
                    mapProperties.put(key, value);
                }

                configurationService.update(mapProperties);
            }
        } finally {
            bundleContext.ungetService(serviceReference);
        }
    }
}
