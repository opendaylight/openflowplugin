/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.onf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;

@RunWith(MockitoJUnitRunner.class)
public class OnfExtensionProviderTest {

    @Mock
    private SwitchConnectionProvider switchConnectionProvider;
    @Mock
    private ConverterExecutor converterExecutor;
    @Mock
    private ExtensionConverterManager extensionConverterManager;

    private OnfExtensionProvider onfExtensionProvider;

    @Before
    public void setUp() throws Exception {
        onfExtensionProvider =
                new OnfExtensionProvider(switchConnectionProvider, extensionConverterManager, converterExecutor);
    }

    @Test
    public void init() throws Exception {
        onfExtensionProvider.init();
        Mockito.verify(switchConnectionProvider, Mockito.times(2))
                .registerExperimenterMessageSerializer(Mockito.any(), Mockito.any());
        Mockito.verify(switchConnectionProvider)
                .registerExperimenterMessageDeserializer(Mockito.any(), Mockito.any());
        Mockito.verify(switchConnectionProvider)
                .registerErrorDeserializer(Mockito.any(), Mockito.any());
        Mockito.verify(extensionConverterManager, Mockito.times(2))
                .registerMessageConvertor(Mockito.<TypeVersionKey<? extends ExperimenterMessageOfChoice>>any(),
                        Mockito.any());
    }

}