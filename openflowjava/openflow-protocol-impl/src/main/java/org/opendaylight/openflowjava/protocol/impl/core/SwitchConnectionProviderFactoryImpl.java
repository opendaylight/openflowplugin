/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static java.util.Objects.requireNonNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProviderFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506.SwitchConnectionConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of the SwitchConnectionProviderFactory interface.
 */
@Singleton
@Component
public class SwitchConnectionProviderFactoryImpl implements SwitchConnectionProviderFactory {
    private final DiagStatusService diagStatus;

    @Inject
    @Activate
    public SwitchConnectionProviderFactoryImpl(@Reference final DiagStatusService diagStatus) {
        this.diagStatus = requireNonNull(diagStatus);
    }

    @Override
    public SwitchConnectionProvider newInstance(final SwitchConnectionConfig config) {
        return new SwitchConnectionProviderImpl(diagStatus, new ConnectionConfigurationImpl(config));
    }
}
