/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.mastership;

import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;

public class MastershipServiceDelegate implements MastershipChangeService, MastershipChangeRegistration {

    private final MastershipChangeService service;

    public MastershipServiceDelegate(final MastershipChangeService service) {
        this.service = service;
    }

    @Override
    public Future<Void> onBecomeOwner(@Nonnull DeviceInfo deviceInfo) {
        return this.service.onBecomeOwner(deviceInfo);
    }

    @Override
    public Future<Void> onLoseOwnership(@Nonnull DeviceInfo deviceInfo) {
        return this.service.onLoseOwnership(deviceInfo);
    }

    @Override
    public void close() throws Exception {
        this.service.close();
    }
}
