package org.opendaylight.openflowplugin.extension.onf;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;

@RunWith(MockitoJUnitRunner.class)
public class OnfExtensionProviderTest {

    @Mock
    SwitchConnectionProvider switchConnectionProvider;
    @Mock
    OpenFlowPluginExtensionRegistratorProvider openFlowPluginExtensionRegistratorProvider;
    @Mock
    ExtensionConverterRegistrator extensionConverterRegistrator;

    private OnfExtensionProvider onfExtensionProvider;

    @Before
    public void setUp() throws Exception {
        Mockito
                .when(openFlowPluginExtensionRegistratorProvider.getExtensionConverterRegistrator())
                .thenReturn(extensionConverterRegistrator);

        onfExtensionProvider =
                new OnfExtensionProvider(switchConnectionProvider, openFlowPluginExtensionRegistratorProvider);
    }

    @Test
    public void init() throws Exception {
        onfExtensionProvider.init();
    }

}