/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;

/**
 * Util class for injecting new serializers into OpenflowJava
 */
public class SerializerInjector {

    /**
     * Injects serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    public static void injectSerializers(final SerializerExtensionProvider provider) {
        // Inject new serializers here
        MatchSerializerInjector.injectSerializers(provider);
        ActionSerializerInjector.injectSerializers(provider);
        InstructionSerializerInjector.injectSerializers(provider);
        MultipartSerializerInjector.injectSerializers(provider);
        MessageSerializerInjector.injectSerializers(provider);
    }
}
