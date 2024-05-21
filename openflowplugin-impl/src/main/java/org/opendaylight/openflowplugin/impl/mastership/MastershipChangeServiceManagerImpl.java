/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.mastership;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.infrautils.utils.concurrent.LoggingFutures.addErrorLogging;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MasterChecker;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeException;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(immediate = true, service = MastershipChangeServiceManager.class)
public final class MastershipChangeServiceManagerImpl implements MastershipChangeServiceManager, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(MastershipChangeServiceManagerImpl.class);

    private final List<MastershipChangeService> serviceGroup = new CopyOnWriteArrayList<>();

    private ReconciliationFrameworkEvent rfService = null;
    private MasterChecker masterChecker = null;

    @Inject
    @Activate
    public MastershipChangeServiceManagerImpl() {
        // for DI only
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        serviceGroup.clear();
    }

    @Override
    public Registration register(final MastershipChangeService service) {
        serviceGroup.add(requireNonNull(service));
        final var registration = new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                serviceGroup.remove(service);
            }
        };

        if (masterChecker != null && masterChecker.isAnyDeviceMastered()) {
            masterChecker.listOfMasteredDevices().forEach(service::onBecomeOwner);
        }
        return registration;
    }

    @Override
    public synchronized Registration reconciliationFrameworkRegistration(
            final ReconciliationFrameworkEvent reconciliationFrameworkEvent) throws MastershipChangeException {
        if (rfService != null) {
            throw new MastershipChangeException("Reconciliation framework already registered.");
        }
        rfService = requireNonNull(reconciliationFrameworkEvent);
        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                synchronized (MastershipChangeServiceManagerImpl.this) {
                    rfService = null;
                }
            }
        };
    }

    @Override
    public void becomeMaster(@NonNull final DeviceInfo deviceInfo) {
        serviceGroup.forEach(mastershipChangeService -> mastershipChangeService.onBecomeOwner(deviceInfo));
    }

    @Override
    public void becomeSlaveOrDisconnect(@NonNull final DeviceInfo deviceInfo) {
        if (rfService != null) {
            final var future = rfService.onDeviceDisconnected(deviceInfo);
            // TODO This null future check here should ideally not be required, but some tests currently rely on it
            if (future != null) {
                addErrorLogging(future, LOG, "onDeviceDisconnected() failed");
            }
        }
        serviceGroup.forEach(mastershipChangeService -> mastershipChangeService.onLoseOwnership(deviceInfo));
    }

    @Override
    public ListenableFuture<ResultState> becomeMasterBeforeSubmittedDS(final DeviceInfo deviceInfo) {
        return rfService == null ? Futures.immediateFuture(ResultState.DONOTHING)
            : rfService.onDevicePrepared(deviceInfo);
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
