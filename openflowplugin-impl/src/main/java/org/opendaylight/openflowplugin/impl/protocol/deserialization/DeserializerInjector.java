/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import java.util.function.Consumer;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;

/**
 * Util class for injecting new deserializers into OpenflowJava
 */
public class DeserializerInjector {

    /**
     * Injects deserializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     */
    public static void injectDeserializers(final DeserializerExtensionProvider provider) {
        // Helper deserialization function
        final Function<Integer, Function<Class<?>, Consumer<OFGeneralDeserializer>>> registrator =
                code -> deserializedClass -> deserializer ->
                        provider.registerDeserializer(
                                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, code, deserializedClass),
                                deserializer);
    }
}
