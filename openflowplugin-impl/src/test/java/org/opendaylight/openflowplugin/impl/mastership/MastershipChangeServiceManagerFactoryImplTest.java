/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.mastership;

import com.google.common.util.concurrent.FutureCallback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MasterChecker;
import org.opendaylight.openflowplugin.api.openflow.mastership.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MastershipChangeServiceManagerFactoryImplTest {

    @Mock
    private MastershipChangeService service;
    @Mock
    private MastershipChangeService secondService;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private FutureCallback<ResultState> resultStateFutureCallback;
    @Mock
    private MasterChecker masterChecker;
    @Mock
    private ReconciliationFrameworkEvent event;
    @Mock
    private ReconciliationFrameworkEvent secondEvent;

    private MastershipChangeServiceManager manager;
    private MastershipChangeRegistration registration;

    @Before
    public void setUp(){
        final MastershipChangeServiceManagerFactory factory = new MastershipChangeServiceManagerFactoryImpl();
        manager = factory.newInstance();
        registration = manager.register(service);
    }

    @Test
    public void newInstance() throws Exception {
        Assert.assertNotNull(manager);
    }

    @Test
    public void registerUnregister() throws Exception {
        Assert.assertNotNull(registration);
        Assert.assertTrue(((MastershipChangeServiceManagerImpl)manager).serviceGroupListSize() == 1);
        registration.close();
        Assert.assertTrue(((MastershipChangeServiceManagerImpl)manager).serviceGroupListSize() == 0);
    }

    @Test
    public void events() throws Exception {
        manager.becomeMaster(deviceInfo);
        Mockito.verify(service).onBecomeOwner(deviceInfo);
        manager.becomeSlaveOrDisconnect(deviceInfo);
        Mockito.verify(service).onLoseOwnership(deviceInfo);
        manager.becomeMasterBeforeSubmittedDS(deviceInfo, resultStateFutureCallback);
        Mockito.verify(service).onLoseOwnership(deviceInfo);
        registration.close();
    }

    @Test
    public void envokeEventAfterRegistration() throws Exception {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        deviceInfos.add(deviceInfo);
        manager.setMasterChecker(masterChecker);
        Mockito.when(masterChecker.isAnyDeviceMastered()).thenReturn(true);
        Mockito.when(masterChecker.listOfMasteredDevices()).thenReturn(deviceInfos);
        final MastershipChangeRegistration secondRegistration = manager.register(secondService);
        Mockito.verify(secondService).onBecomeOwner(deviceInfo);
        registration.close();
        secondRegistration.close();
    }

    @Test
    public void reconciliationFrameworkRegistration() throws Exception {
        manager.reconciliationFrameworkRegistration(event);
        manager.becomeMasterBeforeSubmittedDS(deviceInfo, resultStateFutureCallback);
        Mockito.verify(event).onDevicePrepared(deviceInfo, resultStateFutureCallback);
        registration.close();
        manager.close();
    }

    @Test
    public void twoReconciliationFrameworkRegistrations() throws Exception {
        manager.reconciliationFrameworkRegistration(event);
        manager.reconciliationFrameworkRegistration(secondEvent);
        manager.becomeMasterBeforeSubmittedDS(deviceInfo, resultStateFutureCallback);
        Mockito.verify(secondEvent, Mockito.never()).onDevicePrepared(deviceInfo, resultStateFutureCallback);
        Mockito.verify(event).onDevicePrepared(deviceInfo, resultStateFutureCallback);
        registration.close();
        manager.close();
    }
}