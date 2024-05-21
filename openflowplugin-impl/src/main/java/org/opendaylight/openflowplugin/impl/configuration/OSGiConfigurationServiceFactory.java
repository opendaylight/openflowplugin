/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.configuration;

import com.google.common.collect.Maps;
import java.util.Map;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationServiceFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = ConfigurationServiceFactory.class, configurationPid = OFConstants.CONFIG_FILE_ID)
@Designate(ocd = OSGiConfiguration.class)
public final class OSGiConfigurationServiceFactory extends ConfigurationServiceFactoryImpl {
    private final Map<String, String> properties;

    @Activate
    public OSGiConfigurationServiceFactory(final Map<String, ?> properties) {
        this.properties = Map.copyOf(Maps.transformValues(properties, Object::toString));
    }

    @Override
    public ConfigurationService newInstance(final OpenflowProviderConfig providerConfig) {
        var cs = super.newInstance(providerConfig);
        cs.update(properties);
        return cs;
    }
}
