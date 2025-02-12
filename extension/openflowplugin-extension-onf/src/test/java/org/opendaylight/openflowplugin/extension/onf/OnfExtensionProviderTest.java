/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.onf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

@ExtendWith(MockitoExtension.class)
class OnfExtensionProviderTest {
    @Mock
    private SwitchConnectionProvider switchConnectionProvider;
    @Mock
    private OpenFlowPluginExtensionRegistratorProvider ofpExtensionRegistratorProvider;
    @Mock
    private ExtensionConverterRegistrator extensionConverterRegistrator;
    @Mock
    private ObjectRegistration<?> reg;

    @Test
    void testConstruction() {
        doReturn(extensionConverterRegistrator).when(ofpExtensionRegistratorProvider)
            .getExtensionConverterRegistrator();
        doReturn(reg).when(extensionConverterRegistrator).registerMessageConvertor(any(MessageTypeKey.class), any());
        doReturn(reg).when(extensionConverterRegistrator).registerMessageConvertor(any(TypeVersionKey.class), any());

        try (var prov = new OnfExtensionProvider(switchConnectionProvider, ofpExtensionRegistratorProvider)) {
            verify(switchConnectionProvider, times(2)).registerExperimenterMessageSerializer(any(), any());
            verify(switchConnectionProvider).registerExperimenterMessageDeserializer(any(), any());
            verify(switchConnectionProvider).registerErrorDeserializer(any(), any());
            verify(extensionConverterRegistrator, times(2)).registerMessageConvertor(any(MessageTypeKey.class), any());
            verify(extensionConverterRegistrator, times(2)).registerMessageConvertor(any(TypeVersionKey.class), any());
            verifyNoInteractions(reg);
        }

        verify(reg, times(4)).close();
    }
}
