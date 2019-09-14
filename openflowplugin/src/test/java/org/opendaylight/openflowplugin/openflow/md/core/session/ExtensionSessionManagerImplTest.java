/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterManager;
import org.opendaylight.openflowplugin.extension.api.core.session.ExtensionSessionManager;

/**
 * test of {@link ExtensionSessionManagerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtensionSessionManagerImplTest {

    @Mock
    private ExtensionConverterManager extensionConverterManager;

    private ExtensionSessionManager sm;


    /**
     * prepare session manager.
     */
    @Before
    public void setUp() {
        sm = ExtensionSessionManagerImpl.getInstance();
    }

    @Test
    public void setExtensionConverterProvider() {
        sm.setExtensionConverterProvider(extensionConverterManager);
        assertEquals(extensionConverterManager, sm.getExtensionConverterProvider());
    }
}
