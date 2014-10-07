/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;


import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;

/**
 * @author michal.polkorab
 *
 */
public class OpenflowPluginProviderTest {

    @Mock BindingAwareBroker baBroker;
    @Mock SwitchConnectionProvider switchProvider;

    OpenflowPluginProvider provider = new OpenflowPluginProvider();

    /**
     * Initializes mocks
     */
    @Before
    public void startUp() {
        MockitoAnnotations.initMocks(this);
        provider = new OpenflowPluginProvider();
    }

    /**
     * Test {@link OpenflowPluginProvider#initialization()}
     */
    @Test
    public void testInitialization() {
        ArrayList<SwitchConnectionProvider> switchProviders = new ArrayList<>();
        switchProviders.add(switchProvider);
        provider.setSwitchConnectionProviders(switchProviders);
        provider.setBroker(baBroker);
        provider.initialization();

        Assert.assertNotNull("Wrong message count dumper", provider.getMessageCountDumper());
        Assert.assertNotNull("Wrong extension converter registrator", provider.getExtensionConverterRegistrator());
        Assert.assertNull("Wrong context", provider.getContext());
        Assert.assertNotNull("Wrong broker", provider.getBroker());
    }
}