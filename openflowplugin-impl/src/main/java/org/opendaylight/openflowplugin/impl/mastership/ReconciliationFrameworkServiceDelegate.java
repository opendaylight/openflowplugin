/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.mastership;

import com.google.common.util.concurrent.FutureCallback;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;

import javax.annotation.Nonnull;

public class ReconciliationFrameworkServiceDelegate implements ReconciliationFrameworkEvent, ReconciliationFrameworkRegistration {

    private final ReconciliationFrameworkEvent service;
    private final MastershipChangeServiceManager manager;

    ReconciliationFrameworkServiceDelegate(final ReconciliationFrameworkEvent service,
                                           final MastershipChangeServiceManager manager) {
        this.service = service;
        this.manager = manager;
    }

    @Override
    public void close() throws Exception {
        ((MastershipChangeServiceManagerImpl)this.manager).unregisterReconciliationFramework();
        this.service.close();
    }

    @Override
    public void onDevicePrepared(@Nonnull DeviceInfo deviceInfo, @Nonnull FutureCallback<ResultState> callback) {
        this.service.onDevicePrepared(deviceInfo, callback);
    }

    @Override
    public void onDeviceDisconnected(@Nonnull DeviceInfo deviceInfo) {
        this.service.onDeviceDisconnected(deviceInfo);
    }

    @Override
    public String toString() {
        return service.toString();
    }
}
