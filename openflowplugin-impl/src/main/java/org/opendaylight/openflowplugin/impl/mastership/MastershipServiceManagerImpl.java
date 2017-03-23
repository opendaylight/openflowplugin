/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.mastership;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MasterChecker;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MastershipServiceManagerImpl implements MastershipChangeServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(MastershipServiceManagerImpl.class);

    private final List<MastershipChangeService> serviceGroup = new LinkedList<>();
    private MasterChecker masterChecker;

    @Nonnull
    @Override
    public MastershipChangeRegistration register(@Nonnull MastershipChangeService service) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Mastership change service registered: {}", service);
        }
        MastershipServiceDelegate registration = new MastershipServiceDelegate(service, this);
        serviceGroup.add(registration);
        fireBecomeOwnerAfterRegistration(registration);
        return registration;
    }

    @Override
    public void unregister(@Nonnull MastershipChangeService service) {
        serviceGroup.remove(service);
    }

    @Override
    public void becomeMaster(@Nonnull final DeviceInfo deviceInfo) {
        serviceGroup.forEach(mastershipChangeService -> mastershipChangeService.onBecomeOwner(deviceInfo));
    }

    @Override
    public void becomeSlaveOrDisconnect(@Nonnull final DeviceInfo deviceInfo) {
        serviceGroup.forEach(mastershipChangeService -> mastershipChangeService.onLoseOwnership(deviceInfo));
    }

    @Override
    public void setMasterChecker(@Nonnull final MasterChecker masterChecker) {
        this.masterChecker = masterChecker;
    }

    private void fireBecomeOwnerAfterRegistration(@Nonnull final  MastershipChangeService service) {
        masterChecker.checkForMaster().forEach(service::onBecomeOwner);
    }
}
