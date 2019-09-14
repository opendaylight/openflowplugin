/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.mastership;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;

@RunWith(MockitoJUnitRunner.class)
public class MastershipServiceDelegateTest {
    @Mock
    private MastershipChangeService mastershipChangeService;
    @Mock
    private AutoCloseable unregisterService;
    @Mock
    private DeviceInfo deviceInfo;

    private MastershipServiceDelegate mastershipServiceDelegate;

    @Before
    public void setUp() {
        mastershipServiceDelegate = new MastershipServiceDelegate(mastershipChangeService, unregisterService);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mastershipChangeService);
        verifyNoMoreInteractions(unregisterService);
    }

    @Test
    public void close() throws Exception {
        mastershipServiceDelegate.close();
        verify(unregisterService).close();
    }

    @Test
    public void onBecomeOwner() {
        mastershipServiceDelegate.onBecomeOwner(deviceInfo);
        verify(mastershipChangeService).onBecomeOwner(deviceInfo);
    }

    @Test
    public void onLoseOwnership() {
        mastershipServiceDelegate.onLoseOwnership(deviceInfo);
        verify(mastershipChangeService).onLoseOwnership(deviceInfo);
    }
}