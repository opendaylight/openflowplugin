/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.mastership;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MastershipServiceDelegate implements MastershipChangeService, MastershipChangeRegistration {

    private static final Logger LOG = LoggerFactory.getLogger(MastershipServiceDelegate.class);

    private final MastershipChangeService service;
    private final AutoCloseable unregisterService;

    MastershipServiceDelegate(final MastershipChangeService service,
                              final AutoCloseable unregisterService) {
        LOG.debug("Mastership change service registered: {}", service);
        this.service = service;
        this.unregisterService = unregisterService;
    }

    @Override
    public void close() throws Exception {
        LOG.debug("Mastership change service un-registered: {}", service);
        this.unregisterService.close();
    }

    @Override
    public void onBecomeOwner(@NonNull final DeviceInfo deviceInfo) {
        this.service.onBecomeOwner(deviceInfo);
    }

    @Override
    public void onLoseOwnership(@NonNull final DeviceInfo deviceInfo) {
        this.service.onLoseOwnership(deviceInfo);
    }

    @Override
    public String toString() {
        return service.toString();
    }
}
