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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
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

public final class MastershipChangeServiceManagerImpl implements MastershipChangeServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(MastershipChangeServiceManagerImpl.class);

    private final List<MastershipChangeService> serviceGroup = new ArrayList<>();
    private ReconciliationFrameworkRegistration rfRegistration = null;
    private MasterChecker masterChecker;

    @Nonnull
    @Override
    public MastershipChangeRegistration register(@Nonnull MastershipChangeService service) {
        LOG.debug("Mastership change service registered: {}", service);
        MastershipServiceDelegate registration = new MastershipServiceDelegate(service, () -> {
            LOG.debug("Mastership change service un-registered: {}", service);
            serviceGroup.remove(service);
        });
        serviceGroup.add(service);
        if (masterChecker!= null && masterChecker.isAnyDeviceMastered()) {
            fireBecomeOwnerAfterRegistration(registration);
        }
        return registration;
    }

    @Override
    public ReconciliationFrameworkRegistration reconciliationFrameworkRegistration(
            @Nonnull ReconciliationFrameworkEvent reconciliationFrameworkEvent) throws MastershipChangeException {
        LOG.debug("Reconciliation framework service registered: {}", reconciliationFrameworkEvent);
        if (rfRegistration != null) {
            throw new MastershipChangeException("Reconciliation framework already registered.");
        } else {
            rfRegistration = new ReconciliationFrameworkServiceDelegate(
                    reconciliationFrameworkEvent,
                    () -> {
                        LOG.debug("Reconciliation framework service un-registered: {}", rfRegistration);
                        rfRegistration = null;
                    }
            );
        }
        return rfRegistration;
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
    public void becomeSlaveOrDisconnect(@Nonnull final DeviceInfo deviceInfo) {
        if (rfRegistration != null) {
            ((ReconciliationFrameworkEvent)rfRegistration).onDeviceDisconnected(deviceInfo);
        }
        serviceGroup.forEach(mastershipChangeService -> mastershipChangeService.onLoseOwnership(deviceInfo));
    }

    @Override
    public ListenableFuture<ResultState> becomeMasterBeforeSubmittedDS(@Nonnull DeviceInfo deviceInfo) {
        return rfRegistration == null ? null :
                ((ReconciliationFrameworkEvent)rfRegistration).onDevicePrepared(deviceInfo);
    }

    @Override
    public void setMasterChecker(@Nonnull final MasterChecker masterChecker) {
        this.masterChecker = masterChecker;
    }

    @Override
    public boolean isReconciliationFrameworkRegistered() {
        return (rfRegistration != null);
    }

    @VisibleForTesting
    int serviceGroupListSize() {
        return serviceGroup.size();
    }

    private void fireBecomeOwnerAfterRegistration(@Nonnull final MastershipChangeService service) {
        masterChecker.listOfMasteredDevices().forEach(service::onBecomeOwner);
    }
}
