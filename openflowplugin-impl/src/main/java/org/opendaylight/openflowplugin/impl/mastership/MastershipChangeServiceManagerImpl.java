/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.mastership;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListenableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MasterChecker;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeException;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkEvent;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;

@Singleton
@Service(classes = MastershipChangeServiceManager.class)
public final class MastershipChangeServiceManagerImpl implements MastershipChangeServiceManager {

    private final List<MastershipChangeService> serviceGroup = new CopyOnWriteArrayList<>();
    private ReconciliationFrameworkEvent rfService = null;
    private MasterChecker masterChecker;

    @Nonnull
    @Override
    public MastershipChangeRegistration register(@Nonnull MastershipChangeService service) {
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
            @Nonnull ReconciliationFrameworkEvent reconciliationFrameworkEvent) throws MastershipChangeException {
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
    public void becomeMaster(@Nonnull final DeviceInfo deviceInfo) {
        serviceGroup.forEach(mastershipChangeService -> mastershipChangeService.onBecomeOwner(deviceInfo));
    }

    @Override
    // FB flags this for onDeviceDisconnected but unclear why - seems a false positive.
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void becomeSlaveOrDisconnect(@Nonnull final DeviceInfo deviceInfo) {
        if (rfService != null) {
            rfService.onDeviceDisconnected(deviceInfo);
        }
        serviceGroup.forEach(mastershipChangeService -> mastershipChangeService.onLoseOwnership(deviceInfo));
    }

    @Override
    public ListenableFuture<ResultState> becomeMasterBeforeSubmittedDS(@Nonnull DeviceInfo deviceInfo) {
        return rfService == null ? null : rfService.onDevicePrepared(deviceInfo);
    }

    @Override
    public void setMasterChecker(@Nonnull final MasterChecker masterChecker) {
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
