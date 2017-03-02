/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;

/**
 * Util class for injecting new deserializers into OpenflowJava
 */
public class DeserializerInjector {

    /**
     * Injects deserializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     */
    public static void injectDeserializers(final DeserializerExtensionProvider provider) {
        // Inject new deserializers here
        MatchDeserializerInjector.injectDeserializers(provider);
        ActionDeserializerInjector.injectDeserializers(provider);
        InstructionDeserializerInjector.injectDeserializers(provider);
        MultipartDeserializerInjector.injectDeserializers(provider);
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
