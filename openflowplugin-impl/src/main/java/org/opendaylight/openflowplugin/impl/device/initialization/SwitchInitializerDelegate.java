/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.initialization;

import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.initialization.SwitchInitializer;
import org.opendaylight.openflowplugin.api.openflow.device.initialization.SwitchInitializerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwitchInitializerDelegate implements SwitchInitializerRegistration, SwitchInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchInitializerDelegate.class);

    private final SwitchInitializer service;
    private final AutoCloseable unregisterService;

    public SwitchInitializerDelegate(final SwitchInitializer service,
                                     final AutoCloseable unregisterService) {
        LOG.info("Switch initializer registered: {}", service);
        this.service = service;
        this.unregisterService = unregisterService;
    }

    @Override
    public void close() throws Exception {
        LOG.info("Switch initializer un-registered: {}", service);
        this.unregisterService.close();
        this.service.close();
    }

    @Override
    public Future<Void> onDevicePrepared(@Nonnull DeviceInfo deviceInfo) {
        return service.onDevicePrepared(deviceInfo);
    }

    @Override
    public String toString() {
        return service.toString();
    }
}
