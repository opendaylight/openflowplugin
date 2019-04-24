/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.mastership;

import static org.opendaylight.infrautils.utils.concurrent.LoggingFutures.addErrorLogging;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Service;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MasterChecker;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeException;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkEvent;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Service(classes = MastershipChangeServiceManager.class)
public final class MastershipChangeServiceManagerImpl implements MastershipChangeServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(MastershipChangeServiceManagerImpl.class);

    private final List<MastershipChangeService> serviceGroup = new CopyOnWriteArrayList<>();
    private ReconciliationFrameworkEvent rfService = null;
    private MasterChecker masterChecker;

    @NonNull
    @Override
    public MastershipChangeRegistration register(@NonNull MastershipChangeService service) {
        final MastershipServiceDelegate registration =
                new MastershipServiceDelegate(service, () -> serviceGroup.remove(service));
        serviceGroup.add(service);
        if (masterChecker != null && masterChecker.isAnyDeviceMastered()) {
            masterChecker.listOfMasteredDevices().forEach(service::onBecomeOwner);
        }
        return registration;
    }

    @Override
    public ReconciliationFrameworkRegistration reconciliationFrameworkRegistration(
            @NonNull ReconciliationFrameworkEvent reconciliationFrameworkEvent) throws MastershipChangeException {
        if (rfService != null) {
            throw new MastershipChangeException("Reconciliation framework already registered.");
        } else {
            rfService = reconciliationFrameworkEvent;
            return new ReconciliationFrameworkServiceDelegate(reconciliationFrameworkEvent, () -> rfService = null);
        }
    }

    @Override
    public void close() {
        serviceGroup.clear();
    }

    @Override
    public void becomeMaster(@NonNull final DeviceInfo deviceInfo) {
        serviceGroup.forEach(mastershipChangeService -> mastershipChangeService.onBecomeOwner(deviceInfo));
    }

    @Override
    public void becomeSlaveOrDisconnect(@NonNull final DeviceInfo deviceInfo) {
        if (rfService != null) {
            ListenableFuture<Void> future = rfService.onDeviceDisconnected(deviceInfo);
            // TODO This null future check here should ideally not be required, but some tests currently rely on it
            if (future != null) {
                addErrorLogging(future, LOG, "onDeviceDisconnected() failed");
            }
        }
        serviceGroup.forEach(mastershipChangeService -> mastershipChangeService.onLoseOwnership(deviceInfo));
    }

    @Override
    public ListenableFuture<ResultState> becomeMasterBeforeSubmittedDS(@NonNull DeviceInfo deviceInfo) {
        return rfService == null ? null : rfService.onDevicePrepared(deviceInfo);
    }

    @Override
    public void setMasterChecker(@NonNull final MasterChecker masterChecker) {
        this.masterChecker = masterChecker;
    }

    @Override
    public boolean isReconciliationFrameworkRegistered() {
        return rfService != null;
    }

    @VisibleForTesting
    int serviceGroupListSize() {
        return serviceGroup.size();
    }
}
