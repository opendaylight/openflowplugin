/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.initialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInitializerProviderTest {
    @Mock
    private AbstractDeviceInitializer abstractDeviceInitializer;
    private DeviceInitializerProvider provider;

    @Before
    public void setUp() {
        provider = new DeviceInitializerProvider();
    }

    @Test
    public void register() {
        provider.register(OFConstants.OFP_VERSION_1_3, abstractDeviceInitializer);
        final Optional<AbstractDeviceInitializer> lookup = provider.lookup(OFConstants.OFP_VERSION_1_3);
        assertTrue(lookup.isPresent());
        assertEquals(abstractDeviceInitializer, lookup.get());
    }

    @Test
    public void lookup() {
        assertFalse(provider.lookup(OFConstants.OFP_VERSION_1_0).isPresent());
    }

}