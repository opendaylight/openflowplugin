/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.mastership;

import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MasterChecker;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeException;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkEvent;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkRegistration;

@RunWith(MockitoJUnitRunner.class)
public class MastershipChangeServiceManagerImplTest {

    @Mock
    private MastershipChangeService service;
    @Mock
    private MastershipChangeService secondService;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private MasterChecker masterChecker;
    @Mock
    private ReconciliationFrameworkEvent event;
    @Mock
    private ReconciliationFrameworkEvent secondEvent;

    private final MastershipChangeServiceManager manager = new MastershipChangeServiceManagerImpl();
    private MastershipChangeRegistration registration;
    private ReconciliationFrameworkRegistration registrationRF;

    @Before
    public void setUp() throws Exception {
        registration = manager.register(service);
        registrationRF = manager.reconciliationFrameworkRegistration(event);

        Mockito.when(event.onDeviceDisconnected(Mockito.any())).thenReturn(Futures.immediateFuture(null));
    }

    @Test
    public void register() {
        Assert.assertNotNull(registration);
    }

    @Test
    public void registerTwice() {
        MastershipChangeRegistration registration2;
        registration2 = manager.register(secondService);
        Assert.assertNotNull(registration);
        Assert.assertNotNull(registration2);
    }

    @Test
    public void uregisterTwice() throws Exception {
        MastershipChangeRegistration registration2;
        registration2 = manager.register(secondService);
        Assert.assertTrue(((MastershipChangeServiceManagerImpl)manager).serviceGroupListSize() == 2);
        registration.close();
        Assert.assertTrue(((MastershipChangeServiceManagerImpl)manager).serviceGroupListSize() == 1);
        registration2.close();
        Assert.assertTrue(((MastershipChangeServiceManagerImpl)manager).serviceGroupListSize() == 0);
    }

    @Test
    public void reconciliationFrameworkRegistration() {
        Assert.assertNotNull(registrationRF);
    }

    @Test(expected = MastershipChangeException.class)
    public void reconciliationFrameworkRegistrationTwice() throws Exception {
        manager.reconciliationFrameworkRegistration(secondEvent);
    }

    @Test
    public void unregosteringRF() throws Exception {
        registrationRF.close();
        ReconciliationFrameworkRegistration registration1;
        registration1 = manager.reconciliationFrameworkRegistration(secondEvent);
        Assert.assertNotNull(registration1);
    }

    @Test
    public void becomeMaster() {
        manager.becomeMaster(deviceInfo);
        Mockito.verify(service).onBecomeOwner(deviceInfo);
        manager.becomeSlaveOrDisconnect(deviceInfo);
        Mockito.verify(service).onLoseOwnership(deviceInfo);
    }

    @Test
    public void becomeMasterBeforeDS() {
        manager.becomeMasterBeforeSubmittedDS(deviceInfo);
        Mockito.verify(event).onDevicePrepared(deviceInfo);
    }

    @Test
    public void isReconciliationFrameworkRegistered() throws Exception {
        Assert.assertTrue(manager.isReconciliationFrameworkRegistered());
        registrationRF.close();
        Assert.assertFalse(manager.isReconciliationFrameworkRegistered());
    }

    @Test
    public void evokeEventAfterRegistration() {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        deviceInfos.add(deviceInfo);
        manager.setMasterChecker(masterChecker);
        Mockito.when(masterChecker.isAnyDeviceMastered()).thenReturn(true);
        Mockito.when(masterChecker.listOfMasteredDevices()).thenReturn(deviceInfos);
        manager.register(secondService);
        Mockito.verify(secondService).onBecomeOwner(deviceInfo);
    }

}
