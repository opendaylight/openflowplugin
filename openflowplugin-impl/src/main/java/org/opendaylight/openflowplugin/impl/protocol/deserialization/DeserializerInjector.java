/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;

/**
 * Util class for injecting new deserializers into OpenflowJava
 */
public class DeserializerInjector {

    /**
     * Injects deserializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     */
    public static void injectDeserializers(final DeserializerExtensionProvider provider,
                                           final ExtensionConverterProvider extensionConverterProvider) {
        // Inject new deserializers here
        MatchDeserializerInjector.injectDeserializers(provider, extensionConverterProvider);
        ActionDeserializerInjector.injectDeserializers(provider);
        InstructionDeserializerInjector.injectDeserializers(provider);
        MultipartDeserializerInjector.injectDeserializers(provider, extensionConverterProvider);
        MessageDeserializerInjector.injectDeserializers(provider);
    }

    /**
     * Reverts original deserializers in provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     */
    public static void revertDeserializers(final DeserializerExtensionProvider provider) {
        MessageDeserializerInjector.revertDeserializers(provider);
    }
}
