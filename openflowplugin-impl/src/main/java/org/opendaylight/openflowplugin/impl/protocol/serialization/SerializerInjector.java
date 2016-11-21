/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import java.util.function.Consumer;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.messages.MeterMessageSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessage;

/**
 * Util class for injecting new serializers into OpenflowJava
 */
public class SerializerInjector {

    /**
     * Injects serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    public static void injectSerializers(final SerializerExtensionProvider provider) {
        // Helper serialization function
        final Function<Class<?>, Consumer<OFGeneralSerializer>> registrator =
                type -> serializer ->
                        provider.registerSerializer(
                                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, type),
                                serializer);

        // Meter mod serializer
        registrator.apply(MeterMessage.class).accept(new MeterMessageSerializer());
    }
}
