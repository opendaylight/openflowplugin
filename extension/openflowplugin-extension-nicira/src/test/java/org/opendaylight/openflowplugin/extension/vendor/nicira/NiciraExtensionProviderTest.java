/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;

/**
 * Test for {@link NiciraExtensionProvider}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NiciraExtensionProviderTest {

    @Mock
    private ExtensionConverterRegistrator extensionConverterRegistrator;

    private NiciraExtensionProvider niciraExtensionProvider;

    @Before
    public void setUp() throws Exception {

        niciraExtensionProvider = new NiciraExtensionProvider();
        niciraExtensionProvider.setExtensionConverterRegistrator(extensionConverterRegistrator);
    }

    @Test
    public void testRegisterConverters() throws Exception {
        niciraExtensionProvider.registerConverters();
        Mockito.verify(extensionConverterRegistrator, Mockito.atLeastOnce()).registerActionConvertor(Matchers.<TypeVersionKey<? extends Action>>any(), Matchers.<ConvertorActionToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action>>any());
    }
}