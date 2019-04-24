/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.mastership;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkEvent;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconciliationFrameworkServiceDelegate implements
        ReconciliationFrameworkEvent, ReconciliationFrameworkRegistration {

    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationFrameworkServiceDelegate.class);

    private final ReconciliationFrameworkEvent service;
    private final AutoCloseable unregister;

    ReconciliationFrameworkServiceDelegate(final ReconciliationFrameworkEvent service,
                                           final AutoCloseable unregisterService) {
        LOG.debug("Reconciliation framework service registered: {}", service);
        this.service = service;
        this.unregister = unregisterService;
    }

    @Override
    public void close() throws Exception {
        LOG.debug("Reconciliation framework service un-registered: {}", service);
        this.unregister.close();
        this.service.close();
    }

    @Override
    public ListenableFuture<ResultState> onDevicePrepared(@NonNull DeviceInfo deviceInfo) {
        return this.service.onDevicePrepared(deviceInfo);
    }

    @Override
    public ListenableFuture<Void> onDeviceDisconnected(@NonNull DeviceInfo deviceInfo) {
        return this.service.onDeviceDisconnected(deviceInfo);
    }

    @Override
    public String toString() {
        return service.toString();
    }
}
