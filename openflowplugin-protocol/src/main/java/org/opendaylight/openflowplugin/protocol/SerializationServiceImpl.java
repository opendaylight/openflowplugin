package org.opendaylight.openflowplugin.protocol;

import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.protocol.SerializationService;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.protocol.deserialization.DeserializerInjector;
import org.opendaylight.openflowplugin.protocol.serialization.SerializerInjector;

public class SerializationServiceImpl implements SerializationService {
    private final ExtensionConverterProvider extensionConverterProvider;

    public SerializationServiceImpl(final ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @Override
    public void injectSerializers(final SwitchConnectionProvider provider) {
        SerializerInjector.injectSerializers(provider, extensionConverterProvider);
        DeserializerInjector.injectDeserializers(provider, extensionConverterProvider);
    }

    @Override
    public void revertSerializers(final SwitchConnectionProvider provider) {
        DeserializerInjector.revertDeserializers(provider);
    }
}