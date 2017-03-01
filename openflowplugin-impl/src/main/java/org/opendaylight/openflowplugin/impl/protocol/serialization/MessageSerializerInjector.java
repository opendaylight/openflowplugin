/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.messages.FlowMessageSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.messages.GroupMessageSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.messages.MeterMessageSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.messages.PortMessageSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestMessageSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Util class for injecting new message serializers into OpenflowJava
 */
class MessageSerializerInjector {

    /**
     * Injects message serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    static void injectSerializers(final SerializerExtensionProvider provider) {
        // Inject new message serializers here using injector created by createInjector method
        final Function<Class<?>, Consumer<OFSerializer<? extends OfHeader>>> injector =
                createInjector(provider, EncodeConstants.OF13_VERSION_ID);

        injector.apply(FlowMessage.class).accept(new FlowMessageSerializer());
        injector.apply(MeterMessage.class).accept(new MeterMessageSerializer());
        injector.apply(PortMessage.class).accept(new PortMessageSerializer());
        injector.apply(GroupMessage.class).accept(new GroupMessageSerializer());
        injector.apply(MultipartRequest.class).accept(new MultipartRequestMessageSerializer());
    }

    /**
     * Create injector that will inject new serializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Class<?>, Consumer<OFSerializer<? extends OfHeader>>> createInjector(final SerializerExtensionProvider provider,
                                                                                                 final byte version) {
        return type -> serializer ->
                provider.registerSerializer(
                        new MessageTypeKey<>(version, type),
                        serializer);
    }

}
