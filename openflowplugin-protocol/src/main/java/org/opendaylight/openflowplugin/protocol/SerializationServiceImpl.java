/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
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