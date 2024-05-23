/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.configuration;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

/**
 * A {@link ConfigurationService} based on OSGi Configuration Admin configuration
 */
@Singleton
@Component(service = ConfigurationService.class, configurationPid = OSGiConfiguration.CONFIGURATION_PID)
@Designate(ocd = OSGiConfiguration.class)
public final class OSGiConfigurationService implements ConfigurationService, AutoCloseable {
    private final Registration reg;

    @Inject
    public OSGiConfigurationService(final DataBroker dataBroker) {
        reg = dataBroker.registerDataListener(DataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION,
            InstanceIdentifier.create(OpenflowProviderConfig.class)), this::dataChangedTo);
    }

    @Activate
    public OSGiConfigurationService(@Reference final DataBroker dataBroker, final OSGiConfiguration configuration) {

    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        reg.close();
    }

    private void dataChangedTo(final @Nullable OpenflowProviderConfig data) {
        // TODO Auto-generated method stub
    }
}
